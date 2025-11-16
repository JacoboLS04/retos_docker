FROM node:20-alpine

# Create app directory
WORKDIR /usr/src/app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy package files
COPY package*.json ./

# Install all dependencies (including dev for testing)
RUN npm ci

# Copy source and test files
COPY . .

# Default env
ENV NODE_ENV=development
ENV PORT=3001

EXPOSE 3001

CMD ["node", "src/index.js"]
