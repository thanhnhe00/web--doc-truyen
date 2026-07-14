import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, User, LogOut, BookOpen, Shield, PenTool, Eye } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import NotificationBell from '../NotificationBell/NotificationBell';
import './Header.css';

const Header = () => {
  const { user, isAuthenticated, logout, isCreator, isAdmin } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const getRoleBadge = () => {
    if (isAdmin) return { icon: <Shield size={12} />, label: 'Admin', className: 'role-admin' };
    if (isCreator) return { icon: <PenTool size={12} />, label: 'Tác giả', className: 'role-creator' };
    return { icon: <Eye size={12} />, label: 'Độc giả', className: 'role-reader' };
  };

  return (
    <header className="header">
      <div className="container header-container">
        <Link to="/" className="logo">
          <span className="logo-text">NetTruyen</span>
        </Link>

        <form className="search-bar" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Tìm truyện..."
            className="search-input"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <button className="search-btn" type="submit">
            <Search size={20} />
          </button>
        </form>

        <div className="user-actions">
          {isAuthenticated ? (
            <>
              <NotificationBell />

              <Link to="/profile" className="user-info">
                <User size={18} />
                <span>{user?.username}</span>
                {(() => {
                  const badge = getRoleBadge();
                  return (
                    <span className={`role-badge ${badge.className}`}>
                      {badge.icon} {badge.label}
                    </span>
                  );
                })()}
              </Link>

              {isAdmin && (
                <Link to="/admin" className="btn btn-outline admin-link">
                  <Shield size={16} /> Quản trị
                </Link>
              )}

              {isCreator && (
                <Link to="/creator" className="btn btn-primary creator-link">
                  <PenTool size={16} /> Viết truyện
                </Link>
              )}

              <button className="btn btn-outline logout-btn" onClick={handleLogout}>
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline login-btn">Đăng nhập</Link>
              <Link to="/register" className="btn btn-primary register-btn">Đăng ký</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
