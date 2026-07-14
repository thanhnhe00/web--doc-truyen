import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Eye } from 'lucide-react';
import api from '../../utils/api';
import './CategoryPage.css';

const CategoryPage = () => {
  const { id } = useParams();
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [categoryName, setCategoryName] = useState('');

  useEffect(() => {
    setPage(0);
    setStories([]);
    fetchStories(0);
    fetchCategoryName();
  }, [id]);

  const fetchCategoryName = async () => {
    try {
      const res = await api.get('/categories');
      const cat = res.data.find(c => String(c.categoryId) === String(id));
      if (cat) setCategoryName(cat.name);
    } catch {}
  };

  const fetchStories = async (p) => {
    setLoading(true);
    try {
      const res = await api.get(`/stories/by-category?categoryId=${id}&page=${p}&size=10`);
      const data = res.data.content || res.data;
      if (p === 0) {
        setStories(data);
      } else {
        setStories(prev => [...prev, ...data]);
      }
      setHasMore(data.length === 10);
    } catch {
      setStories([]);
    } finally {
      setLoading(false);
    }
  };

  const loadMore = () => {
    const next = page + 1;
    setPage(next);
    fetchStories(next);
  };

  return (
    <div className="container category-page">
      <h2 className="section-title">{categoryName || `Thể loại #${id}`}</h2>
      {stories.length === 0 && !loading ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Không có truyện nào.</div>
      ) : (
        <>
          <div className="category-grid">
            {stories.map(story => (
              <Link key={story.storyId} to={`/story/${story.storyId}`} className="category-card card">
                <div className="category-cover-wrapper">
                  <img src={story.coverImage || 'https://via.placeholder.com/150'} alt={story.title} className="category-cover" />
                </div>
                <div className="category-info">
                  <div className="category-title">{story.title}</div>
                  <div className="category-meta"><Eye size={14} /> {story.viewCount || 0}</div>
                </div>
              </Link>
            ))}
          </div>
          {hasMore && (
            <div style={{ textAlign: 'center', marginTop: '20px' }}>
              <button className="btn btn-outline" onClick={loadMore} disabled={loading}>
                {loading ? 'Đang tải...' : 'Xem thêm'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default CategoryPage;
