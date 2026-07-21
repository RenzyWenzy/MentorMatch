import apiClient from './client';

export async function fetchSubjects() {
  const { data } = await apiClient.get('/subjects');
  return data;
}

export async function createSubject(payload) {
  const { data } = await apiClient.post('/subjects', payload);
  return data;
}

export async function updateSubject(id, payload) {
  const { data } = await apiClient.put(`/subjects/${id}`, payload);
  return data;
}

export async function deleteSubject(id) {
  await apiClient.delete(`/subjects/${id}`);
}
