import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './Auth.css';

const Login = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { loginUser } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await api.post('/auth/login', { usernameOrEmail, password });
      if (res.data.token) {
        loginUser(res.data.token, { username: res.data.username, role: res.data.role });
        const role = res.data.role;
        if (role === 'CREATOR') {
          navigate('/creator');
        } else if (role === 'ADMIN') {
          navigate('/admin');
        } else {
          navigate('/');
        }
      }
    } catch (err) {
      console.error(err);
      const msg = err.response?.data?.message || err.response?.data;
      setError(typeof msg === 'string' ? msg : 'Tài khoản hoặc mật khẩu không chính xác');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container auth-page">
      <div className="card auth-card">
        <h2 className="section-title" style={{ justifyContent: 'center' }}>Đăng Nhập</h2>
        {error && <div className="auth-error">{error}</div>}
        <form className="auth-form" onSubmit={handleLogin}>
          <div className="form-group">
            <label>Tên đăng nhập hoặc Email</label>
            <input
              type="text"
              placeholder="Nhập tên đăng nhập hoặc email"
              value={usernameOrEmail}
              onChange={e => setUsernameOrEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Mật khẩu</label>
            <input 
              type="password" 
              placeholder="Nhập mật khẩu" 
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Đăng Nhập'}
          </button>
        </form>
        <div className="auth-links">
          <p>Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link></p>
        </div>
      </div>
    </div>
  );
};

export default Login;
