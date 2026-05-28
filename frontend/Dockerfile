FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
ARG VITE_API_URL
ARG VITE_OAUTH_ENABLED
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_OAUTH_ENABLED=$VITE_OAUTH_ENABLED
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1:80/ || exit 1
CMD ["nginx", "-g", "daemon off;"]
