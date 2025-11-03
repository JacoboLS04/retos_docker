//go:build e2e
// +build e2e

package e2e

import (
	"context"
	"encoding/json"
	"fmt"
	"math/rand"
	"testing"
	"time"

	"retos_observabilidad/system-tests/internal/testutil"
)

type userEvent struct {
	Channel string `json:"channel"`
	To      string `json:"to"`
	Subject string `json:"subject"`
	Body    string `json:"body"`
	TraceID string `json:"traceId"`
}

func Test_Rabbit_To_DB(t *testing.T) {
	// Config por defecto para tu compose
	amqpURL := getenv("AMQP_URL", "amqp://guest:guest@rabbitmq:5672")
	// En modo local publicamos directo en la cola de ms-notifications para evitar depender del orquestador
	queue := getenv("TEST_PUBLISH_QUEUE", "notification.events.queue")
	// Tabla/columna a verificar en notifications_db; por defecto consultamos JSON payload->>'traceId'
	// Ajusta con NOTIF_TABLE/NOTIF_COLUMN si tu esquema es diferente
	table := getenv("NOTIF_TABLE", "notification_logs")
	column := getenv("NOTIF_COLUMN", "payload->>'traceId'")

	// Preparar evento con TraceID único para poder buscarlo en DB
	rnd := rand.New(rand.NewSource(time.Now().UnixNano()))
	suffix := string(rune('a' + rnd.Intn(26)))
	trace := fmt.Sprintf("%s-%s", time.Now().Format("20060102T150405"), suffix)
	payload := userEvent{
		Channel: "EMAIL",
		To:      "demo@example.com",
		Subject: "Welcome",
		Body:    "Hello from E2E",
		TraceID: trace,
	}
	b, _ := json.Marshal(payload)

	if err := testutil.PublishJSON(amqpURL, queue, b); err != nil {
		t.Fatalf("no se pudo publicar en RabbitMQ: %v", err)
	}

	// Conectar a Postgres y esperar aparición del registro
	pgcfg := testutil.DefaultPGConfig()
	pool, err := testutil.NewPGPool(pgcfg)
	if err != nil {
		t.Fatalf("postgres pool: %v", err)
	}
	defer pool.Close()

	// Asegurar tabla (idempotente) para que el servicio pueda insertar
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	_, _ = pool.Exec(ctx, `
		CREATE TABLE IF NOT EXISTS notification_logs (
			id SERIAL PRIMARY KEY,
			channel TEXT,
			recipient JSONB,
			payload JSONB,
			status TEXT,
			error TEXT,
			created_at TIMESTAMPTZ DEFAULT now()
		)
	`)

	if err := testutil.WaitForRow(pool, 2*time.Minute, table, column, trace); err != nil {
		t.Fatalf("no se encontró registro en DB para trace_id=%s: %v", trace, err)
	}
}

// getenv is defined in helpers_test.go for package e2e
