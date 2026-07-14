import React, { useState } from 'react';
import { Star } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './RatingStars.css';

const RatingStars = ({ storyId, averageRating = 0, ratingCount = 0, onRatingChange }) => {
  const { isAuthenticated } = useAuth();
  const [hoveredStar, setHoveredStar] = useState(0);
  const [userRating, setUserRating] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [showLoginHint, setShowLoginHint] = useState(false);

  const handleRate = async (score) => {
    if (!isAuthenticated) {
      setShowLoginHint(true);
      setTimeout(() => setShowLoginHint(false), 3000);
      return;
    }
    if (submitting) return;

    setSubmitting(true);
    try {
      await api.post(`/stories/${storyId}/rating`, { score });
      setUserRating(score);
      if (onRatingChange) onRatingChange(score);
    } catch {
      // Nếu đã rate rồi, thử PUT
      try {
        await api.put(`/stories/${storyId}/rating`, { score });
        setUserRating(score);
        if (onRatingChange) onRatingChange(score);
      } catch {
        // Bỏ qua lỗi
      }
    } finally {
      setSubmitting(false);
    }
  };

  const displayRating = userRating || 0;

  return (
    <div className="rating-stars-container">
      <div className="rating-stars-row">
        <div className="stars-interactive">
          {[1, 2, 3, 4, 5].map(star => (
            <button
              key={star}
              className={`star-btn ${star <= (hoveredStar || displayRating) ? 'star-active' : ''}`}
              onMouseEnter={() => setHoveredStar(star)}
              onMouseLeave={() => setHoveredStar(0)}
              onClick={() => handleRate(star)}
              disabled={submitting}
              title={`${star} sao`}
            >
              <Star size={20} fill={star <= (hoveredStar || displayRating) ? '#f7941d' : 'none'} />
            </button>
          ))}
        </div>
        <span className="rating-average">{averageRating.toFixed(1)}</span>
        <span className="rating-count">({ratingCount} đánh giá)</span>
      </div>
      {showLoginHint && (
        <div className="rating-login-hint">
          Vui lòng <a href="/login">đăng nhập</a> để đánh giá.
        </div>
      )}
    </div>
  );
};

export default RatingStars;
