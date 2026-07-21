import apiClient from './client';

export async function fetchUserById(id) {
  const { data } = await apiClient.get(`/users/${id}`);
  return data;
}

export async function activateUser(id) {
  const { data } = await apiClient.put(`/users/${id}/activate`);
  return data;
}

export async function deactivateUser(id) {
  const { data } = await apiClient.put(`/users/${id}/deactivate`);
  return data;
}

export async function removeUser(id) {
  await apiClient.delete(`/users/${id}`);
}