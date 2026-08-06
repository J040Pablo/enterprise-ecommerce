export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  cpf: string;
  phone: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  cpf?: string;
  phone?: string;
  enabled?: boolean;
  emailVerified?: boolean;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  type: string;
  expiresIn: number;
  user: User;
}

export interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
  type: string;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  refreshToken: string;
}
