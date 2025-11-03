package testutil

import (
	"time"
)

// Retry calls fn until it returns nil or the timeout expires.
// It waits 'interval' between attempts and applies a small backoff.
func Retry(timeout, interval time.Duration, fn func() error) error {
	deadline := time.Now().Add(timeout)
	cur := interval
	for {
		if err := fn(); err == nil {
			return nil
		}
		if time.Now().After(deadline) {
			// Call one more time to return the last error
			return fn()
		}
		time.Sleep(cur)
		if cur < 5*time.Second {
			cur += 200 * time.Millisecond
		}
	}
}
