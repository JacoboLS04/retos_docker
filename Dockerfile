FROM node:20-alpine

# Create app directory
WORKDIR /usr/src/app

# Install only production dependencies for runtime image
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev || npm install --production

# Copy source
COPY . .

# Default env
ENV NODE_ENV=production
ENV PORT=3001

EXPOSE 3001

CMD ["node", "src/index.js"]
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/servidor-web-spring-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]