import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Home, ChevronLeft, ChevronRight, List, X, Settings, Sun, Moon, Type } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import CommentSection from '../../components/CommentSection/CommentSection';
import './Reading.css';

const Reading = () => {
  const { id, chapter } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const contentRef = useRef(null);

  const [chapterData, setChapterData] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showChapterList, setShowChapterList] = useState(false);
  const [ageError, setAgeError] = useState(null);

  // FR04/FR05: Cài đặt đọc
  const [showSettings, setShowSettings] = useState(false);
  const [fontSize, setFontSize] = useState(() => localStorage.getItem('reading-fontSize') || '18');
  const [darkMode, setDarkMode] = useState(() => localStorage.getItem('reading-darkMode') === 'true');
  const [lineHeight, setLineHeight] = useState(() => localStorage.getItem('reading-lineHeight') || '1.8');

  // FR08: Vị trí cuộn
  const scrollKey = `scroll-${id}-${chapter}`;
  const lastScrollKey = `last-chapter-${id}`;

  // Lưu cài đặt vào localStorage
  useEffect(() => {
    localStorage.setItem('reading-fontSize', fontSize);
    localStorage.setItem('reading-darkMode', darkMode);
    localStorage.setItem('reading-lineHeight', lineHeight);
  }, [fontSize, darkMode, lineHeight]);

  // FR08: Lưu vị trí cuộn khi rời trang (debounce)
  useEffect(() => {
    let scrollTimeout;
    const handleScroll = () => {
      if (scrollTimeout) clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        if (contentRef.current) {
          const scrollY = window.scrollY - contentRef.current.offsetTop;
          if (scrollY > 0) {
            sessionStorage.setItem(scrollKey, scrollY.toString());
          }
        }
      }, 500);
    };

    const handleBeforeUnload = () => {
      if (scrollTimeout) clearTimeout(scrollTimeout);
      if (contentRef.current) {
        const scrollY = window.scrollY - contentRef.current.offsetTop;
        if (scrollY > 0) sessionStorage.setItem(scrollKey, scrollY.toString());
      }
      localStorage.setItem(lastScrollKey, chapter);
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('scroll', handleScroll);
      window.removeEventListener('beforeunload', handleBeforeUnload);
      if (scrollTimeout) clearTimeout(scrollTimeout);
    };
  }, [scrollKey, lastScrollKey, chapter]);

  // FR08: Khôi phục vị trí cuộn
  const restoreScroll = useCallback(() => {
    const saved = sessionStorage.getItem(scrollKey);
    if (saved) {
      setTimeout(() => {
        window.scrollTo({ top: parseInt(saved), behavior: 'smooth' });
      }, 100);
    } else {
      window.scrollTo({ top: 0 });
    }
  }, [scrollKey]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [chapterRes, chaptersRes] = await Promise.all([
          api.get(`/chapters/${chapter}/read`),
          api.get(`/stories/${id}/chapters`)
        ]);
        setChapterData(chapterRes.data);
        setChapters(chaptersRes.data);
      } catch (err) {
        if (err.response && err.response.status === 403) {
          setAgeError(err.response.data?.message || 'Nội dung này yêu cầu độ tuổi phù hợp.');
          setLoading(false);
          return;
        }
        setChapterData(null);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id, chapter]);

  // Khôi phục vị trí cuộn khi chapter data thay đổi
  useEffect(() => {
    if (chapterData) {
      restoreScroll();
    }
  }, [chapterData, restoreScroll]);

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải chương...</div>;

  if (ageError) {
    return (
      <div className="container" style={{ padding: '60px 20px', textAlign: 'center' }}>
        <div className="card" style={{ padding: '40px', maxWidth: '500px', margin: '0 auto' }}>
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>🔒</div>
          <h2 style={{ color: '#e74c3c', marginBottom: '12px' }}>Nội dung bị hạn chế</h2>
          <p style={{ color: '#666', marginBottom: '24px' }}>{ageError}</p>
          <Link to={`/story/${id}`} className="btn btn-primary">Quay lại trang truyện</Link>
        </div>
      </div>
    );
  }
  if (!chapterData) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Không tìm thấy chương!</div>;

  const { chapterNumber, title, content, imageUrls, storyTitle, prevChapterId, nextChapterId } = chapterData;
  const storyId = chapterData.storyId || id;
  const currentChapterId = parseInt(chapter);

  const handlePrev = () => {
    if (prevChapterId) navigate(`/read/${storyId}/${prevChapterId}`);
  };

  const handleNext = () => {
    if (nextChapterId) navigate(`/read/${storyId}/${nextChapterId}`);
  };

  // FR04/FR05: Cài đặt đọc
  const readingStyle = {
    fontSize: `${fontSize}px`,
    lineHeight: lineHeight,
  };

  const pageStyle = darkMode ? { backgroundColor: '#1a1a2e', color: '#e0e0e0' } : {};

  const renderControls = () => (
    <div className="reading-controls container">
      <button className="btn btn-outline" onClick={handlePrev} disabled={!prevChapterId}>
        <ChevronLeft size={18} /> Chap Trước
      </button>
      <button className="btn btn-primary" onClick={() => setShowChapterList(true)}>
        <List size={18} /> Chọn Chap
      </button>
      <button className="btn btn-outline" onClick={handleNext} disabled={!nextChapterId}>
        Chap Sau <ChevronRight size={18} />
      </button>
    </div>
  );

  return (
    <div className={`reading-page ${darkMode ? 'reading-dark' : ''}`} style={pageStyle}>
      <div className="reading-top-bar">
        <div className="container reading-top-container">
          <Link to={`/story/${storyId}`} className="reading-breadcrumb">
            <Home size={18} />
            {storyTitle || 'Tên Truyện'}
          </Link>
          <span className="reading-chapter-title">/ {title || 'Chương...'}</span>
          <div className="reading-top-actions">
            <button className="reading-setting-btn" onClick={() => setShowSettings(!showSettings)} title="Cài đặt đọc">
              <Settings size={18} />
            </button>
          </div>
        </div>
      </div>

      {/* FR04/FR05: Panel cài đặt đọc */}
      {showSettings && (
        <div className={`reading-settings-panel ${darkMode ? 'settings-dark' : ''}`}>
          <div className="container settings-inner">
            <div className="setting-group">
              <label><Type size={14} /> Cỡ chữ: {fontSize}px</label>
              <input type="range" min="14" max="28" value={fontSize} onChange={e => setFontSize(e.target.value)} />
            </div>
            <div className="setting-group">
              <label>Khoảng cách dòng: {lineHeight}</label>
              <input type="range" min="1.2" max="2.5" step="0.1" value={lineHeight} onChange={e => setLineHeight(e.target.value)} />
            </div>
            <div className="setting-group">
              <button className={`btn btn-sm ${darkMode ? 'btn-primary' : 'btn-outline'}`} onClick={() => setDarkMode(!darkMode)}>
                {darkMode ? <Sun size={14} /> : <Moon size={14} />}
                {darkMode ? ' Sáng' : ' Tối'}
              </button>
            </div>
          </div>
        </div>
      )}

      {renderControls()}

      <div className="reading-content-area" ref={contentRef}>
        {imageUrls && imageUrls.length > 0 ? (
          <div className="comic-images">
            {imageUrls.map((url, idx) => (
              <img key={idx} src={url} alt={`Page ${idx + 1}`} className="comic-page" loading="lazy" />
            ))}
          </div>
        ) : (
          <div className="novel-text container card" style={readingStyle}>
            {content || 'Chương này chưa có nội dung.'}
          </div>
        )}
      </div>

      {renderControls()}

      {/* FR07: Bình luận & Phản hồi */}
      <div className="container">
        <CommentSection chapterId={currentChapterId} />
      </div>

      {/* Modal chọn chapter */}
      {showChapterList && (
        <div className="chapter-modal-overlay" onClick={() => setShowChapterList(false)}>
          <div className={`chapter-modal card ${darkMode ? 'modal-dark' : ''}`} onClick={e => e.stopPropagation()}>
            <div className="chapter-modal-header">
              <h3>Danh sách chương</h3>
              <button className="chapter-modal-close" onClick={() => setShowChapterList(false)}>
                <X size={20} />
              </button>
            </div>
            <div className="chapter-modal-list">
              {chapters.map(chap => {
                const chapId = chap.chapterId || chap.id;
                const isActive = chapId === currentChapterId;
                return (
                  <Link
                    key={chapId}
                    to={`/read/${storyId}/${chapId}`}
                    className={`chapter-modal-item ${isActive ? 'active' : ''}`}
                    onClick={() => setShowChapterList(false)}
                  >
                    {chap.title || `Chương ${chap.chapterNumber}`}
                  </Link>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Reading;
