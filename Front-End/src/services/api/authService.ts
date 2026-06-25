import { getApiClient } from './client';
import type { LoginRequest, RegisterRequest, LoginResponse, PasswordResetRequest, User } from '@/types/auth';

const client = getApiClient();

export interface SocialLoginRequest {
  provider: 'google' | 'facebook';
  token: string;
}

export const authService = {
  async login(request: LoginRequest): Promise<LoginResponse> {
    const { data } = await client.post<LoginResponse>('/auth/login', request);
    return data;
  },

  async register(request: RegisterRequest): Promise<LoginResponse> {
    const { data } = await client.post<LoginResponse>('/auth/register', request);
    return data;
  },

  async resetPassword(request: PasswordResetRequest): Promise<void> {
    await client.post('/auth/reset-password', request);
  },

  async refreshToken(): Promise<LoginResponse> {
    const { data } = await client.post<LoginResponse>('/auth/refresh-token');
    return data;
  },

  async socialLogin(request: SocialLoginRequest): Promise<LoginResponse> {
    const { data } = await client.post<LoginResponse>('/auth/social-login', request);
    return data;
  },
};
