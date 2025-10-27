# Build
FROM --platform=linux/amd64 node:20-alpine AS build
WORKDIR /app
COPY package.json vite.config.js .
COPY src ./src
COPY index.html ./
RUN npm install && npm run build
# Serve
FROM --platform=linux/amd64 nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
RUN printf "server {\n  listen 80;\n  location / { root /usr/share/nginx/html; try_files $uri /index.html; }\n  location /api/ { proxy_pass http://gateway-java-spring:8080; }\n}\n" > /etc/nginx/conf.d/default.conf
EXPOSE 80
