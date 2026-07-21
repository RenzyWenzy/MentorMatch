import apiClient from './client';

export async function fetchOwnNotifications() {
  const { data } = await apiClient.get('/notifications/me');
  return data;
}

export async function fetchUnreadNotificationCount() {
  const { data } = await apiClient.get('/notifications/me/unread-count');
  return data.count;
}

export async function markNotificationAsRead(id) {
  const { data } = await apiClient.put(`/notifications/${id}/read`);
  return data;
}

export async function markAllNotificationsAsRead() {
  await apiClient.put('/notifications/read-all');
}