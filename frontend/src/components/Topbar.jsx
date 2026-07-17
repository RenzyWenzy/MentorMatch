import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Topbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="topbar">
      <div className="topbar-brand">CTMMS</div>
      {user && (
        <div className="topbar-user">
          <span>{user.fullName}</span>
          <span className="badge">{user.role}</span>
          <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
        </div>
      )}
    </header>
  );
}
