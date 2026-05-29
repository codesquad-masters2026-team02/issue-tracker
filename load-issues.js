import http from 'k6/http';
  import { check, sleep } from 'k6';

  const BASE_URL = 'https://d2fr8s1xlew5bs.cloudfront.net';

  export const options = {
    stages: [
      { duration: '30s', target: 10 },
      { duration: '1m', target: 50 },
      { duration: '30s', target: 0 },
    ],
    thresholds: {
      http_req_failed: ['rate<0.01'],
      http_req_duration: ['p(95)<500'],
    },
  };

  export function setup() {
    const loginRes = http.post(
      `${BASE_URL}/api/users/signin`,
      JSON.stringify({
        username: 'loadtester',
        password: '!Test1234',
      }),
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );

    check(loginRes, {
      'login success': (res) => res.status === 200,
    });

    const body = loginRes.json();
    const token = body?.data?.accessToken;

    if (!token) {
      throw new Error(`No access token returned: ${loginRes.body}`);
    }

    return { token };
  }

  export default function (data) {
    const headers = {
      Authorization: `Bearer ${data.token}`,
    };

    const page = Math.floor(Math.random() * 20);
    const status = Math.random() > 0.2 ? 'OPEN' : 'CLOSED';

    const res = http.get(
      `${BASE_URL}/api/issues?status=${status}&pageNumber=${page}`,
      { headers },
    );

    check(res, {
      'issue list 200': (r) => r.status === 200,
      'issue list success true': (r) => r.json('success') === true,
    });

    sleep(1);
  }
