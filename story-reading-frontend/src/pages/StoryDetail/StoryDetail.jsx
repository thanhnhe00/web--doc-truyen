import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { BookOpen, Bookmark, List, Eye, User, BookmarkCheck, Clock, Flag, Star, MessageCircle, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import ReportModal from '../../components/ReportModal/ReportModal';
import RatingStars from '../../components/RatingStars/RatingStars';
import CommentSection from '../../components/CommentSection/CommentSection';
import './StoryDetail.css';

const StoryDetail = () => {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const [story, setStory] = useState(null);
  const [chapters, setChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isFollowing, setIsFollowing] = useState(false);
  const [resumeHistory, setResumeHistory] = useState(null);
  const [isDismissed, setIsDismissed] = useState(() => {
    return sessionStorage.getItem(`dismissed-resume-${id}`) === 'true';
  });
  const [showReportModal, setShowReportModal] = useState(false);
  const [ratingSummary, setRatingSummary] = useState({ average: 0, count: 0, myScore: null });
  const [commentCount, setCommentCount] = useState(0);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const [storyRes, chaptersRes, ratingRes] = await Promise.all([
          api.get(`/stories/${id}`),
          api.get(`/stories/${id}/chapters`),
          api.get(`/stories/${id}/rating`)
        ]);
        setStory(storyRes.data);
        setChapters(chaptersRes.data);
        setIsFollowing(storyRes.data.isFollowing || false);
        setRatingSummary({
          average: ratingRes.data.averageScore || 0,
          count: ratingRes.data.ratingCount || 0,
          myScore: ratingRes.data.myScore
        });
        setCommentCount(storyRes.data.commentCount || 0);

        // FR08: Kiểm tra lịch sử đọc để hiển thị "Đọc tiếp"
        if (isAuthenticated) {
          try {
            const historyRes = await api.get(`/users/me/history/${id}`);
            if (historyRes.data) {
              setResumeHistory(historyRes.data);
            }
          } catch {
            // Không có lịch sử hoặc lỗi -> bỏ qua
          }
        }
      } catch {
        setStory(null);
        setChapters([]);
        setRatingSummary({ average: 0, count: 0, myScore: null });
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id, isAuthenticated]);

  const handleFollow = async () => {
    if (!isAuthenticated) return;
    try {
      if (isFollowing) {
        await api.delete(`/stories/${id}/follow`);
        setIsFollowing(false);
      } else {
        await api.post(`/stories/${id}/follow`);
        setIsFollowing(true);
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;
  if (!story) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Không tìm thấy truyện!</div>;

  const storyId = story.storyId || story.id;
  const firstChapterId = chapters.length > 0 ? (chapters[0].chapterId || chapters[0].id) : null;
  const categories = Array.isArray(story.categories)
    ? story.categories.map(c => typeof c === 'string' ? c : c.name)
    : [];

  return (
    <div className="container story-detail-page">
      <div className="card detail-card">
        <div className="detail-header">
          <div className="detail-cover-wrapper">
            <img src={story.coverImage || 'https://via.placeholder.com/200x266'} alt={story.title} className="detail-cover" />
          </div>
          <div className="detail-info">
            <h1 className="detail-title">{story.title}</h1>

            {/* FR06: Đánh giá sao */}
            <RatingStars
              storyId={storyId}
              averageRating={ratingSummary.average}
              ratingCount={ratingSummary.count}
              onRatingChange={(score) => setRatingSummary(prev => ({ ...prev, myScore: score }))}
            />

            <ul className="detail-meta">
              <li><User size={16} /> Tác giả: <Link to={`/author/${encodeURIComponent(story.author || '')}`} style={{ color: '#e74c3c', textDecoration: 'none' }}>{story.author || 'Đang cập nhật'}</Link></li>
              <li><Eye size={16} /> Lượt xem: {story.viewCount || 0}</li>
              <li><Star size={16} /> Đánh giá: {ratingSummary.average.toFixed(1)}/5 ({ratingSummary.count} lượt)</li>
              <li><MessageCircle size={16} /> Bình luận: {commentCount}</li>
              <li>Tình trạng: <span className="status-badge">{story.status || 'Đang cập nhật'}</span></li>
              {story.ageRating > 0 && (
                <li className="age-rating-badge">Tuổi: {story.ageRating}+</li>
              )}
            </ul>

            <div className="detail-categories">
              {categories.map((cat, idx) => (
                <span key={idx} className="category-tag">{cat}</span>
              ))}
            </div>

            <div className="detail-actions">
              {resumeHistory && !resumeHistory.isPrompted && !isDismissed ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Link
                    to={`/read/${storyId}/${resumeHistory.chapterId}`}
                    className="btn btn-primary resume-btn"
                    onClick={() => {
                      sessionStorage.setItem(`dismissed-resume-${id}`, 'true');
                      api.patch(`/users/me/history/${id}/prompted`).catch(() => {});
                    }}
                  >
                    <Clock size={18} /> Đọc tiếp - Chương {resumeHistory.chapterNumber}
                  </Link>
                  <button
                    className="btn btn-outline"
                    style={{ padding: '10px 12px', minWidth: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                    onClick={async () => {
                      setIsDismissed(true);
                      sessionStorage.setItem(`dismissed-resume-${id}`, 'true');
                      try {
                        await api.patch(`/users/me/history/${id}/prompted`);
                      } catch (err) {
                        console.error(err);
                      }
                    }}
                    title="Bỏ qua"
                  >
                    <X size={18} />
                  </button>
                </div>
              ) : firstChapterId ? (
                <Link to={`/read/${storyId}/${firstChapterId}`} className="btn btn-primary">
                  <BookOpen size={18} /> Đọc Từ Đầu
                </Link>
              ) : (
                <button className="btn btn-primary" disabled>Chưa có chương</button>
              )}
              {isAuthenticated && (
                <button className={`btn ${isFollowing ? 'btn-primary' : 'btn-outline'}`} onClick={handleFollow}>
                  {isFollowing ? <BookmarkCheck size={18} /> : <Bookmark size={18} />}
                  {isFollowing ? 'Đang theo dõi' : 'Theo dõi'}
                </button>
              )}
              {isAuthenticated && (
                <button className="btn btn-outline report-btn" onClick={() => setShowReportModal(true)}>
                  <Flag size={18} /> Báo cáo
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="detail-description">
          <h3 className="section-title"><BookOpen size={20} /> Giới Thiệu</h3>
          <p>{story.description}</p>
        </div>

        <div className="detail-chapters">
          <h3 className="section-title"><List size={20} /> Danh Sách Chương ({chapters.length})</h3>
          {chapters.length > 0 ? (
            <div className="chapter-grid">
              {chapters.map(chap => (
                <Link key={chap.chapterId || chap.id} to={`/read/${storyId}/${chap.chapterId || chap.id}`} className="chapter-link-box">
                  {chap.title || `Chương ${chap.chapterNumber}`}
                </Link>
              ))}
            </div>
          ) : (
            <p>Truyện chưa có chương nào.</p>
          )}
        </div>
      </div>

      <div className="card detail-card" style={{ marginTop: '20px' }}>
        <CommentSection chapterId={chapters.length > 0 ? (chapters[0].chapterId || chapters[0].id) : null} />
      </div>

      <ReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        targetType="STORY"
        targetId={storyId}
      />
    </div>
  );
};

export default StoryDetail;
