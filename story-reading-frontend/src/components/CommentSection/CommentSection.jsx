import React, { useState, useEffect, useCallback } from 'react';
import { MessageCircle, Reply, Edit3, Trash2, ChevronDown, ChevronUp, Send, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './CommentSection.css';

const CommentSection = ({ chapterId }) => {
  const { user, isAuthenticated } = useAuth();
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  // Reply state
  const [replyTo, setReplyTo] = useState(null); // { commentId, username }
  const [replyContent, setReplyContent] = useState('');
  const [submittingReply, setSubmittingReply] = useState(false);

  // Edit state
  const [editingId, setEditingId] = useState(null);
  const [editContent, setEditContent] = useState('');
  const [savingEdit, setSavingEdit] = useState(false);

  // Expanded replies
  const [expandedReplies, setExpandedReplies] = useState({});

  const fetchComments = useCallback(async (pageNum = 0, append = false) => {
    if (append) setLoadingMore(true);
    else setLoading(true);

    try {
      const isAdmin = user?.role === 'ADMIN';
      const url = `/chapters/${chapterId}/comments?page=${pageNum}&size=10${isAdmin ? '&showAll=true' : ''}`;
      const res = await api.get(url);
      const data = res.data.content || res.data;
      if (append) {
        setComments(prev => [...prev, ...data]);
      } else {
        setComments(data);
      }
      setHasMore(data.length === 10);
      setPage(pageNum);
    } catch {
      if (!append) setComments([]);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [chapterId, user]);

  useEffect(() => {
    fetchComments(0, false);
  }, [fetchComments]);

  // ========== Gửi bình luận mới ==========
  const handleSubmitComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || submitting) return;

    setSubmitting(true);
    setError('');
    try {
      const res = await api.post(`/chapters/${chapterId}/comments`, {
        content: newComment.trim()
      });
      setComments(prev => [res.data, ...prev]);
      setNewComment('');
    } catch (err) {
      if (err.response?.status === 429) {
        setError(err.response.data?.message || 'Bạn bình luận quá nhanh.');
      } else {
        setError('Gửi bình luận thất bại.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  // ========== Gửi phản hồi ==========
  const handleSubmitReply = async (parentCommentId) => {
    if (!replyContent.trim() || submittingReply) return;

    setSubmittingReply(true);
    try {
      const res = await api.post(`/chapters/${chapterId}/comments`, {
        content: replyContent.trim(),
        parentId: parentCommentId
      });

      // Thêm phản hồi vào danh sách
      setComments(prev => prev.map(c => {
        if (c.commentId === parentCommentId) {
          return {
            ...c,
            replies: [...(c.replies || []), res.data]
          };
        }
        return c;
      }));

      setReplyContent('');
      setReplyTo(null);
      setExpandedReplies(prev => ({ ...prev, [parentCommentId]: true }));
    } catch (err) {
      if (err.response?.status === 429) {
        setError(err.response.data?.message || 'Bạn bình luận quá nhanh.');
      } else {
        setError('Gửi phản hồi thất bại.');
      }
    } finally {
      setSubmittingReply(false);
    }
  };

  // ========== Chỉnh sửa bình luận ==========
  const handleEdit = async (commentId) => {
    if (!editContent.trim() || savingEdit) return;

    setSavingEdit(true);
    try {
      const res = await api.put(`/comments/${commentId}`, { content: editContent.trim() });

      setComments(prev => prev.map(c => {
        if (c.commentId === commentId) {
          return { ...c, content: res.data.content, updatedAt: res.data.updatedAt };
        }
        // Kiểm tra trong replies
        if (c.replies && c.replies.length > 0) {
          return {
            ...c,
            replies: c.replies.map(r =>
              r.commentId === commentId ? { ...r, content: res.data.content, updatedAt: res.data.updatedAt } : r
            )
          };
        }
        return c;
      }));

      setEditingId(null);
      setEditContent('');
    } catch {
      setError('Chỉnh sửa bình luận thất bại.');
    } finally {
      setSavingEdit(false);
    }
  };

  // ========== Xóa bình luận ==========
  const handleDelete = async (commentId) => {
    if (!window.confirm('Bạn có chắc muốn xóa bình luận này?')) return;

    try {
      await api.delete(`/comments/${commentId}`);
      setComments(prev => prev.filter(c => {
        if (c.commentId === commentId) return false;
        if (c.replies && c.replies.length > 0) {
          c.replies = c.replies.filter(r => r.commentId !== commentId);
        }
        return true;
      }));
    } catch {
      setError('Xóa bình luận thất bại.');
    }
  };

  // ========== Ẩn/hiện bình luận (Admin) ==========
  const handleToggleHide = async (commentId, isHidden) => {
    try {
      if (isHidden) {
        await api.patch(`/comments/${commentId}/unhide`);
      } else {
        await api.patch(`/comments/${commentId}/hide`);
      }
      fetchComments(0, false);
    } catch {
      setError('Thay đổi trạng thái bình luận thất bại.');
    }
  };

  // ========== Toggle hiển thị phản hồi ==========
  const toggleReplies = (commentId) => {
    setExpandedReplies(prev => ({ ...prev, [commentId]: !prev[commentId] }));
  };

  const isAdmin = user?.role === 'ADMIN';

  // ========== Render 1 bình luận ==========
  const renderComment = (comment, isReply = false) => {
    const isOwner = user && comment.username === user.username;
    const isEditing = editingId === comment.commentId;
    const isExpanded = expandedReplies[comment.commentId];
    const replyCount = comment.replies?.length || 0;

    return (
      <div key={comment.commentId} className={`comment-item ${isReply ? 'comment-reply' : ''} ${comment.isHidden ? 'comment-hidden' : ''}`}>
        <div className="comment-header">
          <div className="comment-author">
            {comment.avatarUrl ? (
              <img src={comment.avatarUrl} alt="" className="comment-avatar" />
            ) : (
              <div className="comment-avatar-placeholder">
                {comment.username?.charAt(0).toUpperCase()}
              </div>
            )}
            <span className="comment-username">{comment.username}</span>
            {comment.isHidden && <span className="comment-hidden-badge">Đã ẩn</span>}
          </div>
          <span className="comment-time">
            {comment.createdAt ? new Date(comment.createdAt).toLocaleString('vi-VN') : ''}
            {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
              <span className="comment-edited"> (đã chỉnh sửa)</span>
            )}
          </span>
        </div>

        <div className="comment-body">
          {isEditing ? (
            <div className="comment-edit-form">
              <textarea
                className="comment-edit-input"
                value={editContent}
                onChange={e => setEditContent(e.target.value)}
                rows={3}
                maxLength={1000}
              />
              <div className="comment-edit-actions">
                <button className="btn btn-primary btn-sm" onClick={() => handleEdit(comment.commentId)} disabled={savingEdit || !editContent.trim()}>
                  {savingEdit ? 'Đang lưu...' : 'Lưu'}
                </button>
                <button className="btn btn-outline btn-sm" onClick={() => { setEditingId(null); setEditContent(''); }}>
                  <X size={14} /> Hủy
                </button>
              </div>
            </div>
          ) : (
            <p className="comment-content">{comment.content}</p>
          )}
        </div>

        <div className="comment-actions">
          {isAuthenticated && !isEditing && (
            <button className="comment-action-btn" onClick={() => { setReplyTo({ commentId: comment.commentId, username: comment.username }); setReplyContent(''); }}>
              <Reply size={14} /> Phản hồi
            </button>
          )}
          {isOwner && !isEditing && (
            <>
              <button className="comment-action-btn" onClick={() => { setEditingId(comment.commentId); setEditContent(comment.content); }}>
                <Edit3 size={14} /> Sửa
              </button>
              <button className="comment-action-btn comment-action-delete" onClick={() => handleDelete(comment.commentId)}>
                <Trash2 size={14} /> Xóa
              </button>
            </>
          )}
          {isAdmin && !isReply && (
            <button className="comment-action-btn" onClick={() => handleToggleHide(comment.commentId, comment.isHidden)}>
              {comment.isHidden ? 'Hiện lại' : 'Ẩn'}
            </button>
          )}
        </div>

        {/* Form phản hồi */}
        {replyTo?.commentId === comment.commentId && (
          <div className="reply-form">
            <p className="reply-to">Phản hồi <strong>@{replyTo.username}</strong>:</p>
            <div className="reply-input-row">
              <textarea
                className="reply-input"
                value={replyContent}
                onChange={e => setReplyContent(e.target.value)}
                placeholder="Viết phản hồi..."
                rows={2}
                maxLength={1000}
                autoFocus
              />
              <div className="reply-actions">
                <button className="btn btn-primary btn-sm" onClick={() => handleSubmitReply(comment.commentId)} disabled={submittingReply || !replyContent.trim()}>
                  {submittingReply ? '...' : <><Send size={14} /> Gửi</>}
                </button>
                <button className="btn btn-outline btn-sm" onClick={() => setReplyTo(null)}>
                  <X size={14}
                  /> Hủy
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Danh sách phản hồi */}
        {!isReply && replyCount > 0 && (
          <div className="replies-section">
            <button className="replies-toggle" onClick={() => toggleReplies(comment.commentId)}>
              {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
              {isExpanded ? 'Ẩn' : 'Xem'} {replyCount} phản hồi
            </button>
            {isExpanded && comment.replies?.map(reply => renderComment(reply, true))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="comment-section">
      <h3 className="section-title">
        <MessageCircle size={20} /> Bình luận ({comments.length})
      </h3>

      {/* Form bình luận mới */}
      {isAuthenticated ? (
        <form className="comment-form" onSubmit={handleSubmitComment}>
          <textarea
            className="comment-input"
            placeholder="Viết bình luận..."
            value={newComment}
            onChange={e => setNewComment(e.target.value)}
            rows={3}
            maxLength={1000}
          />
          <div className="comment-form-footer">
            <span className="char-count">{newComment.length}/1000</span>
            <button type="submit" className="btn btn-primary" disabled={submitting || !newComment.trim()}>
              {submitting ? 'Đang gửi...' : <><Send size={16} /> Gửi bình luận</>}
            </button>
          </div>
        </form>
      ) : (
        <div className="comment-login-hint">
          <p>Vui lòng <a href="/login">đăng nhập</a> để bình luận.</p>
        </div>
      )}

      {error && <div className="comment-error">{error}</div>}

      {/* Danh sách bình luận */}
      <div className="comments-list">
        {loading ? (
          <div className="comment-loading">Đang tải bình luận...</div>
        ) : comments.length === 0 ? (
          <div className="comment-empty">Chưa có bình luận nào. Hãy là người đầu tiên bình luận!</div>
        ) : (
          comments.map(comment => renderComment(comment))
        )}
      </div>

      {/* Nút xem thêm */}
      {hasMore && !loading && comments.length > 0 && (
        <div className="comment-load-more">
          <button className="btn btn-outline" onClick={() => fetchComments(page + 1, true)} disabled={loadingMore}>
            {loadingMore ? 'Đang tải...' : 'Xem thêm bình luận'}
          </button>
        </div>
      )}
    </div>
  );
};

export default CommentSection;
