import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../lib/auth';

export function ProtectedRoute() {
  const { user, isBootstrapping } = useAuth();

  if (isBootstrapping) {
    return <p className="app-route-status">불러오는 중...</p>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
