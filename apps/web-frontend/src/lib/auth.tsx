import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';
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
  userKeys,
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
  const queryClient = useQueryClient();
  const [user, setUser] = useState<UserInfoResponse | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  const cacheCurrentUser = useCallback((me: UserInfoResponse) => {
    queryClient.setQueryData<UserInfoResponse[]>(userKeys.list(), (current = []) => (
      current.some((cachedUser) => cachedUser.id === me.id)
        ? current
        : [...current, me]
    ));
    queryClient.invalidateQueries({ queryKey: userKeys.all });
  }, [queryClient]);

  useEffect(() => {
    let cancelled = false;

    async function bootstrap() {
      try {
        await refreshAccessToken();
        const me = await fetchMyInfo();
        if (!cancelled) {
          setUser(me);
          cacheCurrentUser(me);
        }
      } catch {
        setAccessToken(null);
        if (!cancelled) {
          setUser(null);
          queryClient.clear();
        }
      } finally {
        if (!cancelled) setIsBootstrapping(false);
      }
    }

    bootstrap();
    return () => {
      cancelled = true;
    };
  }, [cacheCurrentUser, queryClient]);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isBootstrapping,
    login: async (body) => {
      await signIn(body);
      const me = await fetchMyInfo();
      setUser(me);
      cacheCurrentUser(me);
    },
    loginWithGithub: async (code) => {
      await signInWithGithub(code);
      const me = await fetchMyInfo();
      setUser(me);
      cacheCurrentUser(me);
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
        queryClient.clear();
      }
    },
    refreshUser: async () => {
      setUser(await fetchMyInfo());
    },
    clearSession: () => {
      setAccessToken(null);
      setUser(null);
      queryClient.clear();
    },
  }), [cacheCurrentUser, isBootstrapping, queryClient, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return value;
}
