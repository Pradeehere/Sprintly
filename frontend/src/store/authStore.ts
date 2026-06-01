import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  fullName: string;
  email: string;
  avatarUrl: string | null;
}

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  setAuth: (token: string, refreshToken: string, user: User) => void;
  setToken: (token: string, refreshToken?: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      setAuth: (token, refreshToken, user) => set({ token, refreshToken, user, isAuthenticated: true }),
      setToken: (token, refreshToken) => set((state) => ({ token, refreshToken: refreshToken || state.refreshToken })),
      logout: () => set({ token: null, refreshToken: null, user: null, isAuthenticated: false }),
    }),
    {
      name: 'sprintly-auth',
    }
  )
);
