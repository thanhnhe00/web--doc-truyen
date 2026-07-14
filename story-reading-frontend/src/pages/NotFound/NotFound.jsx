import React from 'react';
import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';
import './NotFound.css';

const NotFound = () => {
  return (
    <div className="not-found-page">
      <h1>404</h1>
      <p>Không tìm thấy trang bạn yêu cầu.</p>
      <Link to="/" className="btn btn-primary">
        <Home size={18} /> Về trang chủ
      </Link>
    </div>
  );
};

export default NotFound;
