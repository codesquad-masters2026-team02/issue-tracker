import {
  createContext,
  type ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import {
  fetchMyInfo,
  refreshAccessToken,
  signIn,
  signInWithGithub,
  signOut,
  signUp,
  type LoginRequest,
  type SignupRequest,
  type UserInfoResponse,
} from './api';
import { setAccessToken } from './authToken';

interface AuthContextValue {
  user: UserInfoResponse | null;
  isBootstrapping: boolean;
  login: (body: LoginRequest) => Promise<void>;
  loginWithGithub: (code: string) => Promise<void>;
  signup: (body: SignupRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  clearSession: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserInfoResponse | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      try {
        await refreshAccessToken();
        const me = await fetchMyInfo();
        if (!cancelled) setUser(me);
      } catch {
        setAccessToken(null);
        if (!cancelled) setUser(null);
      } finally {
        if (!cancelled) setIsBootstrapping(false);
      }
    }

    bootstrap();
    return () => {
      cancelled = true;
    };
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isBootstrapping,
    login: async (body) => {
      await signIn(body);
      setUser(await fetchMyInfo());
    },
    loginWithGithub: async (code) => {
      await signInWithGithub(code);
      setUser(await fetchMyInfo());
    },
    signup: async (body) => {
      await signUp(body);
    },
    logout: async () => {
      try {
        await signOut();
      } catch {
        // The local session should still end if the server-side logout request fails.
      } finally {
        setAccessToken(null);
        setUser(null);
      }
    },
    refreshUser: async () => {
      setUser(await fetchMyInfo());
    },
    clearSession: () => {
      setAccessToken(null);
      setUser(null);
    },
  }), [isBootstrapping, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return value;
}
