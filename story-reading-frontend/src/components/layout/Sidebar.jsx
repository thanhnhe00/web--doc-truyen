import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Trophy, Eye } from 'lucide-react';
import api from '../../utils/api';
import './Sidebar.css';

const Sidebar = () => {
  const [activeTab, setActiveTab] = useState('month');
  const [topStories, setTopStories] = useState([]);

  useEffect(() => {
    const days = activeTab === 'month' ? 30 : activeTab === 'week' ? 7 : 1;
    api.get(`/stories/trending?days=${days}&limit=5`)
      .then(res => {
        setTopStories((res.data || []).map((s, i) => ({
          id: s.storyId || s.id,
          title: s.title,
          views: s.viewCount || 0,
          rank: i + 1
        })));
      })
      .catch(() => setTopStories([]));
  }, [activeTab]);

  return (
    <aside className="sidebar">
      <div className="card ranking-card">
        <h3 className="section-title">
          <Trophy size={20} />
          Bảng xếp hạng
        </h3>

        <div className="ranking-tabs">
          <button className={`tab-btn ${activeTab === 'month' ? 'active' : ''}`} onClick={() => setActiveTab('month')}>Tháng</button>
          <button className={`tab-btn ${activeTab === 'week' ? 'active' : ''}`} onClick={() => setActiveTab('week')}>Tuần</button>
          <button className={`tab-btn ${activeTab === 'day' ? 'active' : ''}`} onClick={() => setActiveTab('day')}>Ngày</button>
        </div>

        <div className="ranking-list">
          {topStories.length === 0 ? (
            <div style={{ padding: '16px', textAlign: 'center', color: '#999', fontSize: '0.85rem' }}>Chưa có dữ liệu xếp hạng.</div>
          ) : (
            topStories.map((story) => (
              <div key={story.id} className="ranking-item">
                <div className={`rank-number rank-${story.rank}`}>
                  {story.rank < 10 ? `0${story.rank}` : story.rank}
                </div>
                <div className="ranking-info">
                  <Link to={`/story/${story.id}`} className="ranking-title">{story.title}</Link>
                  <div className="ranking-meta">
                    <span className="ranking-views">
                      <Eye size={14} /> {story.views}
                    </span>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
