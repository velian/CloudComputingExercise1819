FROM nginx
RUN rm /etc/nginx/conf.d/default.conf
COPY frontend.nginx.conf /etc/nginx
