import { post } from './api';
import { ApiResponse, UserDTO } from '@/types';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: UserDTO;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const res = await post<ApiResponse<LoginResponse>>('/auth/login', { username, password });
  if (!res.success || !res.data) throw new Error(res.message || 'Login failed');
  return res.data;
}

export async function register(userData: {
  firstName: string;
  middleName?: string;
  lastName: string;
  email: string;
  mobileNumber?: string;
  password: string;
  confirmPassword: string;
  profilePhoto?: string | null;
}): Promise<void> {
  const res = await post<ApiResponse<string>>('/auth/register', userData);
  if (!res.success) throw new Error(res.message || 'Registration failed');
}

