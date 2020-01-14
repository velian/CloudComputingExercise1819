FROM nginx
RUN rm /etc/nginx/conf.d/default.conf
RUN rm /etc/nginx/nginx.conf
COPY frontend.nginx.conf /etc/nginx
RUN mv /etc/nginx/frontend.nginx.conf /etc/nginx/nginx.conf
