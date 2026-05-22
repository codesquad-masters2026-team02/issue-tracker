let accessToken = typeof window === 'undefined'
  ? null
  : window.sessionStorage.getItem('accessToken');

export function getAccessToken() {
  return accessToken;
}

export function setAccessToken(token: string | null) {
  accessToken = token;
  if (typeof window === 'undefined') return;

  if (token) {
    window.sessionStorage.setItem('accessToken', token);
  } else {
    window.sessionStorage.removeItem('accessToken');
  }
}
