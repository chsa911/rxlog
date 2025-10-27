package main
import (
	"context"
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"os"
	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
)
type reserveReq struct{ Code string `json:"code"`; SizeRuleID int `json:"sizeRuleId"` }
type assignReq struct{ Code string `json:"code"`; BookID string `json:"bookId"` }
type releaseReq struct{ Code string `json:"code"`; BookID string `json:"bookId"` }
type verifyReq struct{ Code string `json:"code"`; Width int `json:"width"`; Height int `json:"height"` }
func jsonOK(w http.ResponseWriter, v any){ w.Header().Set("content-type", "application/json"); _ = json.NewEncoder(w).Encode(v) }
func main(){
	port := os.Getenv("SERVICE_PORT"); if port == "" { port = "8082" }
	dburl := os.Getenv("DATABASE_URL"); if dburl == "" { log.Fatal("DATABASE_URL required") }
	ctx := context.Background()
	pool, err := pgxpool.New(ctx, dburl); if err != nil { log.Fatalf("db: %v", err) }
	defer pool.Close()
	rdb := redis.NewClient(&redis.Options{Addr: os.Getenv("REDIS_ADDR")})
	if err := rdb.Ping(ctx).Err(); err != nil { log.Printf("redis ping: %v", err) }
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) { w.Write([]byte("ok")) })
	http.HandleFunc("/api/barcodes/reserve", func(w http.ResponseWriter, r *http.Request) {
		var req reserveReq; if err := json.NewDecoder(r.Body).Decode(&req); err != nil { http.Error(w,"bad json",400); return }
		_, err := pool.Exec(ctx, `INSERT INTO barcodes(code,status,size_rule_id,updated_at) VALUES($1,'RESERVED',$2,now())
			ON CONFLICT (code) DO UPDATE SET status='RESERVED', size_rule_id=$2, updated_at=now()`, req.Code, req.SizeRuleID)
		if err != nil { http.Error(w, err.Error(), 400); return }
		jsonOK(w, map[string]any{"status":"RESERVED"})
	})
	http.HandleFunc("/api/barcodes/assign", func(w http.ResponseWriter, r *http.Request) {
		var req assignReq; if err := json.NewDecoder(r.Body).Decode(&req); err != nil { http.Error(w,"bad json",400); return }
		if _, err := pool.Exec(ctx, `UPDATE barcodes SET status='ASSIGNED', updated_at=now() WHERE code=$1`, req.Code); err != nil { http.Error(w, err.Error(), 400); return }
		if _, err := pool.Exec(ctx, `INSERT INTO barcode_assignments(code,book_id) VALUES($1,$2) ON CONFLICT DO NOTHING`, req.Code, req.BookID); err != nil { http.Error(w, err.Error(), 400); return }
		jsonOK(w, map[string]any{"status":"ASSIGNED"})
	})
	http.HandleFunc("/api/barcodes/release", func(w http.ResponseWriter, r *http.Request) {
		var req releaseReq; if err := json.NewDecoder(r.Body).Decode(&req); err != nil { http.Error(w,"bad json",400); return }
		if _, err := pool.Exec(ctx, `DELETE FROM barcode_assignments WHERE code=$1 AND book_id=$2`, req.Code, req.BookID); err != nil { http.Error(w, err.Error(), 400); return }
		var cnt int; _ = pool.QueryRow(ctx, `SELECT COUNT(*) FROM barcode_assignments WHERE code=$1`, req.Code).Scan(&cnt)
		if cnt == 0 { _, _ = pool.Exec(ctx, `UPDATE barcodes SET status='AVAILABLE', updated_at=now() WHERE code=$1`, req.Code) }
		jsonOK(w, map[string]any{"status":"RELEASED"})
	})
	http.HandleFunc("/api/barcodes/verify", func(w http.ResponseWriter, r *http.Request) {
		var req verifyReq; if err := json.NewDecoder(r.Body).Decode(&req); err != nil { http.Error(w,"bad json",400); return }
		var minW, minH int
		if err := pool.QueryRow(ctx, `SELECT sr.min_width, sr.min_height FROM size_rules sr JOIN barcodes b ON b.size_rule_id = sr.id WHERE b.code = $1`, req.Code).Scan(&minW,&minH); err != nil { http.Error(w, errors.New("size rule not found").Error(), 404); return }
		jsonOK(w, map[string]any{"ok": req.Width>=minW && req.Height>=minH, "minWidth":minW, "minHeight":minH})
	})
	log.Println("barcodes service on :"+port); log.Fatal(http.ListenAndServe(":"+port, nil))
}
