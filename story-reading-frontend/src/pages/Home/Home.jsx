import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Star, Clock } from 'lucide-react';
import Sidebar from '../../components/layout/Sidebar';
import api from '../../utils/api';
import './Home.css';

const Home = () => {
  const [recommendedStories, setRecommendedStories] = useState([]);
  const [newlyUpdated, setNewlyUpdated] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStories = async () => {
      try {
        const [trendingRes, latestRes] = await Promise.all([
          api.get('/stories/trending?limit=5'),
          api.get('/stories/latest?limit=8')
        ]);
        setRecommendedStories(trendingRes.data || []);
        setNewlyUpdated(latestRes.data || []);
      } catch {
        setRecommendedStories([]);
        setNewlyUpdated([]);
      } finally {
        setLoading(false);
      }
    };
    fetchStories();
  }, []);

  const getStoryId = (s) => s.storyId || s.id;

  return (
    <div className="container home-page">
      <div className="home-content">
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải dữ liệu...</div>
        ) : (
          <>
        <section className="section">
          <h2 className="section-title">
            <Star size={20} /> Truyện đề cử
          </h2>
          {recommendedStories.length === 0 ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>Chưa có truyện đề cử nào.</div>
          ) : (
            <div className="recommended-grid">
              {recommendedStories.map(story => (
                <div key={getStoryId(story)} className="story-card card">
                  <Link to={`/story/${getStoryId(story)}`} className="story-cover-wrapper">
                    <img src={story.coverImage || 'https://via.placeholder.com/150'} alt={story.title} className="story-cover" />
                  </Link>
                  <div className="story-info">
                    <Link to={`/story/${getStoryId(story)}`} className="story-title">{story.title}</Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="section">
          <h2 className="section-title">
            <Clock size={20} /> Truyện mới cập nhật
          </h2>
          {newlyUpdated.length === 0 ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>Chưa có truyện mới.</div>
          ) : (
            <div className="updated-grid">
              {newlyUpdated.map(story => (
                <div key={getStoryId(story)} className="updated-card card">
                  <Link to={`/story/${getStoryId(story)}`} className="updated-cover-wrapper">
                    <img src={story.coverImage || 'https://via.placeholder.com/150'} alt={story.title} className="updated-cover" />
                  </Link>
                  <div className="updated-info">
                    <Link to={`/story/${getStoryId(story)}`} className="updated-title">{story.title}</Link>
                    <ul className="chapter-list">
                      {story.chapters?.map((chap, idx) => (
                        <li key={idx} className="chapter-item">
                          <Link to={`/read/${getStoryId(story)}/${chap.chapterId || chap.id}`} className="chapter-link">{chap.title || `Chương ${chap.chapterNumber}`}</Link>
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
        </>
        )}
      </div>

      <div className="home-sidebar">
        <Sidebar />
      </div>
    </div>
  );
};

export default Home;
