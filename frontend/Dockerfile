FROM node:13.8.0-alpine as diamant_frontend_builder

ADD package.json /data/
WORKDIR /data
RUN npm install

ARG VUE_APP_API_ENDPOINT=http://localhost:8181/api/
ARG VUE_APP_KEYCLOAK_URL=http://localhost:9090/auth
ARG VUE_APP_VERSION=DEV

ENV VUE_APP_API_ENDPOINT=${VUE_APP_API_ENDPOINT} \
    VUE_APP_KEYCLOAK_URL=${VUE_APP_KEYCLOAK_URL} \
    VUE_APP_VERSION=${VUE_APP_VERSION}


ADD . /data
RUN npm run build

FROM nginx:1.21.1-alpine as diamant_frontend_runner
RUN rm /etc/nginx/conf.d/default.conf
COPY ./nginx.conf /etc/nginx/conf.d
COPY --from=diamant_frontend_builder /data/dist/ /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]
