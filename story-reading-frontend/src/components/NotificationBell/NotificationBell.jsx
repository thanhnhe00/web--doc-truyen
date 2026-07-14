import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { Bell, Check, CheckCheck, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './NotificationBell.css';

const NotificationBell = () => {
  const { isAuthenticated } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  // Lấy số thông báo chưa đọc
  useEffect(() => {
    if (!isAuthenticated) return;
    const fetchCount = async () => {
      try {
        const res = await api.get('/notifications/unread-count');
        setUnreadCount(res.data.count || 0);
      } catch {
        setUnreadCount(0);
      }
    };
    fetchCount();
    // Poll mỗi 30 giây
    const interval = setInterval(fetchCount, 30000);
    return () => clearInterval(interval);
  }, [isAuthenticated]);

  // Đóng dropdown khi click bên ngoài
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Mở dropdown -> load danh sách thông báo
  const handleToggle = async () => {
    if (!isOpen) {
      setLoading(true);
      try {
        const res = await api.get('/notifications?page=0&size=10');
        setNotifications(res.data.content || res.data || []);
      } catch {
        setNotifications([]);
      } finally {
        setLoading(false);
      }
    }
    setIsOpen(!isOpen);
  };

  // Đánh dấu đã đọc 1 thông báo
  const handleMarkRead = async (notificationId) => {
    try {
      await api.patch(`/notifications/${notificationId}/read`);
      setNotifications(prev => prev.map(n =>
        n.notificationId === notificationId ? { ...n, isRead: true } : n
      ));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch {
      // Bỏ qua
    }
  };

  // Đánh dấu tất cả đã đọc
  const handleMarkAllRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch {
      // Bỏ qua
    }
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
    if (notification.storyId) {
      return `/story/${notification.storyId}`;
    }
    return '#';
  };

  if (!isAuthenticated) return null;

  return (
    <div className="notification-bell-wrapper" ref={dropdownRef}>
      <button className="notification-bell-btn" onClick={handleToggle} title="Thông báo">
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        )}
      </button>

      {isOpen && (
        <div className="notification-dropdown">
          <div className="notification-dropdown-header">
            <h4>Thông báo</h4>
            {unreadCount > 0 && (
              <button className="mark-all-read-btn" onClick={handleMarkAllRead}>
                <CheckCheck size={14} /> Đọc tất cả
              </button>
            )}
          </div>

          <div className="notification-list">
            {loading ? (
              <div className="notification-loading">Đang tải...</div>
            ) : notifications.length === 0 ? (
              <div className="notification-empty">Không có thông báo nào.</div>
            ) : (
              notifications.map(n => (
                <Link
                  key={n.notificationId}
                  to={getNotificationLink(n)}
                  className={`notification-item ${!n.isRead ? 'notification-unread' : ''}`}
                  onClick={() => {
                    if (!n.isRead) handleMarkRead(n.notificationId);
                    setIsOpen(false);
                  }}
                >
                  <span className="notification-icon">{getNotificationIcon(n.type)}</span>
                  <div className="notification-content">
                    <div className="notification-title">{n.title}</div>
                    <div className="notification-text">{n.content}</div>
                    <div className="notification-time">
                      {n.createdAt ? new Date(n.createdAt).toLocaleString('vi-VN') : ''}
                    </div>
                  </div>
                  {!n.isRead && <div className="notification-dot" />}
                </Link>
              ))
            )}
          </div>

          <div className="notification-dropdown-footer">
            <Link to="/notifications" className="view-all-link" onClick={() => setIsOpen(false)}>
              Xem tất cả thông báo
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
