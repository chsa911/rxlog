FROM golang:1.22 as build
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go mod tidy
ENV CGO_ENABLED=0
RUN go build -o /svc
FROM gcr.io/distroless/base-debian12
COPY --from=build /svc /svc
EXPOSE 8084
ENTRYPOINT ["/svc"]
