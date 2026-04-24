FROM nginx:1.25-alpine

COPY playground/dist /usr/share/nginx/html
COPY playground/nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
