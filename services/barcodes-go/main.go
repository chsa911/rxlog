package main

import (
	"context"
	"encoding/json"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/jackc/pgx/v5/pgxpool"
)

type nextReq struct {
	Width  int `json:"width"`
	Height int `json:"height"`
}

type codeReq struct {
	Code string `json:"code"`
}

func jsonOK(w http.ResponseWriter, v any) {
	w.Header().Set("content-type", "application/json")
	_ = json.NewEncoder(w).Encode(v)
}

// e=down, l=left, o=up
func positionFromHeight(h int) string {
	if h <= 200 {
		return "down" // egk
	}
	if h <= 260 {
		return "left" // lgk
	}
	return "up" // ogk
}

func main() {
	port := os.Getenv("SERVICE_PORT")
	if port == "" {
		port = "8082"
	}
	dburl := os.Getenv("DATABASE_URL")
	if dburl == "" {
		log.Fatal("DATABASE_URL required (e.g. postgresql://rxlog:rxlog@postgres:5432/rxlog?sslmode=disable)")
	}

	ctx := context.Background()
	pool, err := pgxpool.New(ctx, dburl)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}
	defer pool.Close()

	// --- health ---
	http.HandleFunc("/health", func(w http.ResponseWriter, _ *http.Request) { _, _ = w.Write([]byte("ok")) })

	// ---------- ASSIGN (atomic pick + mark unavailable; single source of truth) ----------
	assignHandler := func(w http.ResponseWriter, r *http.Request) {
		var in nextReq
		if err := json.NewDecoder(r.Body).Decode(&in); err != nil || in.Width <= 0 || in.Height <= 0 {
			http.Error(w, "bad json", http.StatusBadRequest)
			return
		}

		// 1) find matching size rule (inclusive bounds)
		var rid int
		var color, pDown, pLeft, pUp *string
		err := pool.QueryRow(ctx, `
			SELECT id, COALESCE(color,''), prefix_down, prefix_left, prefix_up
			  FROM size_rules
			 WHERE $1 >= COALESCE(min_width,0)  AND $1 <= COALESCE(max_width,2147483647)
			   AND $2 >= COALESCE(min_height,0) AND $2 <= COALESCE(max_height,2147483647)
			 ORDER BY (COALESCE(max_width,2147483647)-min_width) ASC, min_width DESC
			 LIMIT 1
		`, in.Width, in.Height).Scan(&rid, &color, &pDown, &pLeft, &pUp)
		if err != nil {
			http.Error(w, "no size rule for width/height", http.StatusNotFound)
			return
		}

		// 2) choose prefix by position (down|left|up)
		pos := positionFromHeight(in.Height)
		var pfx string
		switch pos {
		case "down":
			if pDown != nil {
				pfx = *pDown
			}
		case "left":
			if pLeft != nil {
				pfx = *pLeft
			}
		case "up":
			if pUp != nil {
				pfx = *pUp
			}
		}
		if strings.TrimSpace(pfx) == "" {
			http.Error(w, "rule has no prefix for position", http.StatusBadRequest)
			return
		}

		// 3) atomically assign the next AVAILABLE code (flip to FALSE in the same statement)
		var code string
		err = pool.QueryRow(ctx, `
			WITH picked AS (
				SELECT code
				  FROM barcodes
				 WHERE is_available = TRUE
				   AND size_rule_id  = $1
				   AND lower(code) LIKE $2   -- prefix%
				 ORDER BY code ASC
				 FOR UPDATE SKIP LOCKED
				 LIMIT 1
			)
			UPDATE barcodes b
			   SET is_available = FALSE, updated_at = now()
			  FROM picked
			 WHERE b.code = picked.code
			RETURNING b.code
		`, rid, strings.ToLower(pfx)+"%").Scan(&code)
		if err != nil {
			http.Error(w, "no available barcode", http.StatusNotFound)
			return
		}

		jsonOK(w, map[string]any{
			"code":        code,
			"isAvailable": false, // already consumed
			"position":    pos,   // "down" | "left" | "up"
			"sizeRuleId":  rid,
			"prefix":      pfx,
			"color":       func() string { if color != nil { return *color }; return "" }(),
		})
	}
	// register both (gateway may StripPrefix=1)
	http.HandleFunc("/api/barcodes/assignForDimensions", assignHandler)
	http.HandleFunc("/barcodes/assignForDimensions", assignHandler)

	// ---------- COMMIT (deprecated; no-op for compatibility) ----------
	http.HandleFunc("/api/barcodes/commit", func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	})
	http.HandleFunc("/barcodes/commit", func(w http.ResponseWriter, _ *http.Request) {
		w.WriteHeader(http.StatusNoContent)
	})

	// ---------- RELEASE (optional admin/manual: make code available again) ----------
	releaseHandler := func(w http.ResponseWriter, r *http.Request) {
		var in codeReq
		if err := json.NewDecoder(r.Body).Decode(&in); err != nil || strings.TrimSpace(in.Code) == "" {
			http.Error(w, "bad json", http.StatusBadRequest)
			return
		}
		if _, err := pool.Exec(ctx, `
			UPDATE barcodes
			   SET is_available = TRUE, updated_at = now()
			 WHERE code = $1
		`, in.Code); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		jsonOK(w, map[string]any{"code": in.Code, "isAvailable": true})
	}
	http.HandleFunc("/api/barcodes/release", releaseHandler)
	http.HandleFunc("/barcodes/release", releaseHandler)

	// ---------- VERIFY (optional; checks rule bounds for a given code) ----------
	http.HandleFunc("/api/barcodes/verify", func(w http.ResponseWriter, r *http.Request) {
		var in struct {
			Code   string `json:"code"`
			Width  int    `json:"width"`
			Height int    `json:"height"`
		}
		if err := json.NewDecoder(r.Body).Decode(&in); err != nil || strings.TrimSpace(in.Code) == "" {
			http.Error(w, "bad json", http.StatusBadRequest)
			return
		}
		var minW, maxW, minH, maxH int
		err := pool.QueryRow(ctx, `
			SELECT COALESCE(sr.min_width,0),
			       COALESCE(sr.max_width,2147483647),
			       COALESCE(sr.min_height,0),
			       COALESCE(sr.max_height,2147483647)
			  FROM barcodes b
			  JOIN size_rules sr ON sr.id = b.size_rule_id
			 WHERE b.code = $1
			 LIMIT 1
		`, in.Code).Scan(&minW, &maxW, &minH, &maxH)
		if err != nil {
			http.Error(w, "size rule not found", http.StatusNotFound)
			return
		}
		ok := in.Width >= minW && in.Width <= maxW && in.Height >= minH && in.Height <= maxH
		jsonOK(w, map[string]any{
			"ok":        ok,
			"minWidth":  minW,
			"maxWidth":  maxW,
			"minHeight": minH,
			"maxHeight": maxH,
		})
	})

	log.Println("barcodes service on :" + port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}