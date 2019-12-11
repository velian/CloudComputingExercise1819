import os
import random
import string
import subprocess

import click


@click.group()
def cli():
    pass


@cli.command()
@click.argument('container_path', type=click.Path(exists=False))
def init(container_path):
    create_command = "sudo debootstrap stable " + container_path + "/ http://deb.debian.org/debian/"
    mount_command = f'sudo mount --bind {container_path}/proc {container_path}/proc'
    make_private_command = f'sudo mount --make-private {container_path}/proc'

    _ = subprocess.call(create_command, shell=True)
    _ = subprocess.call(mount_command, shell=True)
    _ = subprocess.call(make_private_command, shell=True)

    print("Container initialized")


@cli.command()
@click.argument('container_path', type=click.Path(exists=True))
@click.argument('host_path', type=click.Path(exists=True))
@click.argument('target_path', type=click.Path(exists=False))
def map(container_path, host_path, target_path):
    complete_path = os.path.normpath(container_path + target_path)

    mkdir_command = f'sudo mkdir -p {complete_path}'
    command = f'sudo mount --rbind -o ro {host_path} {complete_path}'

    _ = subprocess.call(mkdir_command, shell=True)
    _ = subprocess.call(command, shell=True)

    print("Map complete")


@cli.command(context_settings=dict(
    ignore_unknown_options=True,
    help_option_names=[],
))
@click.argument('container_path', type=click.Path(exists=True))
@click.option('--namespace', '-n', help='The namespace the process will join.')
@click.option('--limit', '-l', help='Resource usage limit')
@click.argument('executable', type=click.Path(exists=True))
@click.argument('args', nargs=-1, type=click.UNPROCESSED)
def run(container_path, namespace, limit, executable, args):
    subcommands = ['sudo']

    if limit is not None:
        controller, key = limit.split('=')[0].split('.', 1)
        value = limit.split('=')[1]
        group_name = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))

        create_command = f'cgcreate -g {controller}:{group_name}'
        set_command = f'cgset -r {controller}.{key}={value} {group_name}'

        _ = subprocess.call(create_command, shell=True)
        _ = subprocess.call(set_command, shell=True)

        exec_command = f'cgexec -g {controller}:{group_name}'
        subcommands.append(exec_command)

    if namespace is not None:
        kind = namespace.split('=')[0]
        pid = namespace.split('=')[1]

        enter_command = f'nsenter --pid=/proc/{pid}/ns/{kind}'
        subcommands.append(enter_command)

    unshare_command = f'unshare -p -f --mount-proc={container_path}/proc'
    chroot_command = f'chroot {container_path} {executable} {" ".join(args)}'
    subcommands.append(unshare_command)
    subcommands.append(chroot_command)

    _ = subprocess.call(' '.join(subcommands), shell=True)


if __name__ == '__main__':
    cli()
