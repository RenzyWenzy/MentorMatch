import apiClient from './client';

/** FR-013: startDate/endDate as 'YYYY-MM-DD' strings. */
export async function fetchAdminReport(startDate, endDate) {
  const { data } = await apiClient.get('/admin/reports', {
    params: { startDate, endDate },
  });
  return data;
}