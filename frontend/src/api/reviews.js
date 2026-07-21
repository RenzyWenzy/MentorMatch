import apiClient from './client';

export async function submitReview(bookingId, payload) {
  const { data } = await apiClient.post(`/bookings/${bookingId}/review`, payload);
  return data;
}

export async function fetchTutorReviews(tutorProfileId) {
  const { data } = await apiClient.get(`/tutor-profiles/${tutorProfileId}/reviews`);
  return data;
}