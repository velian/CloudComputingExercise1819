server {
  listen 80 default_server;
  
  root /www/data;  
  
  server_name benchmark.com;
  
  location / {
    autoindex on;
    try_files $uri $uri/ =404;
  }
}
