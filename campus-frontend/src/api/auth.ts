import request from './request'

export interface LoginParams {
  phone: string
  password: string
}

export interface RegisterParams {
  phone: string
  password: string
  nickname?: string
}

export interface LoginResult {
  code: number
  data: {
    token: string
    userId: number
    role: number
    nickname: string
  }
  message: string
}

export function loginApi(params: LoginParams) {
  return request.post<any, LoginResult>('/common/login', params)
}

export function registerApi(params: RegisterParams) {
  return request.post<any, { code: number; message: string }>('/common/register', params)
}
