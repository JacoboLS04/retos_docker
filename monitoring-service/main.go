package main

import (
	"bytes"
	"context"
	"encoding/json"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/gorilla/mux"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

type Service struct {
	Name string `json:"name"`
	URL  string `json:"url"`
}

type RegService struct {
	svc      Service
	lastUp   int
	lastTime time.Time
}

// lastSlashIndex returns the index of the last '/' in the path
func lastSlashIndex(path string) int {
	for i := len(path) - 1; i >= 0; i-- {
		if path[i] == '/' {
			return i
		}
	}
	return -1
}

var (
	mu         sync.RWMutex
	services   = map[string]*RegService{}
	scrapeFreq = 15 * time.Second
	filePath   = "/out/targets.json"
	// Prometheus metrics
	serviceUp = prometheus.NewGaugeVec(prometheus.GaugeOpts{
		Name: "service_up",
		Help: "Service up (1) or down (0)",
	}, []string{"instance", "name"})
	serviceLastCheck = prometheus.NewGaugeVec(prometheus.GaugeOpts{
		Name: "service_last_check_timestamp",
		Help: "Last check unix timestamp",
	}, []string{"instance", "name"})
)

func init() {
	prometheus.MustRegister(serviceUp)
	prometheus.MustRegister(serviceLastCheck)
}

func main() {
	// optional override via env
	if v := os.Getenv("SCRAPE_INTERVAL_SECONDS"); v != "" {
		if d, err := time.ParseDuration(v + "s"); err == nil {
			scrapeFreq = d
		}
	}
	if p := os.Getenv("TARGETS_FILE"); p != "" {
		filePath = p
	}

	r := mux.NewRouter()
	r.HandleFunc("/register", registerHandler).Methods("POST")
	r.HandleFunc("/services", listHandler).Methods("GET")
	r.HandleFunc("/alert", alertHandler).Methods("POST")
	r.Handle("/metrics", promhttp.Handler())

	go backgroundChecker(context.Background())

	port := os.Getenv("PORT")
	if port == "" {
		port = "8085"
	}
	log.Printf("monitoring-service: listening on :%s", port)
	if err := http.ListenAndServe(":"+port, r); err != nil {
		log.Fatal(err)
	}
}

func registerHandler(w http.ResponseWriter, r *http.Request) {
	var s Service
	if err := json.NewDecoder(r.Body).Decode(&s); err != nil {
		http.Error(w, "invalid json", http.StatusBadRequest)
		return
	}
	if s.Name == "" || s.URL == "" {
		http.Error(w, "name and url required", http.StatusBadRequest)
		return
	}
	mu.Lock()
	services[s.Name] = &RegService{svc: s, lastUp: 0, lastTime: time.Time{}}
	mu.Unlock()
	if err := writeTargetsFile(); err != nil {
		log.Printf("error writing targets: %v", err)
		http.Error(w, "failed to write targets", http.StatusInternalServerError)
		return
	}
	// Ejecutar un check inicial para este servicio
	up := 0
	client := &http.Client{Timeout: 5 * time.Second}
	resp, err := client.Get(s.URL)
	if err == nil && resp.StatusCode >= 200 && resp.StatusCode < 300 {
		up = 1
	}
	mu.Lock()
	if rs, ok := services[s.Name]; ok {
		rs.lastUp = up
		rs.lastTime = time.Now()
	}
	mu.Unlock()
	// actualizar métricas de prometheus
	serviceUp.WithLabelValues(s.URL, s.Name).Set(float64(up))
	serviceLastCheck.WithLabelValues(s.URL, s.Name).Set(float64(time.Now().Unix()))

	w.WriteHeader(http.StatusCreated)
}

func listHandler(w http.ResponseWriter, r *http.Request) {
	mu.RLock()
	defer mu.RUnlock()
	out := map[string]interface{}{}
	for k, v := range services {
		out[k] = map[string]interface{}{"url": v.svc.URL, "lastUp": v.lastUp, "lastTime": v.lastTime}
	}
	w.Header().Set("Content-Type", "application/json")
	_ = json.NewEncoder(w).Encode(out)
}

func alertHandler(w http.ResponseWriter, r *http.Request) {
	// accept Alertmanager webhook payload and forward it to notification microservice
	body, _ := ioutil.ReadAll(r.Body)
	log.Printf("received alert webhook: %s", string(body))
	go forwardToNotifications(body)
	w.WriteHeader(http.StatusOK)
}

func forwardToNotifications(body []byte) {
	// best-effort forward to ms-notifications container; if unreachable, just log
	notifURL := os.Getenv("NOTIFICATIONS_ENDPOINT")
	if notifURL == "" {
		notifURL = "http://ms-notifications:8080/alerts"
	}
	req, err := http.NewRequest("POST", notifURL, bytes.NewReader(body))
	if err != nil {
		log.Printf("forward create req err: %v", err)
		return
	}
	req.Header.Set("Content-Type", "application/json")
	client := &http.Client{Timeout: 5 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("failed forward to notifications: %v", err)
		return
	}
	defer resp.Body.Close()
	// read body to avoid leaking connections
	io.Copy(ioutil.Discard, resp.Body)
	log.Printf("forwarded alert, got status: %s", resp.Status)
}

func backgroundChecker(ctx context.Context) {
	ticker := time.NewTicker(scrapeFreq)
	defer ticker.Stop()
	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			runChecks()
		}
	}
}

func runChecks() {
	mu.RLock()
	copyList := make([]Service, 0, len(services))
	for _, v := range services {
		copyList = append(copyList, v.svc)
	}
	mu.RUnlock()

	for _, s := range copyList {
		up := 0
		client := &http.Client{Timeout: 5 * time.Second}
		resp, err := client.Get(s.URL)
		if err == nil && resp.StatusCode >= 200 && resp.StatusCode < 300 {
			up = 1
		}
		mu.Lock()
		if rs, ok := services[s.Name]; ok {
			rs.lastUp = up
			rs.lastTime = time.Now()
		}
		mu.Unlock()
		// update prometheus metrics
		serviceUp.WithLabelValues(s.URL, s.Name).Set(float64(up))
		serviceLastCheck.WithLabelValues(s.URL, s.Name).Set(float64(time.Now().Unix()))
	}
}

func writeTargetsFile() error {
	type tg struct {
		Targets []string          `json:"targets"`
		Labels  map[string]string `json:"labels,omitempty"`
	}
	mu.RLock()
	tgs := []tg{}
	for _, v := range services {
		tgs = append(tgs, tg{Targets: []string{v.svc.URL}, Labels: map[string]string{"job": "blackbox"}})
	}
	mu.RUnlock()
	b, err := json.MarshalIndent(tgs, "", "  ")
	if err != nil {
		return err
	}
	// ensure parent directory exists
	targetDir := ""
	if idx := lastSlashIndex(filePath); idx >= 0 {
		targetDir = filePath[:idx]
	}
	if targetDir != "" {
		if err := os.MkdirAll(targetDir, 0755); err != nil {
			return err
		}
	}
	return ioutil.WriteFile(filePath, b, 0644)
}
