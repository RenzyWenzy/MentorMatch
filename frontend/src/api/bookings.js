import apiClient from './client';

export async function createBooking(payload) {
  const { data } = await apiClient.post('/bookings', payload);
  return data;
}

export async function fetchOwnBookingsAsStudent() {
  const { data } = await apiClient.get('/bookings/me');
  return data;
}

export async function fetchOwnBookingsAsMentor() {
  const { data } = await apiClient.get('/bookings/tutor/me');
  return data;
}

export async function confirmBooking(id) {
  const { data } = await apiClient.put(`/bookings/${id}/confirm`);
  return data;
}

export async function declineBooking(id) {
  const { data } = await apiClient.put(`/bookings/${id}/decline`);
  return data;
}

export async function cancelBooking(id) {
  const { data } = await apiClient.put(`/bookings/${id}/cancel`);
  return data;
}

export async function completeBooking(id) {
  const { data } = await apiClient.put(`/bookings/${id}/complete`);
  return data;
}