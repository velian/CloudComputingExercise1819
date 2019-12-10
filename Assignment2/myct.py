import os
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
    click.echo(f'container_path: {container_path}, host_path: {host_path}, target_path: {target_path}')
    complete_path = os.path.normpath(container_path + target_path)

    mkdir_command = f'sudo mkdir -p {complete_path}'
    command = f'sudo mount --rbind -o ro {host_path} {complete_path}'

    _ = subprocess.call(mkdir_command, shell=True)
    _ = subprocess.call(command, shell=True)

    print("Map complete") #./test /etc /etc2


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
    click.echo(
        f'container_path: {container_path}, namespace: {namespace}, limit: {limit}, executable: {executable}, args: {args}')

    # sudo chroot ./test /bin/bash


if __name__ == '__main__':
    cli()
