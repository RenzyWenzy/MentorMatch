import apiClient from './client';

export async function fetchMyTutorProfile() {
  const { data } = await apiClient.get('/tutor-profiles/me');
  return data;
}

export async function saveMyTutorProfile(payload) {
  // payload: { bio, subjects: [{ subjectId, proficiencyLevel }] }
  const { data } = await apiClient.put('/tutor-profiles/me', payload);
  return data;
}
