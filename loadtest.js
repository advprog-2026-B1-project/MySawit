import http from 'k6/http';
import { check, sleep } from 'k6';

// Non Functional Requirements (NFR)
// - Capacity: Sistem dapat menangani 50 concurrent users
// - Response Time: Response time < 500ms untuk read endpoint
// - Error Rate: API error rate harus < 1%

export const options = {
  vus: 50,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  const res = http.post(
    'http://localhost:8080/api/login',
    JSON.stringify({ email: 'admin@mysawit.com', password: 'admin123' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const cookies = res.cookies;
  return { jsessionid: cookies['JSESSIONID'] ? cookies['JSESSIONID'][0].value : '' };
}

export default function (data) {
  const params = {
    headers: { Cookie: `JSESSIONID=${data.jsessionid}` },
  };

  const kebunList = http.get('http://localhost:8080/api/kebun', params);
  check(kebunList, {
    'GET /api/kebun status 200': (r) => r.status === 200,
    'GET /api/kebun < 500ms': (r) => r.timings.duration < 500,
  });

  const kebunDetail = http.get('http://localhost:8080/api/kebun/8', params);
  check(kebunDetail, {
    'GET /api/kebun/{id} status 200': (r) => r.status === 200,
    'GET /api/kebun/{id} < 500ms': (r) => r.timings.duration < 500,
  });

  const dashboard = http.get('http://localhost:8080/api/kebun/dashboard', params);
  check(dashboard, {
    'GET /api/kebun/dashboard status 200': (r) => r.status === 200,
    'GET /api/kebun/dashboard < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
