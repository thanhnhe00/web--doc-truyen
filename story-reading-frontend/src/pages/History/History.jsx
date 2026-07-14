import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Clock } from 'lucide-react';
import api from '../../utils/api';
import './History.css';

const History = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/users/me/history')
      .then(res => setHistory(res.data.content || res.data))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="container history-page">
      <h2 className="section-title"><Clock size={20} /> Lịch sử đọc truyện</h2>
      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>
      ) : history.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Bạn chưa đọc truyện nào.</div>
      ) : (
        <div className="history-list">
          {history.map((item, idx) => (
            <Link key={idx} to={`/read/${item.storyId}/${item.chapterId}`} className="history-item card">
              <img src={item.coverImage || 'https://via.placeholder.com/80x106'} alt={item.storyTitle} className="history-cover" />
              <div className="history-info">
                <div className="history-title">{item.storyTitle}</div>
                <div className="history-chapter">{item.chapterTitle || `Chương ${item.chapterNumber}`}</div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default History;
