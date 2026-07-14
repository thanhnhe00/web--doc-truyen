import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Bell, CheckCheck, Filter } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './NotificationPage.css';

const NotificationPage = () => {
  const { isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [filter, setFilter] = useState('all'); // all, unread, read

  const fetchNotifications = useCallback(async (pageNum = 0, append = false) => {
    if (append) setLoadingMore(true);
    else setLoading(true);

    try {
      const res = await api.get(`/notifications?page=${pageNum}&size=20`);
      const data = res.data.content || res.data || [];
      if (append) {
        setNotifications(prev => [...prev, ...data]);
      } else {
        setNotifications(data);
      }
      setHasMore(data.length === 20);
      setPage(pageNum);
    } catch {
      if (!append) setNotifications([]);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated) return;
    fetchNotifications(0, false);
  }, [isAuthenticated, fetchNotifications]);

  const handleMarkRead = async (notificationId) => {
    try {
      await api.patch(`/notifications/${notificationId}/read`);
      setNotifications(prev => prev.map(n =>
        n.notificationId === notificationId ? { ...n, isRead: true } : n
      ));
    } catch {}
  };

  const handleMarkAllRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    } catch {}
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'STORY_APPROVED': return '✅';
      case 'STORY_REJECTED': return '❌';
      case 'CHAPTER_APPROVED': return '📖';
      case 'CHAPTER_REJECTED': return '🚫';
      case 'NEW_CHAPTER': return '🔔';
      case 'CONTENT_HIDDEN': return '👁️‍🗨️';
      default: return '📌';
    }
  };

  const getNotificationLink = (notification) => {
    if (notification.storyId) return `/story/${notification.storyId}`;
    return '#';
  };

  const filteredNotifications = notifications.filter(n => {
    if (filter === 'unread') return !n.isRead;
    if (filter === 'read') return n.isRead;
    return true;
  });

  const unreadCount = notifications.filter(n => !n.isRead).length;

  if (!isAuthenticated) {
    return (
      <div className="container notification-page">
        <div className="card" style={{ padding: '40px', textAlign: 'center' }}>
          <p>Vui lòng <Link to="/login">đăng nhập</Link> để xem thông báo.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container notification-page">
      <div className="notification-page-header">
        <h2 className="section-title"><Bell size={20} /> Thông báo</h2>
        <div className="notification-page-actions">
          {unreadCount > 0 && (
            <button className="btn btn-outline btn-sm" onClick={handleMarkAllRead}>
              <CheckCheck size={14} /> Đọc tất cả ({unreadCount})
            </button>
          )}
        </div>
      </div>

      <div className="notification-filters">
        <button className={`filter-btn ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>
          Tất cả ({notifications.length})
        </button>
        <button className={`filter-btn ${filter === 'unread' ? 'active' : ''}`} onClick={() => setFilter('unread')}>
          Chưa đọc ({unreadCount})
        </button>
        <button className={`filter-btn ${filter === 'read' ? 'active' : ''}`} onClick={() => setFilter('read')}>
          Đã đọc ({notifications.length - unreadCount})
        </button>
      </div>

      <div className="notification-page-list">
        {loading ? (
          <div className="notification-page-loading">Đang tải thông báo...</div>
        ) : filteredNotifications.length === 0 ? (
          <div className="notification-page-empty">
            <Bell size={48} style={{ opacity: 0.3 }} />
            <p>{filter === 'all' ? 'Không có thông báo nào.' : filter === 'unread' ? 'Không có thông báo chưa đọc.' : 'Không có thông báo đã đọc.'}</p>
          </div>
        ) : (
          filteredNotifications.map(n => (
            <Link
              key={n.notificationId}
              to={getNotificationLink(n)}
              className={`notification-page-item card ${!n.isRead ? 'notification-page-unread' : ''}`}
              onClick={() => { if (!n.isRead) handleMarkRead(n.notificationId); }}
            >
              <span className="notification-page-icon">{getNotificationIcon(n.type)}</span>
              <div className="notification-page-content">
                <div className="notification-page-title">{n.title}</div>
                <div className="notification-page-text">{n.content}</div>
                <div className="notification-page-time">
                  {n.createdAt ? new Date(n.createdAt).toLocaleString('vi-VN') : ''}
                </div>
              </div>
              {!n.isRead && <div className="notification-page-dot" />}
            </Link>
          ))
        )}
      </div>

      {hasMore && !loading && (
        <div style={{ textAlign: 'center', padding: '16px 0' }}>
          <button className="btn btn-outline" onClick={() => fetchNotifications(page + 1, true)} disabled={loadingMore}>
            {loadingMore ? 'Đang tải...' : 'Xem thêm'}
          </button>
        </div>
      )}
    </div>
  );
};

export default NotificationPage;
