FROM nginx
RUN rm /etc/nginx/conf.d/default.conf
RUN rm /etc/nginx/nginx.conf
COPY backend.nginx.conf /etc/nginx
RUN mv /etc/nginx/backend.nginx.conf /etc/nginx/nginx.conf
