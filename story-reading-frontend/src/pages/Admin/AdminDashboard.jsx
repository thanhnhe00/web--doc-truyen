import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Shield, Users, BookOpen, AlertTriangle, CheckCircle, XCircle,
  Eye, ChevronDown, ChevronUp, Lock, Unlock, Tag, FolderOpen, Plus, Edit2, Trash2
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const { isAuthenticated, isAdmin } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('stats');
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const [pendingStories, setPendingStories] = useState([]);
  const [loadingStories, setLoadingStories] = useState(false);

  const [pendingChapters, setPendingChapters] = useState([]);
  const [loadingChapters, setLoadingChapters] = useState(false);

  const [users, setUsers] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  const [reports, setReports] = useState([]);
  const [loadingReports, setLoadingReports] = useState(false);

  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [categoryForm, setCategoryForm] = useState({ name: '' });
  const [editingCategory, setEditingCategory] = useState(null);

  const [collections, setCollections] = useState([]);
  const [loadingCollections, setLoadingCollections] = useState(false);
  const [collectionForm, setCollectionForm] = useState({ name: '', description: '', storyIds: [] });
  const [editingCollection, setEditingCollection] = useState(null);

  const [expandedStory, setExpandedStory] = useState(null);
  const [expandedChapter, setExpandedChapter] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [hideReason, setHideReason] = useState('');
  const [actionTarget, setActionTarget] = useState(null);

  useEffect(() => {
    if (!isAuthenticated || !isAdmin) { navigate('/'); return; }
    fetchStats();
  }, [isAuthenticated, isAdmin, navigate]);

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await api.get('/admin/stats');
      setStats(res.data);
    } catch { setStats({ totalUsers: 0, totalStories: 0, pendingReports: 0 }); }
    finally { setLoading(false); }
  };

  const fetchPendingStories = async () => {
    setLoadingStories(true);
    try {
      const res = await api.get('/admin/stories/pending');
      setPendingStories(res.data || []);
    } catch { setPendingStories([]); }
    finally { setLoadingStories(false); }
  };

  const fetchPendingChapters = async () => {
    setLoadingChapters(true);
    try {
      const res = await api.get('/admin/chapters/pending');
      setPendingChapters(res.data || []);
    } catch { setPendingChapters([]); }
    finally { setLoadingChapters(false); }
  };

  const fetchUsers = async () => {
    setLoadingUsers(true);
    try {
      const res = await api.get('/admin/users');
      setUsers(res.data || []);
    } catch { setUsers([]); }
    finally { setLoadingUsers(false); }
  };

  const fetchReports = async () => {
    setLoadingReports(true);
    try {
      const res = await api.get('/admin/reports');
      setReports(res.data || []);
    } catch { setReports([]); }
    finally { setLoadingReports(false); }
  };

  const fetchCategories = async () => {
    setLoadingCategories(true);
    try {
      const res = await api.get('/categories');
      setCategories(res.data || []);
    } catch { setCategories([]); }
    finally { setLoadingCategories(false); }
  };

  const fetchCollections = async () => {
    setLoadingCollections(true);
    try {
      const res = await api.get('/collections');
      setCollections(res.data || []);
    } catch { setCollections([]); }
    finally { setLoadingCollections(false); }
  };

  useEffect(() => {
    if (activeTab === 'stories') fetchPendingStories();
    if (activeTab === 'chapters') fetchPendingChapters();
    if (activeTab === 'users') fetchUsers();
    if (activeTab === 'reports') fetchReports();
    if (activeTab === 'categories') fetchCategories();
    if (activeTab === 'collections') fetchCollections();
  }, [activeTab]);

  // ========== Story Moderation ==========
  const handleApproveStory = async (storyId) => {
    try {
      await api.patch(`/admin/stories/${storyId}/approve`);
      setPendingStories(prev => prev.filter(s => s.storyId !== storyId));
    } catch { alert('Duyệt truyện thất bại!'); }
  };

  const handleRejectStory = async (storyId) => {
    if (!rejectReason.trim()) { alert('Vui lòng nhập lý do từ chối'); return; }
    try {
      await api.patch(`/admin/stories/${storyId}/reject`, { reason: rejectReason });
      setPendingStories(prev => prev.filter(s => s.storyId !== storyId));
      setRejectReason(''); setActionTarget(null);
    } catch { alert('Từ chối truyện thất bại!'); }
  };

  const handleHideStory = async (storyId) => {
    if (!hideReason.trim()) { alert('Vui lòng nhập lý do ẩn'); return; }
    try {
      await api.patch(`/admin/stories/${storyId}/hide`, { reason: hideReason });
      setPendingStories(prev => prev.filter(s => s.storyId !== storyId));
      setHideReason(''); setActionTarget(null);
    } catch { alert('Ẩn truyện thất bại!'); }
  };

  // ========== Chapter Moderation ==========
  const handleApproveChapter = async (chapterId) => {
    try {
      await api.patch(`/admin/chapters/${chapterId}/approve`);
      setPendingChapters(prev => prev.filter(c => c.chapterId !== chapterId));
    } catch { alert('Duyệt chương thất bại!'); }
  };

  const handleRejectChapter = async (chapterId) => {
    if (!rejectReason.trim()) { alert('Vui lòng nhập lý do từ chối'); return; }
    try {
      await api.patch(`/admin/chapters/${chapterId}/reject`, { reason: rejectReason });
      setPendingChapters(prev => prev.filter(c => c.chapterId !== chapterId));
      setRejectReason(''); setActionTarget(null);
    } catch { alert('Từ chối chương thất bại!'); }
  };

  const handleHideChapter = async (chapterId) => {
    if (!hideReason.trim()) { alert('Vui lòng nhập lý do ẩn'); return; }
    try {
      await api.patch(`/admin/chapters/${chapterId}/hide`, { reason: hideReason });
      setPendingChapters(prev => prev.filter(c => c.chapterId !== chapterId));
      setHideReason(''); setActionTarget(null);
    } catch { alert('Ẩn chương thất bại!'); }
  };

  // ========== User Moderation ==========
  const handleChangeRole = async (userId, newRole) => {
    try {
      await api.patch(`/admin/users/${userId}/role?role=${newRole}`);
      setUsers(prev => prev.map(u => u.userId === userId ? { ...u, role: newRole } : u));
    } catch { alert('Thay đổi vai trò thất bại!'); }
  };

  const handleLockUser = async (userId) => {
    const reason = prompt('Nhập lý do khóa tài khoản:');
    if (!reason) return;
    try {
      await api.patch(`/admin/users/${userId}/lock`, { reason });
      setUsers(prev => prev.map(u => u.userId === userId ? { ...u, status: 'LOCKED' } : u));
    } catch { alert('Khóa tài khoản thất bại!'); }
  };

  const handleUnlockUser = async (userId) => {
    try {
      await api.patch(`/admin/users/${userId}/unlock`);
      setUsers(prev => prev.map(u => u.userId === userId ? { ...u, status: 'ACTIVE' } : u));
    } catch { alert('Mở khóa tài khoản thất bại!'); }
  };

  // ========== Report Actions ==========
  const handleResolveReport = async (reportId) => {
    try {
      await api.patch(`/admin/reports/${reportId}/resolve`);
      setReports(prev => prev.filter(r => r.reportId !== reportId));
    } catch { alert('Xử lý báo cáo thất bại!'); }
  };

  const handleDismissReport = async (reportId) => {
    try {
      await api.patch(`/admin/reports/${reportId}/dismiss`);
      setReports(prev => prev.filter(r => r.reportId !== reportId));
    } catch { alert('Bác bỏ báo cáo thất bại!'); }
  };

  // ========== Category CRUD ==========
  const handleSaveCategory = async () => {
    if (!categoryForm.name.trim()) { alert('Nhập tên thể loại'); return; }
    try {
      if (editingCategory) {
        await api.put(`/categories/${editingCategory.categoryId}`, categoryForm);
      } else {
        await api.post('/categories', categoryForm);
      }
      setCategoryForm({ name: '' });
      setEditingCategory(null);
      fetchCategories();
    } catch (e) { alert(e.response?.data?.message || 'Lỗi lưu thể loại!'); }
  };

  const handleDeleteCategory = async (id) => {
    if (!confirm('Xóa thể loại này?')) return;
    try {
      await api.delete(`/categories/${id}`);
      fetchCategories();
    } catch { alert('Xóa thể loại thất bại!'); }
  };

  // ========== Collection CRUD ==========
  const handleSaveCollection = async () => {
    if (!collectionForm.name.trim()) { alert('Nhập tên bộ sưu tập'); return; }
    try {
      if (editingCollection) {
        await api.put(`/collections/${editingCollection.collectionId}`, collectionForm);
      } else {
        await api.post('/collections', collectionForm);
      }
      setCollectionForm({ name: '', description: '', storyIds: [] });
      setEditingCollection(null);
      fetchCollections();
    } catch (e) { alert(e.response?.data?.message || 'Lỗi lưu bộ sưu tập!'); }
  };

  const handleDeleteCollection = async (id) => {
    if (!confirm('Xóa bộ sưu tập này?')) return;
    try {
      await api.delete(`/collections/${id}`);
      fetchCollections();
    } catch { alert('Xóa bộ sưu tập thất bại!'); }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;

  const tabs = [
    { key: 'stats', label: 'Thống kê', icon: <Eye size={16} /> },
    { key: 'stories', label: 'Duyệt truyện', icon: <BookOpen size={16} /> },
    { key: 'chapters', label: 'Duyệt chương', icon: <BookOpen size={16} /> },
    { key: 'users', label: 'Quản lý user', icon: <Users size={16} /> },
    { key: 'categories', label: 'Thể loại', icon: <Tag size={16} /> },
    { key: 'collections', label: 'Bộ sưu tập', icon: <FolderOpen size={16} /> },
    { key: 'reports', label: 'Báo cáo', icon: <AlertTriangle size={16} /> },
  ];

  return (
    <div className="container admin-dashboard">
      <h2 className="section-title"><Shield size={20} /> Quản trị hệ thống</h2>

      <div className="admin-tabs">
        {tabs.map(tab => (
          <button key={tab.key} className={`admin-tab ${activeTab === tab.key ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.key)}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* ========== Stats ========== */}
      {activeTab === 'stats' && (
        <div className="admin-stats-grid">
          <div className="admin-stat-card card">
            <Users size={32} className="admin-stat-icon" />
            <div className="admin-stat-value">{stats?.totalUsers || 0}</div>
            <div className="admin-stat-label">Tổng người dùng</div>
          </div>
          <div className="admin-stat-card card">
            <BookOpen size={32} className="admin-stat-icon" />
            <div className="admin-stat-value">{stats?.totalStories || 0}</div>
            <div className="admin-stat-label">Tổng truyện</div>
          </div>
          <div className="admin-stat-card card">
            <AlertTriangle size={32} className="admin-stat-icon warning" />
            <div className="admin-stat-value">{stats?.pendingReports || 0}</div>
            <div className="admin-stat-label">Báo cáo chờ xử lý</div>
          </div>
        </div>
      )}

      {/* ========== Duyệt truyện ========== */}
      {activeTab === 'stories' && (
        <div className="admin-section">
          <h3>Truyện chờ duyệt</h3>
          {loadingStories ? <div className="admin-loading">Đang tải...</div>
            : pendingStories.length === 0 ? <div className="admin-empty">Không có truyện nào chờ duyệt.</div>
            : (
              <div className="admin-list">
                {pendingStories.map(story => (
                  <div key={story.storyId} className="admin-item card">
                    <div className="admin-item-main">
                      <img src={story.coverImage || 'https://via.placeholder.com/60x80'} alt="" className="admin-item-cover" />
                      <div className="admin-item-info">
                        <div className="admin-item-title">{story.title}</div>
                        <div className="admin-item-meta">
                          Tác giả: {story.author || 'N/A'} | Loại: {story.contentType} | Tuổi: {story.ageRating || 0}+
                        </div>
                        <button className="expand-btn" onClick={() => setExpandedStory(expandedStory === story.storyId ? null : story.storyId)}>
                          {expandedStory === story.storyId ? <ChevronUp size={14} /> : <ChevronDown size={14} />} Chi tiết
                        </button>
                        {expandedStory === story.storyId && (
                          <div className="admin-item-detail"><p>{story.description || 'Không có mô tả.'}</p></div>
                        )}
                      </div>
                    </div>
                    <div className="admin-item-actions">
                      <button className="btn btn-primary btn-sm" onClick={() => handleApproveStory(story.storyId)}>
                        <CheckCircle size={14} /> Duyệt
                      </button>
                      <button className="btn btn-outline btn-sm reject-btn" onClick={() => setActionTarget({ type: 'reject-story', id: story.storyId })}>
                        <XCircle size={14} /> Từ chối
                      </button>
                      <button className="btn btn-outline btn-sm hide-btn" onClick={() => setActionTarget({ type: 'hide-story', id: story.storyId })}>
                        Ẩn
                      </button>
                    </div>
                    {actionTarget?.type === 'reject-story' && actionTarget?.id === story.storyId && (
                      <div className="admin-action-form">
                        <textarea placeholder="Lý do từ chối..." value={rejectReason} onChange={e => setRejectReason(e.target.value)} rows={2} />
                        <div className="admin-action-btns">
                          <button className="btn btn-primary btn-sm" onClick={() => handleRejectStory(story.storyId)}>Xác nhận</button>
                          <button className="btn btn-outline btn-sm" onClick={() => { setActionTarget(null); setRejectReason(''); }}>Hủy</button>
                        </div>
                      </div>
                    )}
                    {actionTarget?.type === 'hide-story' && actionTarget?.id === story.storyId && (
                      <div className="admin-action-form">
                        <textarea placeholder="Lý do ẩn..." value={hideReason} onChange={e => setHideReason(e.target.value)} rows={2} />
                        <div className="admin-action-btns">
                          <button className="btn btn-primary btn-sm" onClick={() => handleHideStory(story.storyId)}>Xác nhận</button>
                          <button className="btn btn-outline btn-sm" onClick={() => { setActionTarget(null); setHideReason(''); }}>Hủy</button>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )
          }
        </div>
      )}

      {/* ========== Duyệt chương ========== */}
      {activeTab === 'chapters' && (
        <div className="admin-section">
          <h3>Chương chờ duyệt</h3>
          {loadingChapters ? <div className="admin-loading">Đang tải...</div>
            : pendingChapters.length === 0 ? <div className="admin-empty">Không có chương nào chờ duyệt.</div>
            : (
              <div className="admin-list">
                {pendingChapters.map(ch => (
                  <div key={ch.chapterId} className="admin-item card">
                    <div className="admin-item-main">
                      <div className="admin-item-info">
                        <div className="admin-item-title">Chương {ch.chapterNumber}: {ch.title}</div>
                        <div className="admin-item-meta">Truyện: {ch.story?.title || 'N/A'}</div>
                        <button className="expand-btn" onClick={() => setExpandedChapter(expandedChapter === ch.chapterId ? null : ch.chapterId)}>
                          {expandedChapter === ch.chapterId ? <ChevronUp size={14} /> : <ChevronDown size={14} />} Nội dung
                        </button>
                        {expandedChapter === ch.chapterId && (
                          <div className="admin-item-detail">
                            <p>{ch.content ? ch.content.substring(0, 500) + '...' : 'Chương comic (xem trên trang truyện)'}</p>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="admin-item-actions">
                      <button className="btn btn-primary btn-sm" onClick={() => handleApproveChapter(ch.chapterId)}>
                        <CheckCircle size={14} /> Duyệt
                      </button>
                      <button className="btn btn-outline btn-sm reject-btn" onClick={() => setActionTarget({ type: 'reject-chapter', id: ch.chapterId })}>
                        <XCircle size={14} /> Từ chối
                      </button>
                      <button className="btn btn-outline btn-sm hide-btn" onClick={() => setActionTarget({ type: 'hide-chapter', id: ch.chapterId })}>
                        Ẩn
                      </button>
                    </div>
                    {actionTarget?.type === 'reject-chapter' && actionTarget?.id === ch.chapterId && (
                      <div className="admin-action-form">
                        <textarea placeholder="Lý do từ chối..." value={rejectReason} onChange={e => setRejectReason(e.target.value)} rows={2} />
                        <div className="admin-action-btns">
                          <button className="btn btn-primary btn-sm" onClick={() => handleRejectChapter(ch.chapterId)}>Xác nhận</button>
                          <button className="btn btn-outline btn-sm" onClick={() => { setActionTarget(null); setRejectReason(''); }}>Hủy</button>
                        </div>
                      </div>
                    )}
                    {actionTarget?.type === 'hide-chapter' && actionTarget?.id === ch.chapterId && (
                      <div className="admin-action-form">
                        <textarea placeholder="Lý do ẩn..." value={hideReason} onChange={e => setHideReason(e.target.value)} rows={2} />
                        <div className="admin-action-btns">
                          <button className="btn btn-primary btn-sm" onClick={() => handleHideChapter(ch.chapterId)}>Xác nhận</button>
                          <button className="btn btn-outline btn-sm" onClick={() => { setActionTarget(null); setHideReason(''); }}>Hủy</button>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )
          }
        </div>
      )}

      {/* ========== Quản lý user ========== */}
      {activeTab === 'users' && (
        <div className="admin-section">
          <h3>Quản lý người dùng</h3>
          {loadingUsers ? <div className="admin-loading">Đang tải...</div>
            : users.length === 0 ? <div className="admin-empty">Không có người dùng nào.</div>
            : (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr><th>ID</th><th>Tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th>Hành động</th></tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.userId} className={u.status === 'LOCKED' ? 'row-locked' : ''}>
                        <td>{u.userId}</td>
                        <td>{u.username}</td>
                        <td>{u.email}</td>
                        <td>
                          <select value={u.role} onChange={e => handleChangeRole(u.userId, e.target.value)} className="role-select">
                            <option value="READER">READER</option>
                            <option value="CREATOR">CREATOR</option>
                            <option value="ADMIN">ADMIN</option>
                          </select>
                        </td>
                        <td><span className={`status-chip ${u.status === 'ACTIVE' ? 'active' : 'locked'}`}>{u.status}</span></td>
                        <td className="table-actions">
                          {u.status === 'ACTIVE' ? (
                            <button className="btn btn-outline btn-sm" onClick={() => handleLockUser(u.userId)}><Lock size={12} /> Khóa</button>
                          ) : (
                            <button className="btn btn-primary btn-sm" onClick={() => handleUnlockUser(u.userId)}><Unlock size={12} /> Mở khóa</button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          }
        </div>
      )}

      {/* ========== Thể loại ========== */}
      {activeTab === 'categories' && (
        <div className="admin-section">
          <h3>Quản lý thể loại</h3>
          <div className="admin-form-inline">
            <input type="text" placeholder="Tên thể loại..." value={categoryForm.name}
              onChange={e => setCategoryForm({ name: e.target.value })} className="admin-input" />
            <button className="btn btn-primary btn-sm" onClick={handleSaveCategory}>
              {editingCategory ? 'Cập nhật' : <><Plus size={14} /> Thêm</>}
            </button>
            {editingCategory && (
              <button className="btn btn-outline btn-sm" onClick={() => { setEditingCategory(null); setCategoryForm({ name: '' }); }}>Hủy</button>
            )}
          </div>
          {loadingCategories ? <div className="admin-loading">Đang tải...</div>
            : categories.length === 0 ? <div className="admin-empty">Chưa có thể loại nào.</div>
            : (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead><tr><th>ID</th><th>Tên</th><th>Hành động</th></tr></thead>
                  <tbody>
                    {categories.map(c => (
                      <tr key={c.categoryId}>
                        <td>{c.categoryId}</td>
                        <td>{c.name}</td>
                        <td className="table-actions">
                          <button className="btn btn-outline btn-sm" onClick={() => { setEditingCategory(c); setCategoryForm({ name: c.name }); }}>
                            <Edit2 size={12} /> Sửa
                          </button>
                          <button className="btn btn-outline btn-sm reject-btn" onClick={() => handleDeleteCategory(c.categoryId)}>
                            <Trash2 size={12} /> Xóa
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          }
        </div>
      )}

      {/* ========== Bộ sưu tập ========== */}
      {activeTab === 'collections' && (
        <div className="admin-section">
          <h3>Quản lý bộ sưu tập chủ đề</h3>
          <div className="admin-form-card card">
            <input type="text" placeholder="Tên bộ sưu tập..." value={collectionForm.name}
              onChange={e => setCollectionForm({ ...collectionForm, name: e.target.value })} className="admin-input" />
            <textarea placeholder="Mô tả..." value={collectionForm.description}
              onChange={e => setCollectionForm({ ...collectionForm, description: e.target.value })} rows={2} className="admin-textarea" />
            <div className="admin-action-btns">
              <button className="btn btn-primary btn-sm" onClick={handleSaveCollection}>
                {editingCollection ? 'Cập nhật' : <><Plus size={14} /> Tạo mới</>}
              </button>
              {editingCollection && (
                <button className="btn btn-outline btn-sm" onClick={() => { setEditingCollection(null); setCollectionForm({ name: '', description: '', storyIds: [] }); }}>Hủy</button>
              )}
            </div>
          </div>
          {loadingCollections ? <div className="admin-loading">Đang tải...</div>
            : collections.length === 0 ? <div className="admin-empty">Chưa có bộ sưu tập nào.</div>
            : (
              <div className="admin-list">
                {collections.map(c => (
                  <div key={c.collectionId} className="admin-item card">
                    <div className="admin-item-info">
                      <div className="admin-item-title"><FolderOpen size={14} /> {c.name}</div>
                      <div className="admin-item-meta">{c.description || 'Không có mô tả'}</div>
                    </div>
                    <div className="admin-item-actions">
                      <button className="btn btn-outline btn-sm" onClick={() => { setEditingCollection(c); setCollectionForm({ name: c.name, description: c.description || '', storyIds: [] }); }}>
                        <Edit2 size={12} /> Sửa
                      </button>
                      <button className="btn btn-outline btn-sm reject-btn" onClick={() => handleDeleteCollection(c.collectionId)}>
                        <Trash2 size={12} /> Xóa
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )
          }
        </div>
      )}

      {/* ========== Báo cáo ========== */}
      {activeTab === 'reports' && (
        <div className="admin-section">
          <h3>Báo cáo vi phạm</h3>
          {loadingReports ? <div className="admin-loading">Đang tải...</div>
            : reports.length === 0 ? <div className="admin-empty">Không có báo cáo nào chờ xử lý.</div>
            : (
              <div className="admin-list">
                {reports.map(r => (
                  <div key={r.reportId} className="admin-item card report-item">
                    <div className="admin-item-info">
                      <div className="admin-item-title"><AlertTriangle size={14} /> Báo cáo #{r.reportId}</div>
                      <div className="admin-item-meta">
                        Đối tượng: {r.targetType} #{r.targetId} | Bởi: {r.reporter?.username || 'N/A'}
                      </div>
                      <div className="report-reason">Lý do: {r.reason}</div>
                      <div className="admin-item-meta">{r.createdAt ? new Date(r.createdAt).toLocaleString('vi-VN') : ''}</div>
                    </div>
                    <div className="admin-item-actions">
                      <button className="btn btn-primary btn-sm" onClick={() => handleResolveReport(r.reportId)}>
                        <CheckCircle size={14} /> Xử lý
                      </button>
                      <button className="btn btn-outline btn-sm" onClick={() => handleDismissReport(r.reportId)}>
                        <XCircle size={14} /> Bác bỏ
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )
          }
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
