FROM golang:1.20-alpine AS build
WORKDIR /src
COPY go.mod .
RUN go mod download
COPY . .
RUN go build -o /monitoring-service

FROM alpine:3.18
COPY --from=build /monitoring-service /monitoring-service
EXPOSE 8085
VOLUME ["/out"]
ENTRYPOINT ["/monitoring-service"]
