import { useState } from 'react';
import { Link, useLocation } from 'wouter';
import { useAuth } from '@/context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [navOpen, setNavOpen] = useState(false);
  const [, navigate] = useLocation();

  function handleLogout() {
    logout();
    setDropdownOpen(false);
    navigate('/');
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark" style={{ background: '#181825', borderBottom: '1px solid #313244' }}>
      <style>{`
        .navbar-profile-pic { width:30px; height:30px; border-radius:50%; object-fit:cover; marginRight:8px; border:1px solid #45475a; }
        .navbar-dark .nav-link { color: #a6adc8 !important; }
        .navbar-dark .nav-link:hover { color: #cdd6f4 !important; }
        .navbar-brand { color: #89b4fa !important; font-weight: 700; }
        .navbar-dark .dropdown-menu { background:#1e1e2e; border-color:#313244; }
        .navbar-dark .dropdown-item { color:#cdd6f4; }
        .navbar-dark .dropdown-item:hover { background:#313244; color:#cdd6f4; }
        .dropdown-divider { border-color:#313244; }
      `}</style>
      <div className="container-fluid">
        <Link href="/" className="navbar-brand">
          <i className="bi bi-code-slash me-1"></i>CodeExam
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          onClick={() => setNavOpen(!navOpen)}
          aria-expanded={navOpen}
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className={`collapse navbar-collapse${navOpen ? ' show' : ''}`}>
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            {user?.role === 'ADMIN' && (
              <>
                <li className="nav-item">
                  <Link href="/admin/dashboard" className="nav-link">
                    <i className="bi bi-grid me-1"></i>Dashboard
                  </Link>
                </li>
                <li className="nav-item">
                  <Link href="/admin/manage-exams" className="nav-link">
                    <i className="bi bi-journal-text me-1"></i>Exams
                  </Link>
                </li>
                <li className="nav-item">
                  <Link href="/admin/students" className="nav-link">
                    <i className="bi bi-people me-1"></i>Students
                  </Link>
                </li>
                <li className="nav-item">
                  <Link href="/admin/leaderboard" className="nav-link">
                    <i className="bi bi-trophy me-1"></i>Leaderboard
                  </Link>
                </li>
              </>
            )}
            {user?.role === 'STUDENT' && (
              <>
                <li className="nav-item">
                  <Link href="/student/dashboard" className="nav-link">
                    <i className="bi bi-grid me-1"></i>Dashboard
                  </Link>
                </li>
                <li className="nav-item">
                  <Link href="/student/my-results" className="nav-link">
                    <i className="bi bi-bar-chart me-1"></i>My Results
                  </Link>
                </li>
              </>
            )}
          </ul>
          <ul className="navbar-nav ms-auto">
            {!user && (
              <>
                <li className="nav-item">
                  <Link href="/login" className="nav-link">Login</Link>
                </li>
                <li className="nav-item">
                  <Link href="/register" className="nav-link">Register</Link>
                </li>
              </>
            )}
            {user && (
              <li className="nav-item dropdown">
                <a
                  className="nav-link dropdown-toggle"
                  href="#"
                  onClick={e => { e.preventDefault(); setDropdownOpen(!dropdownOpen); }}
                >
                  {user.profilePicUrl ? (
                    <img src={user.profilePicUrl} alt="Profile" className="navbar-profile-pic" />
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="30" height="30" fill="currentColor"
                      className="bi bi-person-circle navbar-profile-pic" viewBox="0 0 16 16"
                      style={{ backgroundColor: '#313244' }}>
                      <path d="M11 6a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                      <path fillRule="evenodd" d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8zm8-7a7 7 0 0 0-5.468 11.37C3.242 11.226 4.805 10 8 10s4.757 1.225 5.468 2.37A7 7 0 0 0 8 1z"/>
                    </svg>
                  )}
                  <span style={{ color: '#cdd6f4' }}>{user.username}</span>
                </a>
                <ul className={`dropdown-menu dropdown-menu-end${dropdownOpen ? ' show' : ''}`}>
                  <li>
                    <Link href="/student/profile" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                      <i className="bi bi-person me-2"></i>My Profile
                    </Link>
                  </li>
                  <li><hr className="dropdown-divider" /></li>
                  <li>
                    <button className="dropdown-item" onClick={handleLogout}>
                      <i className="bi bi-box-arrow-right me-2"></i>Logout
                    </button>
                  </li>
                </ul>
              </li>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
}
