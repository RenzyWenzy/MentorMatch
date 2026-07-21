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
export async function fetchAllTutorProfiles() {
  const { data } = await apiClient.get('/tutor-profiles');
  return data;
}

/**
 * FR-004: search tutors by subject and/or a weekly time slot.
 * Pass only the filters you have — omitted/empty fields are left out of
 * the query string so the backend treats them as "no filter".
 * params: { subjectId, dayOfWeek, startTime, endTime }
 */
export async function searchTutorProfiles(params = {}) {
  const query = {};
  if (params.subjectId) query.subjectId = params.subjectId;
  if (params.dayOfWeek) query.dayOfWeek = params.dayOfWeek;
  if (params.startTime) query.startTime = params.startTime;
  if (params.endTime) query.endTime = params.endTime;

  const { data } = await apiClient.get('/tutor-profiles/search', { params: query });
  return data;
}