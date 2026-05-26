import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../lib/auth';

export function GithubCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { loginWithGithub } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get('code');
    if (!code) {
      navigate('/login', { replace: true });
      return;
    }

    loginWithGithub(code)
      .then(() => navigate('/', { replace: true }))
      .catch(() => {
        setError('GitHub 로그인에 실패했습니다.');
        setTimeout(() => navigate('/login', { replace: true }), 2000);
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (error) return <p style={{ textAlign: 'center', marginTop: '2rem' }}>{error}</p>;
  return <p style={{ textAlign: 'center', marginTop: '2rem' }}>로그인 중...</p>;
}
