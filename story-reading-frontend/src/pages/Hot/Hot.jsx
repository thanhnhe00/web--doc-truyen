import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Flame, Eye } from 'lucide-react';
import api from '../../utils/api';
import './Hot.css';

const Hot = () => {
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/stories/trending?limit=20')
      .then(res => setStories(res.data))
      .catch(() => setStories([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container hot-page">
      <h2 className="section-title"><Flame size={20} /> Truyện Hot</h2>
      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>
      ) : stories.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Chưa có truyện hot.</div>
      ) : (
        <div className="hot-grid">
          {stories.map(story => (
            <Link key={story.storyId || story.id} to={`/story/${story.storyId || story.id}`} className="hot-card card">
              <div className="hot-cover-wrapper">
                <img src={story.coverImage || 'https://via.placeholder.com/150'} alt={story.title} className="hot-cover" />
              </div>
              <div className="hot-info">
                <div className="hot-title">{story.title}</div>
                <div className="hot-meta">
                  <Eye size={14} /> {story.viewCount || 0} lượt xem
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Hot;
