import apiClient from './client';

export async function login(email, password) {
  const { data } = await apiClient.post('/auth/login', { email, password });
  return data; // { token, user }
}

export async function register(payload) {
  const { data } = await apiClient.post('/auth/register', payload);
  return data; // { token, user }
}

export async function fetchCurrentUser() {
  const { data } = await apiClient.get('/users/me');
  return data;
}

export async function fetchAllUsers() {
  const { data } = await apiClient.get('/users');
  return data;
}
