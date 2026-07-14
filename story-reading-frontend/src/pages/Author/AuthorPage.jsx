import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { User, Eye, BookOpen } from 'lucide-react';
import api from '../../utils/api';
import './AuthorPage.css';

const AuthorPage = () => {
  const { name } = useParams();
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAuthorStories();
  }, [name]);

  const fetchAuthorStories = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/stories/by-author?author=${encodeURIComponent(name)}`);
      setStories(res.data || []);
    } catch { setStories([]); }
    finally { setLoading(false); }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;

  return (
    <div className="container author-page">
      <h2 className="section-title">
        <User size={20} /> Tác giả: {decodeURIComponent(name)}
      </h2>

      {stories.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>
          <BookOpen size={48} style={{ opacity: 0.3 }} />
          <p>Không tìm thấy truyện nào của tác giả này.</p>
        </div>
      ) : (
        <>
          <p style={{ color: '#666', marginBottom: '20px' }}>Tìm thấy {stories.length} truyện</p>
          <div className="author-stories-grid">
            {stories.map(story => (
              <Link key={story.storyId} to={`/story/${story.storyId}`} className="author-story-card card">
                <div className="author-story-cover">
                  <img src={story.coverImage || 'https://via.placeholder.com/150x200'} alt={story.title} />
                </div>
                <div className="author-story-info">
                  <div className="author-story-title">{story.title}</div>
                  <div className="author-story-meta">
                    <span><Eye size={14} /> {story.viewCount || 0} lượt xem</span>
                    <span>{story.contentType}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default AuthorPage;
