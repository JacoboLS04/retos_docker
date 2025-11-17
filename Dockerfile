FROM node:18

WORKDIR /app

COPY package*.json ./
RUN npm install

# 👇 copiamos el código
COPY . .

# 👇 aseguramos que las plantillas queden disponibles en dist/templates
COPY src/templates ./dist/templates

CMD ["node", "dist/index.js"]
