import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute, { dashboardPathForRole } from './components/ProtectedRoute';
import Topbar from './components/Topbar';
import Login from './pages/Login';
import Register from './pages/Register';
import StudentDashboard from './pages/StudentDashboard';
import MentorDashboard from './pages/MentorDashboard';
import MentorProfileEdit from './pages/MentorProfileEdit';
import AdminDashboard from './pages/AdminDashboard';

function Shell({ children }) {
  return (
    <div className="app-shell">
      <Topbar />
      {children}
    </div>
  );
}

function RootRedirect() {
  const { user } = useAuth();
  return <Navigate to={user ? dashboardPathForRole(user.role) : '/login'} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Shell>
          <Routes>
            <Route path="/" element={<RootRedirect />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route
              path="/dashboard/student"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dashboard/mentor"
              element={
                <ProtectedRoute allowedRoles={['MENTOR']}>
                  <MentorDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dashboard/mentor/profile"
              element={
                <ProtectedRoute allowedRoles={['MENTOR']}>
                  <MentorProfileEdit />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dashboard/admin"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Shell>
      </BrowserRouter>
    </AuthProvider>
  );
}
