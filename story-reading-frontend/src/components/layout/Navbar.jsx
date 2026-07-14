import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Home, Flame, Clock, Bookmark, List, ChevronDown, FolderOpen } from 'lucide-react';
import api from '../../utils/api';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const [categories, setCategories] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    api.get('/categories').then(res => setCategories(res.data)).catch(() => {});
  }, []);

  return (
    <nav className="navbar">
      <div className="container">
        <ul className="nav-list">
          <li>
            <NavLink to="/" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
              <Home size={18} /> Trang chủ
            </NavLink>
          </li>
          <li>
            <NavLink to="/hot" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
              <Flame size={18} /> Hot
            </NavLink>
          </li>
          <li>
            <NavLink to="/collections" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
              <FolderOpen size={18} /> Bộ sưu tập
            </NavLink>
          </li>
          {isAuthenticated && (
            <>
              <li>
                <NavLink to="/history" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
                  <Clock size={18} /> Lịch sử
                </NavLink>
              </li>
              <li>
                <NavLink to="/following" className={({isActive}) => isActive ? "nav-link active" : "nav-link"}>
                  <Bookmark size={18} /> Theo dõi
                </NavLink>
              </li>
            </>
          )}
          <li
            className="nav-dropdown"
            onMouseEnter={() => setShowDropdown(true)}
            onMouseLeave={() => setShowDropdown(false)}
          >
            <div className="nav-link">
              <List size={18} /> Thể loại <ChevronDown size={14} />
            </div>
            {showDropdown && (
              <div className="dropdown-menu">
                {categories.map(cat => (
                  <div
                    key={cat.categoryId}
                    className="dropdown-item"
                    onClick={() => {
                      navigate(`/category/${cat.categoryId}`);
                      setShowDropdown(false);
                    }}
                  >
                    {cat.name}
                  </div>
                ))}
              </div>
            )}
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
