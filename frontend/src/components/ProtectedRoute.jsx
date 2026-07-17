import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Wraps a route so it's only reachable when logged in, and optionally
 * restricted to a set of roles (mirrors the STUDENT/MENTOR/ADMIN checks
 * enforced server-side in SecurityConfig).
 */
export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, initializing } = useAuth();

  if (initializing) return null;

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={dashboardPathForRole(user.role)} replace />;
  }

  return children;
}

export function dashboardPathForRole(role) {
  switch (role) {
    case 'ADMIN':
      return '/dashboard/admin';
    case 'MENTOR':
      return '/dashboard/mentor';
    case 'STUDENT':
    default:
      return '/dashboard/student';
  }
}
