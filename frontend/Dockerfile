### STAGE 1: Build ###
FROM node:21.7.3 AS build

WORKDIR /app

COPY ./frontend/package*.json ./

RUN npm install

ARG VITE_REACT_APP_BASE_URL

COPY ./frontend .

RUN npm run build

### STAGE 2: Run ###
FROM nginx:1.27-alpine

COPY ./frontend/nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]