import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Search, Eye, User } from 'lucide-react';
import api from '../../utils/api';
import './SearchResults.css';

const SearchResults = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
  const author = searchParams.get('author') || '';
  const categoryId = searchParams.get('categoryId') || '';
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState([]);
  const [localKeyword, setLocalKeyword] = useState(keyword);
  const [localAuthor, setLocalAuthor] = useState(author);
  const [localCategory, setLocalCategory] = useState(categoryId);

  useEffect(() => {
    api.get('/categories').then(res => setCategories(res.data || [])).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (author) params.set('author', author);
    if (categoryId) params.set('categoryId', categoryId);
    params.set('page', '0');
    params.set('size', '20');

    api.get(`/stories/search?${params.toString()}`)
      .then(res => setStories(res.data.content || res.data))
      .catch(() => setStories([]))
      .finally(() => setLoading(false));
  }, [keyword, author, categoryId]);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = {};
    if (localKeyword.trim()) params.keyword = localKeyword.trim();
    if (localAuthor.trim()) params.author = localAuthor.trim();
    if (localCategory) params.categoryId = localCategory;
    setSearchParams(params);
  };

  return (
    <div className="container search-page">
      <h2 className="section-title"><Search size={20} /> Tìm kiếm truyện</h2>

      <form className="search-filters" onSubmit={handleSearch}>
        <input type="text" placeholder="Tên truyện..." value={localKeyword}
          onChange={e => setLocalKeyword(e.target.value)} className="search-filter-input" />
        <input type="text" placeholder="Tác giả..." value={localAuthor}
          onChange={e => setLocalAuthor(e.target.value)} className="search-filter-input" />
        <select value={localCategory} onChange={e => setLocalCategory(e.target.value)} className="search-filter-select">
          <option value="">Tất cả thể loại</option>
          {categories.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}
        </select>
        <button type="submit" className="btn btn-primary btn-sm"><Search size={14} /> Tìm</button>
      </form>

      {(keyword || author || categoryId) && (
        <div className="search-result-info">
          Kết quả tìm kiếm: "
          {keyword && <span>{keyword}</span>}
          {author && <span> | Tác giả: {author}</span>}
          {categoryId && <span> | Thể loại: {categories.find(c => c.categoryId == categoryId)?.name || categoryId}</span>}
          " ({stories.length} kết quả)
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Đang tìm kiếm...</div>
      ) : stories.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Không tìm thấy truyện nào.</div>
      ) : (
        <div className="search-grid">
          {stories.map(story => (
            <Link key={story.storyId} to={`/story/${story.storyId}`} className="search-card card">
              <div className="search-cover-wrapper">
                <img src={story.coverImage || 'https://via.placeholder.com/150'} alt={story.title} className="search-cover" />
              </div>
              <div className="search-info">
                <div className="search-title">{story.title}</div>
                {story.author && <div className="search-author"><User size={12} /> {story.author}</div>}
                <div className="search-meta"><Eye size={14} /> {story.viewCount || 0}</div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default SearchResults;
