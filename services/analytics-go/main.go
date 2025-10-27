package main
import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
)
func main(){
	port := os.Getenv("SERVICE_PORT"); if port == "" { port = "8084" }
	dburl := os.Getenv("DATABASE_URL")
	pool, _ := pgxpool.New(context.Background(), dburl)
	defer func(){ if pool!=nil { pool.Close() } }()
	rdb := redis.NewClient(&redis.Options{Addr: os.Getenv("REDIS_ADDR")})
	_ = rdb.Ping(context.Background()).Err()

	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) { w.Write([]byte("ok")) })
	http.HandleFunc("/api/analytics-go", func(w http.ResponseWriter, r *http.Request) { fmt.Fprintf(w, "analytics-go ok") })
	log.Println("analytics-go on :" + port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
