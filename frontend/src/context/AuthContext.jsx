import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { login as apiLogin, register as apiRegister } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('ctmms_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    // Session is rehydrated synchronously from localStorage above;
    // this just marks the initial check as done.
    setInitializing(false);
  }, []);

  const persistSession = (token, userData) => {
    localStorage.setItem('ctmms_token', token);
    localStorage.setItem('ctmms_user', JSON.stringify(userData));
    setUser(userData);
  };

  const login = useCallback(async (email, password) => {
    const { token, user: userData } = await apiLogin(email, password);
    persistSession(token, userData);
    return userData;
  }, []);

  const register = useCallback(async (payload) => {
    const { token, user: userData } = await apiRegister(payload);
    persistSession(token, userData);
    return userData;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('ctmms_token');
    localStorage.removeItem('ctmms_user');
    setUser(null);
  }, []);

  const value = { user, initializing, login, register, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
