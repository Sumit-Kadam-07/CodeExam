import { useEffect, useRef } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useLocation } from 'wouter';

const BASE_URL = import.meta.env.BASE_URL || '/';

type Role = 'ADMIN' | 'STUDENT';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles: Role[];
}

export default function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, initialized } = useAuth();
  const [, navigate] = useLocation();
  const rolesRef = useRef(allowedRoles);
  rolesRef.current = allowedRoles;

  useEffect(() => {
    if (!initialized) return;
    if (!user) {
      navigate(`${BASE_URL}login`);
      return;
    }
    if (!rolesRef.current.includes(user.role)) {
      navigate(BASE_URL);
    }
  }, [initialized, user, navigate]);

  if (!initialized) return null;
  if (!user) return null;
  if (!allowedRoles.includes(user.role)) return null;

  return <>{children}</>;
}
