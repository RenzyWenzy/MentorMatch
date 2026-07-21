import { useEffect, useRef, useState } from 'react';
import {
  fetchOwnNotifications,
  fetchUnreadNotificationCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
} from '../api/notifications';

/** Polling interval for the unread badge while the dropdown is closed. */
const POLL_MS = 30000;

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const wrapperRef = useRef(null);

  useEffect(() => {
    refreshUnreadCount();
    const interval = setInterval(refreshUnreadCount, POLL_MS);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    function handleClickOutside(e) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  async function refreshUnreadCount() {
    try {
      const count = await fetchUnreadNotificationCount();
      setUnreadCount(count);
    } catch {
      // Silently ignore — the badge just won't update this cycle.
    }
  }

  async function toggleOpen() {
    const next = !open;
    setOpen(next);
    if (next) {
      setLoading(true);
      try {
        const data = await fetchOwnNotifications();
        setNotifications(data);
      } finally {
        setLoading(false);
      }
    }
  }

  async function handleMarkRead(id) {
    try {
      await markNotificationAsRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch {
      // Leave the item as-is on failure; the user can retry.
    }
  }

  async function handleMarkAllRead() {
    try {
      await markAllNotificationsAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch {
      // No-op — badge/list simply stay unchanged if this fails.
    }
  }

  return (
    <div ref={wrapperRef} style={{ position: 'relative' }}>
      <button
        onClick={toggleOpen}
        className="btn btn-ghost"
        aria-label="Notifications"
        style={{ position: 'relative' }}
      >
        🔔
        {unreadCount > 0 && (
          <span
            className="badge"
            style={{
              position: 'absolute',
              top: -4,
              right: -4,
              background: 'crimson',
              color: '#fff',
              borderRadius: '999px',
              padding: '0 6px',
              fontSize: 11,
              lineHeight: '16px',
              minWidth: 16,
              textAlign: 'center',
            }}
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div
          style={{
            position: 'absolute',
            right: 0,
            top: '110%',
            width: 340,
            maxHeight: 420,
            overflowY: 'auto',
            background: '#fff',
            color: '#111',
            border: '1px solid #ddd',
            borderRadius: 8,
            boxShadow: '0 4px 16px rgba(0,0,0,0.15)',
            zIndex: 100,
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', borderBottom: '1px solid #eee' }}>
            <strong>Notifications</strong>
            {unreadCount > 0 && (
              <button onClick={handleMarkAllRead} className="btn" style={{ fontSize: 12, padding: '2px 8px' }}>
                Mark all read
              </button>
            )}
          </div>

          {loading ? (
            <p style={{ padding: 12, margin: 0, color: '#111' }}>Loading…</p>
          ) : notifications.length === 0 ? (
            <p style={{ padding: 12, margin: 0, color: '#555' }}>No notifications yet.</p>
          ) : (
            <ul style={{ listStyle: 'none', margin: 0, padding: 0 }}>
              {notifications.map((n) => (
                <li
                  key={n.id}
                  onClick={() => !n.read && handleMarkRead(n.id)}
                  style={{
                    padding: '10px 12px',
                    borderBottom: '1px solid #f2f2f2',
                    background: n.read ? '#fff' : '#f6f9ff',
                    cursor: n.read ? 'default' : 'pointer',
                  }}
                >
                  <p style={{ margin: 0, fontSize: 14, color: '#111' }}>{n.message}</p>
                  <span style={{ fontSize: 11, color: '#888' }}>
                    {new Date(n.createdAt).toLocaleString()}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
