import http from 'k6/http';
  import { check, sleep } from 'k6';

  const BASE_URL = 'https://d2fr8s1xlew5bs.cloudfront.net';

  const USERNAME = 'loadtester';
  const PASSWORD = '!Test1234';

  const ASSIGNEE_ID = 427;
  const LABEL_IDS = [1, 2];

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
        username: USERNAME,
        password: PASSWORD,
      }),
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );

    check(loginRes, {
      'login success': (res) => res.status === 200,
    });

    const token = loginRes.json('data.accessToken');

    if (!token) {
      throw new Error(`No access token returned: ${loginRes.status} ${loginRes.body}`);
    }

    return { token };
  }

  export default function (data) {
    const headers = {
      Authorization: `Bearer ${data.token}`,
    };

    const status = 'OPEN';
    const page = 0;

    const labelQuery = LABEL_IDS.map((id) => `labelIds=${id}`).join('&');

    const res = http.get(
      `${BASE_URL}/api/issues?status=${status}&assigneeIds=${ASSIGNEE_ID}&${labelQuery}&pageNumber=${page}`,
      { headers },
    );

    check(res, {
      'filtered issue list 200': (r) => r.status === 200,
      'filtered issue list success true': (r) => r.json('success') === true,
    });

    sleep(1);
  }
