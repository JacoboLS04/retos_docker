package testutil

import (
	"context"
	"fmt"
	"os"
	"time"

	"github.com/jackc/pgx/v5/pgxpool"
)

type PGConfig struct {
	Host string
	Port string
	DB   string
	User string
	Pass string
}

func DefaultPGConfig() PGConfig {
	return PGConfig{
		Host: getenv("PGHOST", "notifications-db"),
		Port: getenv("PGPORT", "5432"),
		DB:   getenv("PGDATABASE", "notifications_db"),
		User: getenv("PGUSER", "retos_user"),
		Pass: getenv("PGPASSWORD", "retos_pass"),
	}
}

func getenv(k, def string) string {
	v := os.Getenv(k)
	if v == "" {
		return def
	}
	return v
}

func NewPGPool(cfg PGConfig) (*pgxpool.Pool, error) {
	url := fmt.Sprintf("postgres://%s:%s@%s:%s/%s", cfg.User, cfg.Pass, cfg.Host, cfg.Port, cfg.DB)
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	return pgxpool.New(ctx, url)
}

// WaitForRow waits until exists(select 1 from table where column=value) succeeds or timeout.
func WaitForRow(pool *pgxpool.Pool, timeout time.Duration, table, column, value string) error {
	ctx := context.Background()
	end := time.Now().Add(timeout)
	for {
		var one int
		err := pool.QueryRow(ctx, fmt.Sprintf("select 1 from %s where %s=$1 limit 1", table, column), value).Scan(&one)
		if err == nil {
			return nil
		}
		if time.Now().After(end) {
			return fmt.Errorf("row not found in %s where %s=%s before timeout: %v", table, column, value, err)
		}
		time.Sleep(2 * time.Second)
	}
}
