import { type FormEvent, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import './LoginPage.css';

function getErrorMessage(caught: unknown) {
  const maybeApiError = caught as {
    response?: {
      data?: {
        error?: {
          message?: string;
        };
      };
    };
    message?: string;
  };

  return maybeApiError.response?.data?.error?.message
    ?? maybeApiError.message
    ?? '오류가 발생했습니다.';
}

export function LoginPage() {
  const navigate = useNavigate();
  const { user, isBootstrapping, login, signup } = useAuth();
  const [mode, setMode] = useState<'signin' | 'signup'>('signin');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const canSubmit = username.trim().length > 0 && password.length > 0 && !isSubmitting;

  if (!isBootstrapping && user) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSubmit) return;

    setError(null);
    setNotice(null);
    setIsSubmitting(true);
    try {
      const body = { username: username.trim(), password };
      if (mode === 'signin') {
        await login(body);
        navigate('/', { replace: true });
      } else {
        await signup(body);
        setMode('signin');
        setPassword('');
        setNotice('회원가입이 완료되었습니다. 로그인해주세요.');
      }
    } catch (caught) {
      setError(getErrorMessage(caught));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="login-page">
      <section className="login-panel" aria-label={mode === 'signin' ? '로그인' : '회원가입'}>
        <h1 className="login-panel__logo">Issue Tracker</h1>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-field">
            <span>아이디</span>
            <input
              type="text"
              value={username}
              autoComplete="username"
              onChange={(event) => {
                setUsername(event.target.value);
                setNotice(null);
              }}
            />
          </label>

          <label className="login-field">
            <span>비밀번호</span>
            <input
              type="password"
              value={password}
              autoComplete={mode === 'signin' ? 'current-password' : 'new-password'}
              onChange={(event) => {
                setPassword(event.target.value);
                setNotice(null);
              }}
            />
          </label>

          {notice && <p className="login-form__notice">{notice}</p>}
          {error && <p className="login-form__error">{error}</p>}

          <button type="submit" className="btn btn--primary login-form__submit" disabled={!canSubmit}>
            {isSubmitting ? '처리 중...' : mode === 'signin' ? '아이디로 로그인' : '회원가입'}
          </button>
        </form>

        <button
          type="button"
          className="login-panel__switch"
          onClick={() => {
            setError(null);
            setNotice(null);
            setMode((current) => (current === 'signin' ? 'signup' : 'signin'));
          }}
        >
          {mode === 'signin' ? '회원가입' : '로그인'}
        </button>
      </section>
    </main>
  );
}
