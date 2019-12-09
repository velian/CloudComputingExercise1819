# Put the config file in the right folder
cp ./benchmark.com /etc/nginx/sites-available/benchmark.com
ln -s /etc/nginx/sites-available/benchmark.com /etc/nginx/sites-enabled/benchmark.com

# Create the file to be transfered
mkdir -p /www/data

dd if=/dev/zero of=/www/data/output.dat  bs=1M  count=1000

nginx
