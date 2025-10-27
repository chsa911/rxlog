package main
import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"github.com/jackc/pgx/v5/pgxpool"
)
func main(){
	port := os.Getenv("SERVICE_PORT"); if port == "" { port = "8090" }
	dburl := os.Getenv("DATABASE_URL")
	pool, _ := pgxpool.New(context.Background(), dburl)
	defer func(){ if pool!=nil { pool.Close() } }()

	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) { w.Write([]byte("ok")) })
	http.HandleFunc("/api/research-go", func(w http.ResponseWriter, r *http.Request) { fmt.Fprintf(w, "research-go ok") })
	log.Println("research-go on :" + port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}
