import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { BookOpen, PenTool } from 'lucide-react';
import api from '../../utils/api';
import './Auth.css';

const Register = () => {
  const [role, setRole] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await api.post('/auth/register', { username, email, password, birthDate, role });
      setSuccess('Đăng ký thành công! Đang chuyển hướng đến trang đăng nhập...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      console.error(err);
      const msg = err.response?.data?.message || err.response?.data;
      setError(typeof msg === 'string' ? msg : 'Đăng ký thất bại. Tên đăng nhập hoặc email có thể đã tồn tại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container auth-page">
      <div className="card auth-card">
        <h2 className="section-title" style={{ justifyContent: 'center' }}>Đăng Ký</h2>
        {error && <div className="auth-error">{error}</div>}
        {success && <div className="auth-success">{success}</div>}

        {/* Bước 1: Chọn vai trò */}
        {!role && (
          <div className="role-select">
            <p className="role-select-label">Bạn muốn đăng ký với tư cách?</p>
            <div className="role-options">
              <button className="role-card" type="button" onClick={() => setRole('READER')}>
                <BookOpen size={36} className="role-icon" />
                <div className="role-title">Độc giả</div>
                <div className="role-desc">Đọc truyện, theo dõi tác giả, bình luận</div>
              </button>
              <button className="role-card" type="button" onClick={() => setRole('CREATOR')}>
                <PenTool size={36} className="role-icon" />
                <div className="role-title">Tác giả</div>
                <div className="role-desc">Viết truyện, quản lý tác phẩm của bạn</div>
              </button>
            </div>
          </div>
        )}

        {/* Bước 2: Form đăng ký */}
        {role && (
          <>
            <div className="role-badge">
              {role === 'READER' ? <BookOpen size={14} /> : <PenTool size={14} />}
              <span>{role === 'READER' ? 'Độc giả' : 'Tác giả'}</span>
              <button type="button" className="role-change-btn" onClick={() => setRole('')}>đổi</button>
            </div>
            <form className="auth-form" onSubmit={handleRegister}>
              <div className="form-group">
                <label>Tên đăng nhập</label>
                <input
                  type="text"
                  placeholder="Nhập tên đăng nhập"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  placeholder="Nhập email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
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
              <div className="form-group">
                <label>Ngày sinh</label>
                <input
                  type="date"
                  value={birthDate}
                  onChange={e => setBirthDate(e.target.value)}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
                {loading ? 'Đang xử lý...' : 'Đăng Ký'}
              </button>
            </form>
          </>
        )}

        <div className="auth-links">
          <p>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>
        </div>
      </div>
    </div>
  );
};

export default Register;
