package testutil

import (
	"fmt"
	"io"
	"net/http"
	"time"
)

func HTTPGetOK(url string, timeout time.Duration) error {
	client := &http.Client{Timeout: 10 * time.Second}
	end := time.Now().Add(timeout)
	for {
		resp, err := client.Get(url)
		if err == nil {
			io.Copy(io.Discard, resp.Body)
			resp.Body.Close()
			if resp.StatusCode >= 200 && resp.StatusCode < 300 {
				return nil
			}
			return fmt.Errorf("GET %s: status %d", url, resp.StatusCode)
		}
		if time.Now().After(end) {
			return fmt.Errorf("GET %s failed after timeout: %v", url, err)
		}
		time.Sleep(1 * time.Second)
	}
}
