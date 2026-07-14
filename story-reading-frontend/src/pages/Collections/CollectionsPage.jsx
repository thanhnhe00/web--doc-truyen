import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FolderOpen, Eye, ChevronRight } from 'lucide-react';
import api from '../../utils/api';
import './CollectionsPage.css';

const CollectionsPage = () => {
  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const [collectionStories, setCollectionStories] = useState({});
  const [loadingStories, setLoadingStories] = useState(null);

  useEffect(() => {
    fetchCollections();
  }, []);

  const fetchCollections = async () => {
    setLoading(true);
    try {
      const res = await api.get('/collections');
      setCollections(res.data || []);
    } catch { setCollections([]); }
    finally { setLoading(false); }
  };

  const toggleCollection = async (collectionId) => {
    if (expandedId === collectionId) {
      setExpandedId(null);
      return;
    }
    setExpandedId(collectionId);

    if (!collectionStories[collectionId]) {
      setLoadingStories(collectionId);
      try {
        const res = await api.get(`/collections/${collectionId}/stories`);
        setCollectionStories(prev => ({ ...prev, [collectionId]: res.data || [] }));
      } catch {
        setCollectionStories(prev => ({ ...prev, [collectionId]: [] }));
      } finally {
        setLoadingStories(null);
      }
    }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;

  return (
    <div className="container collections-page">
      <h2 className="section-title"><FolderOpen size={20} /> Bộ sưu tập chủ đề</h2>

      {collections.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center' }}>Chưa có bộ sưu tập nào.</div>
      ) : (
        <div className="collections-list">
          {collections.map(col => (
            <div key={col.collectionId} className="collection-item card">
              <div className="collection-header" onClick={() => toggleCollection(col.collectionId)}>
                <div className="collection-info">
                  <h3><FolderOpen size={18} /> {col.name}</h3>
                  {col.description && <p>{col.description}</p>}
                </div>
                <ChevronRight
                  size={20}
                  className={`collection-chevron ${expandedId === col.collectionId ? 'expanded' : ''}`}
                />
              </div>

              {expandedId === col.collectionId && (
                <div className="collection-stories">
                  {loadingStories === col.collectionId ? (
                    <div className="admin-loading">Đang tải truyện...</div>
                  ) : (collectionStories[col.collectionId]?.length || 0) === 0 ? (
                    <div className="empty-state-sm">Bộ sưu tập chưa có truyện nào.</div>
                  ) : (
                    <div className="collection-stories-grid">
                      {collectionStories[col.collectionId]?.map(story => (
                        <Link key={story.storyId} to={`/story/${story.storyId}`} className="collection-story-card">
                          <img src={story.coverImage || 'https://via.placeholder.com/100x133'} alt="" />
                          <div className="collection-story-info">
                            <div className="collection-story-title">{story.title}</div>
                            <div className="collection-story-meta"><Eye size={12} /> {story.viewCount || 0}</div>
                          </div>
                        </Link>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CollectionsPage;
