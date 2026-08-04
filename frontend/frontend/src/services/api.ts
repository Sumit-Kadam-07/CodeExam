import apiClient from '@/lib/interceptors';

export async function get<T>(url: string): Promise<T> {
  const res = await apiClient.get(url);
  return res.data;
}

export async function post<T>(url: string, data?: unknown): Promise<T> {
  const res = await apiClient.post(url, data);
  return res.data;
}

export async function put<T>(url: string, data?: unknown): Promise<T> {
  const res = await apiClient.put(url, data);
  return res.data;
}

export async function del<T>(url: string): Promise<T> {
  const res = await apiClient.delete(url);
  return res.data;
}
