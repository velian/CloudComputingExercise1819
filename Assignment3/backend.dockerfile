FROM nginx
RUN rm /etc/nginx/conf.d/default.conf
COPY backend.nginx.conf /etc/nginx
