import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { getAccessToken } from '../lib/auth';

type ProtectedRouteProps = {
  children: ReactNode;
};

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const location = useLocation();

  if (!getAccessToken()) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  return <>{children}</>;
}
