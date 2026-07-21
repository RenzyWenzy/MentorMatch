import apiClient from './client';

export async function fetchAvailabilityForTutor(tutorProfileId) {
  const { data } = await apiClient.get(`/availability/tutor-profile/${tutorProfileId}`);
  return data;
}

export async function replaceMyAvailability(slots) {
  // slots: [{ dayOfWeek, startTime, endTime }]
  const { data } = await apiClient.put('/availability/me', { slots });
  return data;
}