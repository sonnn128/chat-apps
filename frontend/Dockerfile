### STAGE 1: Build ###
FROM node:21.7.3 AS build

#### make the 'app' folder the current working directory
WORKDIR /app

#### copy both 'package.json' and 'package-lock.json' (if available)
COPY package*.json ./

#### install project dependencies
RUN npm install

ARG VITE_REACT_APP_BASE_URL
# Debug: Print the variable to build logs
RUN echo "Building with VITE_REACT_APP_BASE_URL=$VITE_REACT_APP_BASE_URL"
# Write to .env file so Vite picks it up guaranteed
RUN echo "VITE_REACT_APP_BASE_URL=$VITE_REACT_APP_BASE_URL" > .env

#### copy things
COPY . .

#### generate build --prod
RUN npm run build

### STAGE 2: Run ###
FROM nginx:1.27-alpine

#### copy nginx conf
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

#### copy artifact build from the 'build environment'
COPY --from=build /app/dist /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]