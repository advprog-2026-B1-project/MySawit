import http from 'k6/http';
import { check, sleep } from 'k6';

// Non Functional Requirements (NFR)
// - Capacity: Sistem dapat menangani 50 concurrent users
// - Response Time: Response time < 500ms untuk read endpoint
// - Error Rate: API error rate harus < 1%

export const options = {
  vus: 50, // 50 concurrent virtual users
  duration: '30s', // Run for 30 seconds
  thresholds: {
    // Assert that 95% of requests complete within 500ms
    http_req_duration: ['p(95)<500'],
    // Assert that the error rate is less than 1%
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  // Hit a read endpoint to test the NFR Response Time < 500ms
  // Hit a read endpoint to test the NFR Response Time < 500ms
  // Native execution against localhost
  const res = http.get('http://localhost:8080/api/kebun');
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  // Short sleep to simulate real user wait time between requests
  sleep(1);
}
