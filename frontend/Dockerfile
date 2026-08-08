FROM node:22 AS build

WORKDIR /app

# Copy the entire frontend project
COPY . .

# Install dependencies of the actual React app
WORKDIR /app/frontend

RUN npm install

RUN npm run build

FROM nginx:alpine

COPY --from=build /app/frontend/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
