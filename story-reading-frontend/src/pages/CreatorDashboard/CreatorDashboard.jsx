import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  BookOpen, Eye, Users, BarChart3, Plus, Edit3, Trash2,
  CheckCircle, Clock, AlertCircle, Send, ChevronDown, ChevronUp, Camera
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import api from '../../utils/api';
import './CreatorDashboard.css';

const CreatorDashboard = () => {
  const { isAuthenticated, isCreator } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('overview');
  const [stats, setStats] = useState(null);
  const [storyStats, setStoryStats] = useState([]);
  const [myStories, setMyStories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Story management
  const [expandedStory, setExpandedStory] = useState(null);
  const [storyChapters, setStoryChapters] = useState({});
  const [loadingChapters, setLoadingChapters] = useState(null);

  // Create story form
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [categories, setCategories] = useState([]);
  const [newStory, setNewStory] = useState({
    title: '', author: '', description: '', coverImage: '',
    contentType: 'NOVEL', ageRating: 0, categoryIds: []
  });
  const [creating, setCreating] = useState(false);
  const [createMsg, setCreateMsg] = useState('');

  // Edit story
  const [editingStory, setEditingStory] = useState(null);
  const [editForm, setEditForm] = useState({
    title: '', author: '', description: '', coverImage: '',
    contentType: 'NOVEL', ageRating: 0, categoryIds: []
  });
  const [updating, setUpdating] = useState(false);
  const [editMsg, setEditMsg] = useState('');

  // Create chapter form
  const [showChapterForm, setShowChapterForm] = useState(null);
  const [newChapter, setNewChapter] = useState({ title: '', content: '', chapterNumber: 1, imageUrls: [] });
  const [creatingChapter, setCreatingChapter] = useState(false);

  // Edit chapter form
  const [editingChapter, setEditingChapter] = useState(null); // { storyId, chapterId }
  const [editChapterForm, setEditChapterForm] = useState({ title: '', content: '', imageUrls: [] });
  const [updatingChapter, setUpdatingChapter] = useState(false);

  useEffect(() => {
    if (!isAuthenticated || !isCreator) { navigate('/'); return; }
    fetchData();
  }, [isAuthenticated, isCreator, navigate]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsRes, storiesRes, catsRes, storyStatsRes] = await Promise.all([
        api.get('/users/me/creator/stats'),
        api.get('/stories/my'),
        api.get('/categories'),
        api.get('/users/me/creator/story-stats')
      ]);
      setStats(statsRes.data);
      setMyStories(storiesRes.data || []);
      setCategories(catsRes.data || []);
      setStoryStats(storyStatsRes.data || []);
    } catch {
      setStats({ totalStories: 0, totalViews: 0, totalFollowers: 0 });
    } finally {
      setLoading(false);
    }
  };

  // ========== Tạo truyện mới ==========
  const handleCreateStory = async (e) => {
    e.preventDefault();
    if (!newStory.title.trim()) return;
    setCreating(true);
    setCreateMsg('');
    try {
      await api.post('/stories', newStory);
      setCreateMsg('Tạo truyện thành công!');
      setShowCreateForm(false);
      setNewStory({ title: '', author: '', description: '', coverImage: '', contentType: 'NOVEL', ageRating: 0, categoryIds: [] });
      fetchData();
    } catch (err) {
      setCreateMsg('Tạo truyện thất bại: ' + (err.response?.data?.message || 'Lỗi không xác định'));
    } finally {
      setCreating(false);
    }
  };

  // ========== Gửi duyệt truyện ==========
  const handleSubmitForApproval = async (storyId) => {
    if (!window.confirm('Gửi truyện để duyệt?')) return;
    try {
      await api.patch(`/stories/${storyId}/submit`);
      fetchData();
    } catch { alert('Gửi duyệt thất bại!'); }
  };

  // ========== Xóa truyện ==========
  const handleDeleteStory = async (storyId) => {
    if (!window.confirm('Bạn có chắc muốn xóa truyện này?')) return;
    try {
      await api.delete(`/stories/${storyId}`);
      fetchData();
    } catch { alert('Xóa truyện thất bại!'); }
  };

  // ========== Mở form chỉnh sửa truyện ==========
  const openEditForm = (story) => {
    setEditingStory(story.storyId);
    setEditMsg('');
    setEditForm({
      title: story.title || '',
      author: story.author || '',
      description: story.description || '',
      coverImage: story.coverImage || '',
      contentType: story.contentType || 'NOVEL',
      ageRating: story.ageRating || 0,
      categoryIds: story.categories?.map(c => c.categoryId) || []
    });
  };

  // ========== Cập nhật truyện ==========
  const handleUpdateStory = async (storyId) => {
    if (!editForm.title.trim()) return;
    setUpdating(true);
    setEditMsg('');
    try {
      await api.put(`/stories/${storyId}`, editForm);
      setEditMsg('Cập nhật thành công!');
      setEditingStory(null);
      fetchData();
    } catch (err) {
      setEditMsg('Cập nhật thất bại: ' + (err.response?.data?.message || 'Lỗi không xác định'));
    } finally {
      setUpdating(false);
    }
  };

  // ========== Load chapters của story ==========
  const loadChapters = async (storyId) => {
    if (storyChapters[storyId]) {
      setStoryChapters(prev => { const n = { ...prev }; delete n[storyId]; return n; });
      return;
    }
    setLoadingChapters(storyId);
    try {
      const res = await api.get(`/stories/${storyId}/chapters`);
      setStoryChapters(prev => ({ ...prev, [storyId]: res.data || [] }));
    } catch {
      setStoryChapters(prev => ({ ...prev, [storyId]: [] }));
    } finally {
      setLoadingChapters(null);
    }
  };

  // ========== Tạo chapter mới ==========
  const handleCreateChapter = async (storyId) => {
    if (!newChapter.title.trim()) return;
    setCreatingChapter(true);
    try {
      await api.post(`/stories/${storyId}/chapters`, {
        ...newChapter,
        imageUrls: newChapter.imageUrls.filter(u => u.trim())
      });
      setShowChapterForm(null);
      setNewChapter({ title: '', content: '', chapterNumber: 1, imageUrls: [] });
      loadChapters(storyId);
    } catch (err) {
      alert('Tạo chương thất bại: ' + (err.response?.data?.message || ''));
    } finally {
      setCreatingChapter(false);
    }
  };

  // ========== Gửi duyệt chapter ==========
  const handleSubmitChapter = async (chapterId, storyId) => {
    try {
      await api.patch(`/stories/${storyId}/chapters/${chapterId}/submit`);
      loadChapters(storyId);
    } catch { alert('Gửi duyệt chương thất bại!'); }
  };

  // ========== Xóa chapter ==========
  const handleDeleteChapter = async (chapterId, storyId) => {
    if (!window.confirm('Xóa chương này?')) return;
    try {
      await api.delete(`/stories/${storyId}/chapters/${chapterId}`);
      loadChapters(storyId);
    } catch { alert('Xóa chương thất bại!'); }
  };

  // ========== Mở form sửa chapter ==========
  const openEditChapterForm = async (chapter, storyId) => {
    try {
      const res = await api.get(`/chapters/${chapter.chapterId}/read`);
      setEditingChapter({ storyId, chapterId: chapter.chapterId, chapterNumber: chapter.chapterNumber });
      setEditChapterForm({
        title: chapter.title || '',
        content: res.data.content || '',
        imageUrls: res.data.imageUrls || []
      });
    } catch {
      setEditingChapter({ storyId, chapterId: chapter.chapterId, chapterNumber: chapter.chapterNumber });
      setEditChapterForm({ title: chapter.title || '', content: '', imageUrls: [] });
    }
  };

  // ========== Cập nhật chapter ==========
  const handleUpdateChapter = async (chapterId, storyId) => {
    if (!editChapterForm.title.trim()) return;
    setUpdatingChapter(true);
    try {
      await api.put(`/stories/${storyId}/chapters/${chapterId}`, {
        title: editChapterForm.title,
        content: editChapterForm.content,
        chapterNumber: editingChapter.chapterNumber,
        imageUrls: editChapterForm.imageUrls.filter(u => u && u.trim())
      });
      setEditingChapter(null);
      loadChapters(storyId);
    } catch (err) {
      alert('Cập nhật chương thất bại: ' + (err.response?.data?.message || ''));
    } finally {
      setUpdatingChapter(false);
    }
  };

  const getStatusInfo = (status) => {
    switch (status) {
      case 'PUBLISHED': return { label: 'Đã xuất bản', className: 'published', icon: <CheckCircle size={14} /> };
      case 'PENDING': return { label: 'Chờ duyệt', className: 'pending', icon: <Clock size={14} /> };
      case 'DRAFT': return { label: 'Bản nháp', className: 'draft', icon: <Edit3 size={14} /> };
      case 'REJECTED': return { label: 'Bị từ chối', className: 'rejected', icon: <AlertCircle size={14} /> };
      case 'HIDDEN': return { label: 'Đã ẩn', className: 'hidden', icon: <AlertCircle size={14} /> };
      default: return { label: status, className: '', icon: null };
    }
  };

  if (loading) return <div className="container" style={{ padding: '40px', textAlign: 'center' }}>Đang tải...</div>;

  return (
    <div className="container creator-dashboard">
      <h2 className="section-title"><BarChart3 size={20} /> Bảng điều khiển tác giả</h2>

      <div className="creator-tabs">
        <button className={`creator-tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>
          <BarChart3 size={16} /> Tổng quan
        </button>
        <button className={`creator-tab ${activeTab === 'stories' ? 'active' : ''}`} onClick={() => setActiveTab('stories')}>
          <BookOpen size={16} /> Quản lý truyện
        </button>
      </div>

      {/* ========== Tab Tổng quan ========== */}
      {activeTab === 'overview' && (
        <>
          <div className="stats-grid">
            <div className="stat-card card">
              <BookOpen size={32} className="stat-icon" />
              <div className="stat-value">{stats?.totalStories || 0}</div>
              <div className="stat-label">Tổng truyện</div>
            </div>
            <div className="stat-card card">
              <Eye size={32} className="stat-icon" />
              <div className="stat-value">{stats?.totalViews || 0}</div>
              <div className="stat-label">Tổng lượt xem</div>
            </div>
            <div className="stat-card card">
              <Users size={32} className="stat-icon" />
              <div className="stat-value">{stats?.totalFollowers || 0}</div>
              <div className="stat-label">Người theo dõi</div>
            </div>
          </div>

          <div className="stories-section">
            <h3>Thống kê chi tiết từng truyện</h3>
            {storyStats.length > 0 ? (
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr><th>Truyện</th><th>Trạng thái</th><th>Lượt xem</th><th>Người theo dõi</th></tr>
                  </thead>
                  <tbody>
                    {storyStats.map(s => (
                      <tr key={s.storyId}>
                        <td><Link to={`/story/${s.storyId}`}>{s.title}</Link></td>
                        <td><span className={`status-chip ${s.status === 'PUBLISHED' ? 'active' : s.status === 'PENDING' ? 'pending' : ''}`}>{s.status}</span></td>
                        <td>{s.viewCount || 0}</td>
                        <td>{s.followerCount || 0}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <div className="admin-empty">Chưa có dữ liệu.</div>}
          </div>

          <div className="stories-section">
            <h3>Truyện gần đây</h3>
            {myStories.length === 0 ? (
              <div className="empty-state">
                <p>Bạn chưa có truyện nào. Hãy bắt đầu viết!</p>
                <button className="btn btn-primary" onClick={() => setActiveTab('stories')}>
                  <Plus size={16} /> Tạo truyện mới
                </button>
              </div>
            ) : (
              <div className="my-stories-list">
                {myStories.slice(0, 5).map(story => {
                  const statusInfo = getStatusInfo(story.status);
                  return (
                    <div key={story.storyId} className="my-story-item card">
                      <img src={story.coverImage || 'https://via.placeholder.com/60x80'} alt="" className="my-story-cover" />
                      <div className="my-story-info">
                        <div className="my-story-title">{story.title}</div>
                        <div className="my-story-meta">
                          <span className={`status-chip ${statusInfo.className}`}>{statusInfo.icon} {statusInfo.label}</span>
                        </div>
                      </div>
                      <Link to={`/story/${story.storyId}`} className="btn btn-outline btn-sm">Xem</Link>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      )}

      {/* ========== Tab Quản lý truyện ========== */}
      {activeTab === 'stories' && (
        <div className="stories-management">
          <div className="stories-header">
            <h3>Truyện của tôi ({myStories.length})</h3>
            <button className="btn btn-primary" onClick={() => setShowCreateForm(!showCreateForm)}>
              <Plus size={16} /> Tạo truyện mới
            </button>
          </div>

          {/* Form tạo truyện mới */}
          {showCreateForm && (
            <div className="create-story-form card">
              <h4>Tạo truyện mới</h4>
              {createMsg && <div className={createMsg.includes('thành công') ? 'auth-success' : 'auth-error'}>{createMsg}</div>}
              <form onSubmit={handleCreateStory}>
                <div className="form-row">
                  <div className="form-group">
                    <label>Tiêu đề *</label>
                    <input type="text" value={newStory.title} onChange={e => setNewStory(p => ({ ...p, title: e.target.value }))} required />
                  </div>
                  <div className="form-group">
                    <label>Tác giả</label>
                    <input type="text" value={newStory.author} onChange={e => setNewStory(p => ({ ...p, author: e.target.value }))} />
                  </div>
                </div>
                <div className="form-group">
                  <label>Mô tả</label>
                  <textarea value={newStory.description} onChange={e => setNewStory(p => ({ ...p, description: e.target.value }))} rows={3} />
                </div>
                <div className="form-group">
                  <label>Ảnh bìa</label>
                  <div className="cover-upload-row">
                    {newStory.coverImage && (
                      <img src={newStory.coverImage} alt="Preview" className="cover-preview" />
                    )}
                    <input type="text" value={newStory.coverImage} onChange={e => setNewStory(p => ({ ...p, coverImage: e.target.value }))} placeholder="Hoặc nhập URL..." className="cover-url-input" />
                    <label className="btn btn-outline btn-sm cover-upload-btn">
                      <Camera size={14} /> Chọn file
                      <input type="file" accept="image/*" style={{ display: 'none' }} onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file) return;
                        const formData = new FormData();
                        formData.append('file', file);
                        try {
                          const res = await api.post('/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                          setNewStory(p => ({ ...p, coverImage: res.data.url }));
                        } catch { alert('Tải ảnh thất bại!'); }
                      }} />
                    </label>
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Loại nội dung</label>
                    <select value={newStory.contentType} onChange={e => setNewStory(p => ({ ...p, contentType: e.target.value }))}>
                      <option value="NOVEL">Truyện chữ</option>
                      <option value="COMIC">Truyện tranh</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Phân loại tuổi</label>
                    <select value={newStory.ageRating} onChange={e => setNewStory(p => ({ ...p, ageRating: parseInt(e.target.value) }))}>
                      <option value={0}>Mọi lứa tuổi</option>
                      <option value={16}>16+</option>
                      <option value={18}>18+</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Thể loại</label>
                    <select multiple value={newStory.categoryIds} onChange={e => {
                      const selected = Array.from(e.target.selectedOptions, o => parseInt(o.value));
                      setNewStory(p => ({ ...p, categoryIds: selected }));
                    }}>
                      {categories.map(c => (
                        <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={creating}>
                    {creating ? 'Đang tạo...' : 'Tạo truyện'}
                  </button>
                  <button type="button" className="btn btn-outline" onClick={() => setShowCreateForm(false)}>Hủy</button>
                </div>
              </form>
            </div>
          )}

          {/* Form chỉnh sửa truyện */}
          {editingStory && (
            <div className="create-story-form card">
              <h4>Chỉnh sửa truyện</h4>
              {editMsg && <div className={editMsg.includes('thành công') ? 'auth-success' : 'auth-error'}>{editMsg}</div>}
              <form onSubmit={(e) => { e.preventDefault(); handleUpdateStory(editingStory); }}>
                <div className="form-row">
                  <div className="form-group">
                    <label>Tiêu đề *</label>
                    <input type="text" value={editForm.title} onChange={e => setEditForm(p => ({ ...p, title: e.target.value }))} required />
                  </div>
                  <div className="form-group">
                    <label>Tác giả</label>
                    <input type="text" value={editForm.author} onChange={e => setEditForm(p => ({ ...p, author: e.target.value }))} />
                  </div>
                </div>
                <div className="form-group">
                  <label>Mô tả</label>
                  <textarea value={editForm.description} onChange={e => setEditForm(p => ({ ...p, description: e.target.value }))} rows={3} />
                </div>
                <div className="form-group">
                  <label>Ảnh bìa</label>
                  <div className="cover-upload-row">
                    {editForm.coverImage && (
                      <img src={editForm.coverImage} alt="Preview" className="cover-preview" />
                    )}
                    <input type="text" value={editForm.coverImage} onChange={e => setEditForm(p => ({ ...p, coverImage: e.target.value }))} placeholder="Hoặc nhập URL..." className="cover-url-input" />
                    <label className="btn btn-outline btn-sm cover-upload-btn">
                      <Camera size={14} /> Chọn file
                      <input type="file" accept="image/*" style={{ display: 'none' }} onChange={async (e) => {
                        const file = e.target.files?.[0];
                        if (!file) return;
                        const formData = new FormData();
                        formData.append('file', file);
                        try {
                          const res = await api.post('/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                          setEditForm(p => ({ ...p, coverImage: res.data.url }));
                        } catch { alert('Tải ảnh thất bại!'); }
                      }} />
                    </label>
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Loại nội dung</label>
                    <select value={editForm.contentType} onChange={e => setEditForm(p => ({ ...p, contentType: e.target.value }))}>
                      <option value="NOVEL">Truyện chữ</option>
                      <option value="COMIC">Truyện tranh</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Phân loại tuổi</label>
                    <select value={editForm.ageRating} onChange={e => setEditForm(p => ({ ...p, ageRating: parseInt(e.target.value) }))}>
                      <option value={0}>Mọi lứa tuổi</option>
                      <option value={16}>16+</option>
                      <option value={18}>18+</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Thể loại</label>
                    <select multiple value={editForm.categoryIds} onChange={e => {
                      const selected = Array.from(e.target.selectedOptions, o => parseInt(o.value));
                      setEditForm(p => ({ ...p, categoryIds: selected }));
                    }}>
                      {categories.map(c => (
                        <option key={c.categoryId} value={c.categoryId}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn btn-primary" disabled={updating}>
                    {updating ? 'Đang lưu...' : 'Lưu thay đổi'}
                  </button>
                  <button type="button" className="btn btn-outline" onClick={() => setEditingStory(null)}>Hủy</button>
                </div>
              </form>
            </div>
          )}

          {/* Danh sách truyện */}
          <div className="my-stories-list">
            {myStories.map(story => {
              const statusInfo = getStatusInfo(story.status);
              const isExpanded = expandedStory === story.storyId;
              const chapters = storyChapters[story.storyId];
              const isLoadingCh = loadingChapters === story.storyId;

              return (
                <div key={story.storyId} className="my-story-manage-item card">
                  <div className="manage-item-main">
                    <img src={story.coverImage || 'https://via.placeholder.com/60x80'} alt="" className="my-story-cover" />
                    <div className="my-story-info">
                      <div className="my-story-title">{story.title}</div>
                      <div className="my-story-meta">
                        <span className={`status-chip ${statusInfo.className}`}>{statusInfo.icon} {statusInfo.label}</span>
                        <span className="my-story-type">{story.contentType}</span>
                      </div>
                    </div>
                    <div className="manage-item-actions">
                      {story.status === 'DRAFT' && (
                        <button className="btn btn-primary btn-sm" onClick={() => handleSubmitForApproval(story.storyId)}>
                          <Send size={12} /> Gửi duyệt
                        </button>
                      )}
                      <button className="btn btn-outline btn-sm" onClick={() => openEditForm(story)}>
                        <Edit3 size={12} /> Sửa
                      </button>
                      <button className="btn btn-outline btn-sm" onClick={() => loadChapters(story.storyId)}>
                        {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                        Chương
                      </button>
                      <Link to={`/story/${story.storyId}`} className="btn btn-outline btn-sm">Xem</Link>
                      <button className="btn btn-outline btn-sm delete-btn" onClick={() => handleDeleteStory(story.storyId)}>
                        <Trash2 size={12} />
                      </button>
                    </div>
                  </div>

                  {/* Expanded chapters */}
                  {isExpanded && (
                    <div className="manage-chapters-section">
                      {isLoadingCh ? (
                        <div className="admin-loading">Đang tải chương...</div>
                      ) : (
                        <>
                          <div className="chapters-header">
                            <span>Danh sách chương ({chapters?.length || 0})</span>
                            <button className="btn btn-primary btn-sm" onClick={() => setShowChapterForm(story.storyId)}>
                              <Plus size={12} /> Thêm chương
                            </button>
                          </div>

                           {/* Form tạo chapter */}
                          {showChapterForm === story.storyId && (
                            <div className="create-chapter-form">
                              <div className="form-row">
                                <div className="form-group">
                                  <label>Số chương</label>
                                  <input type="number" min="1" value={newChapter.chapterNumber} onChange={e => setNewChapter(p => ({ ...p, chapterNumber: parseInt(e.target.value) }))} />
                                </div>
                                <div className="form-group">
                                  <label>Tiêu đề chương *</label>
                                  <input type="text" value={newChapter.title} onChange={e => setNewChapter(p => ({ ...p, title: e.target.value }))} required />
                                </div>
                              </div>
                              <div className="form-group">
                                <label>Nội dung</label>
                                <textarea value={newChapter.content} onChange={e => setNewChapter(p => ({ ...p, content: e.target.value }))} rows={5} placeholder="Nhập nội dung chương..." />
                              </div>
                              {story.contentType === 'COMIC' && (
                                <div className="form-group">
                                  <label>URL ảnh (mỗi dòng 1 URL)</label>
                                  <textarea value={newChapter.imageUrls.join('\n')} onChange={e => setNewChapter(p => ({ ...p, imageUrls: e.target.value.split('\n') }))} rows={3} placeholder="https://example.com/page1.jpg" />
                                </div>
                              )}
                              <div className="form-actions">
                                <button className="btn btn-primary btn-sm" onClick={() => handleCreateChapter(story.storyId)} disabled={creatingChapter}>
                                  {creatingChapter ? 'Đang tạo...' : 'Tạo chương'}
                                </button>
                                <button className="btn btn-outline btn-sm" onClick={() => setShowChapterForm(null)}>Hủy</button>
                              </div>
                            </div>
                          )}

                          {/* Form sửa chapter */}
                          {editingChapter?.storyId === story.storyId && (
                            <div className="create-chapter-form">
                              <h5>Sửa chương #{editingChapter.chapterNumber}</h5>
                              <div className="form-group">
                                <label>Tiêu đề chương *</label>
                                <input type="text" value={editChapterForm.title} onChange={e => setEditChapterForm(p => ({ ...p, title: e.target.value }))} required />
                              </div>
                              <div className="form-group">
                                <label>Nội dung</label>
                                <textarea value={editChapterForm.content} onChange={e => setEditChapterForm(p => ({ ...p, content: e.target.value }))} rows={8} placeholder="Nhập nội dung chương..." />
                              </div>
                              {story.contentType === 'COMIC' && (
                                <div className="form-group">
                                  <label>URL ảnh (mỗi dòng 1 URL)</label>
                                  <textarea value={editChapterForm.imageUrls.join('\n')} onChange={e => setEditChapterForm(p => ({ ...p, imageUrls: e.target.value.split('\n') }))} rows={3} placeholder="https://example.com/page1.jpg" />
                                </div>
                              )}
                              <div className="form-actions">
                                <button className="btn btn-primary btn-sm" onClick={() => handleUpdateChapter(editingChapter.chapterId, story.storyId)} disabled={updatingChapter}>
                                  {updatingChapter ? 'Đang lưu...' : 'Lưu thay đổi'}
                                </button>
                                <button className="btn btn-outline btn-sm" onClick={() => setEditingChapter(null)}>Hủy</button>
                              </div>
                            </div>
                          )}

                          {chapters && chapters.length > 0 ? (
                            <div className="chapters-list">
                              {chapters.map(ch => {
                                const chStatus = getStatusInfo(ch.status || 'PUBLISHED');
                                return (
                                  <div key={ch.chapterId} className="chapter-item">
                                    <span className="chapter-number">#{ch.chapterNumber}</span>
                                    <span className="chapter-title">{ch.title}</span>
                                    <span className={`status-chip ${chStatus.className}`}>{chStatus.label}</span>
                                    <div className="chapter-actions">
                                      {(!ch.status || ch.status === 'DRAFT') && (
                                        <button className="btn btn-outline btn-sm" onClick={() => handleSubmitChapter(ch.chapterId, story.storyId)}>
                                          <Send size={10} /> Gửi
                                        </button>
                                      )}
                                      <button className="btn btn-outline btn-sm" onClick={() => openEditChapterForm(ch, story.storyId)}>
                                        <Edit3 size={10} /> Sửa
                                      </button>
                                      <button className="btn btn-outline btn-sm delete-btn" onClick={() => handleDeleteChapter(ch.chapterId, story.storyId)}>
                                        <Trash2 size={10} />
                                      </button>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          ) : (
                            <div className="empty-state-sm">Chưa có chương nào.</div>
                          )}
                        </>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default CreatorDashboard;
