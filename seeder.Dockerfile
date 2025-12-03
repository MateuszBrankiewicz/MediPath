# Stage 1: Use the official mongo image as a source for mongosh
FROM mongo:latest as mongo_fetcher

# Stage 2: Use the standard node image (Debian-based) instead of Alpine
# changing from node:18-alpine to node:18-slim
FROM node:18-slim

# Install necessary libraries that mongosh might depend on (usually openssl)
RUN apt-get update && apt-get install -y \
    openssl \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy the mongosh binary
COPY --from=mongo_fetcher /usr/bin/mongosh /usr/local/bin/mongosh
RUN chmod +x /usr/local/bin/mongosh

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .

ENTRYPOINT ["/bin/sh", "init-db.sh"]
