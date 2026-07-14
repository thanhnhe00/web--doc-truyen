import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Mail, Calendar, Shield, Save, Camera, Image } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './Profile.css';

const Profile = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [email, setEmail] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');

  useEffect(() => {
    if (!isAuthenticated) { navigate('/login'); return; }
    api.get('/users/me')
      .then(res => {
        setProfile(res.data);
        setEmail(res.data.email);
        setBirthDate(res.data.birthDate);
        setAvatarUrl(res.data.avatarUrl || '');
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [isAuthenticated, navigate]);

  const handleAvatarUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setMessage('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setAvatarUrl(res.data.url);
      setMessage('Tải ảnh thành công!');
    } catch {
      setMessage('Tải ảnh thất bại.');
    } finally {
      setUploading(false);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      await api.put('/users/me', { email, birthDate, avatarUrl });
      setMessage('Cập nhật thành công!');
    } catch {
      setMessage('Cập nhật thất bại.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;
  if (!profile) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Không tìm thấy thông tin.</div>;

  return (
    <div className="container profile-page">
      <div className="card profile-card">
        <h2 className="section-title"><User size={20} /> Thông tin cá nhân</h2>
        {message && <div className={message.includes('thành công') ? 'auth-success' : 'auth-error'}>{message}</div>}

        <div className="avatar-section">
          <div className="avatar-preview">
            {avatarUrl ? (
              <img src={avatarUrl} alt="Avatar" className="avatar-image" />
            ) : (
              <div className="avatar-placeholder"><User size={48} /></div>
            )}
          </div>
          <button type="button" className="btn btn-outline btn-sm" onClick={() => fileInputRef.current?.click()} disabled={uploading}>
            <Camera size={14} /> {uploading ? 'Đang tải...' : 'Đổi ảnh'}
          </button>
          <input ref={fileInputRef} type="file" accept="image/*" onChange={handleAvatarUpload} style={{ display: 'none' }} />
        </div>

        <form className="profile-form" onSubmit={handleUpdate}>
          <div className="form-group">
            <label><User size={16} /> Tên đăng nhập</label>
            <input type="text" value={profile.username} disabled />
          </div>
          <div className="form-group">
            <label><Mail size={16} /> Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} />
          </div>
          <div className="form-group">
            <label><Calendar size={16} /> Ngày sinh</label>
            <input type="date" value={birthDate} onChange={e => setBirthDate(e.target.value)} />
          </div>
          <div className="form-group">
            <label><Shield size={16} /> Vai trò</label>
            <input type="text" value={profile.role} disabled />
          </div>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            <Save size={16} /> {saving ? 'Đang lưu...' : 'Cập nhật'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Profile;
