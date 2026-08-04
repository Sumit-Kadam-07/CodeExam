import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import apiClient from '@/lib/interceptors';

export type Role = 'ADMIN' | 'STUDENT';

export interface AuthUser {
  username: string;
  fullName: string;
  role: Role;
  profilePicUrl: string | null;
}

interface AuthContextValue {
  user: AuthUser | null;
  login: (user: AuthUser) => void;
  logout: () => void;
  updateUser: (updates: Partial<AuthUser>) => void;
  initialized?: boolean;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  login: () => {},
  logout: () => {},
  updateUser: () => {},
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    async function init() {
      const token = localStorage.getItem('token');
      if (!token) {
        localStorage.removeItem('user');
        setUser(null);
        setInitialized(true);
        return;
      }

      try {
        const resp = await apiClient.get('/auth/me');
        const body = resp.data;
        const data = body.data ?? body;
        const roleRaw: string = data.role ?? '';
        const role = roleRaw && roleRaw.startsWith('ROLE_') ? roleRaw.replace(/^ROLE_/, '') : roleRaw;
        const u: AuthUser = {
          username: data.username,
          fullName: data.fullName,
          role: role as any,
          profilePicUrl: data.profilePicUrl ?? null,
        };
        setUser(u);
        localStorage.setItem('user', JSON.stringify(u));
      } catch (e) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
      } finally {
        setInitialized(true);
      }
    }

    init();
  }, []);

  function login(nextUser: AuthUser) {
    setUser(nextUser);
  }

  function updateUser(updates: Partial<AuthUser>) {
    setUser(prev => {
      if (!prev) return prev;
      const updated = { ...prev, ...updates };
      localStorage.setItem('user', JSON.stringify(updated));
      return updated;
    });
  }

  function logout() {
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // Clear any exam-related data
    const keysToRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && (key.startsWith('exam_') && (key.endsWith('_codes') || key.endsWith('_langs') || key.endsWith('_violations')))) {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach(k => localStorage.removeItem(k));
  }

  return <AuthContext.Provider value={{ user, login, logout, updateUser, initialized }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
