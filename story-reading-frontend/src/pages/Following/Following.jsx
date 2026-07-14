import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Bookmark, Eye } from 'lucide-react';
import api from '../../utils/api';
import './Following.css';

const Following = () => {
  const [following, setFollowing] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/users/me/following')
      .then(res => setFollowing(res.data))
      .catch(() => setFollowing([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container following-page">
      <h2 className="section-title"><Bookmark size={20} /> Truyện đang theo dõi</h2>
      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>
      ) : following.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Bạn chưa theo dõi truyện nào.</div>
      ) : (
        <div className="following-grid">
          {following.map(item => (
            <Link key={item.storyId} to={`/story/${item.storyId}`} className="following-card card">
              <div className="following-cover-wrapper">
                <img src={item.coverImage || 'https://via.placeholder.com/150'} alt={item.title} className="following-cover" />
              </div>
              <div className="following-info">
                <div className="following-title">{item.title}</div>
                <div className="following-meta">{item.contentType}</div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Following;
