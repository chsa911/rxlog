package main
import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"github.com/redis/go-redis/v9"
)
func main(){
	port := os.Getenv("SERVICE_PORT"); if port == "" { port = "8083" }
	rdb := redis.NewClient(&redis.Options{Addr: os.Getenv("REDIS_ADDR")})
	_ = rdb.Ping(context.Background()).Err()

	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) { w.Write([]byte("ok")) })
	http.HandleFunc("/api/autocomplete-go", func(w http.ResponseWriter, r *http.Request) { fmt.Fprintf(w, "autocomplete-go ok") })
	log.Println("autocomplete-go on :" + port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
