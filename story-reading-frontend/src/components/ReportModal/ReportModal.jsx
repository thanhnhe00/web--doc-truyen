import React, { useState } from 'react';
import { X, AlertTriangle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './ReportModal.css';

const ReportModal = ({ isOpen, onClose, targetType, targetId }) => {
  const { isAuthenticated } = useAuth();
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason.trim()) return;

    setLoading(true);
    setMessage('');
    try {
      await api.post('/reports', { targetType, targetId, reason: reason.trim() });
      setMessage('Báo cáo đã được gửi thành công!');
      setIsSuccess(true);
      setTimeout(() => {
        onClose();
        setReason('');
        setMessage('');
        setIsSuccess(false);
      }, 2000);
    } catch (err) {
      if (err.response?.status === 409) {
        setMessage('Bạn đã báo cáo đối tượng này và đang chờ xử lý.');
      } else {
        setMessage('Gửi báo cáo thất bại. Vui lòng thử lại.');
      }
      setIsSuccess(false);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    onClose();
    setReason('');
    setMessage('');
    setIsSuccess(false);
  };

  return (
    <div className="report-modal-overlay" onClick={handleClose}>
      <div className="report-modal card" onClick={e => e.stopPropagation()}>
        <div className="report-modal-header">
          <h3><AlertTriangle size={20} /> Báo cáo vi phạm</h3>
          <button className="report-modal-close" onClick={handleClose}>
            <X size={20} />
          </button>
        </div>

        {!isAuthenticated ? (
          <div className="report-modal-body">
            <p>Vui lòng đăng nhập để báo cáo.</p>
          </div>
        ) : (
          <form className="report-modal-body" onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Lý do báo cáo:</label>
              <textarea
                className="report-textarea"
                placeholder="Nhập lý do báo cáo (nội dung vi phạm, spam, ..."
                value={reason}
                onChange={e => setReason(e.target.value)}
                rows={4}
                required
              />
            </div>
            {message && (
              <div className={isSuccess ? 'auth-success' : 'auth-error'} style={{ marginTop: '8px' }}>
                {message}
              </div>
            )}
            <div className="report-modal-actions">
              <button type="button" className="btn btn-outline" onClick={handleClose}>Hủy</button>
              <button type="submit" className="btn btn-primary" disabled={loading || !reason.trim()}>
                {loading ? 'Đang gửi...' : 'Gửi báo cáo'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default ReportModal;
