import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import MainLayout from './components/layout/MainLayout';
import Home from './pages/Home/Home';
import StoryDetail from './pages/StoryDetail/StoryDetail';
import Reading from './pages/Reading/Reading';
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import Hot from './pages/Hot/Hot';
import History from './pages/History/History';
import Following from './pages/Following/Following';
import CategoryPage from './pages/Category/CategoryPage';
import SearchResults from './pages/Search/SearchResults';
import Profile from './pages/Profile/Profile';
import CreatorDashboard from './pages/CreatorDashboard/CreatorDashboard';
import NotificationPage from './pages/Notifications/NotificationPage';
import AdminDashboard from './pages/Admin/AdminDashboard';
import CollectionsPage from './pages/Collections/CollectionsPage';
import AuthorPage from './pages/Author/AuthorPage';
import NotFound from './pages/NotFound/NotFound';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="story/:id" element={<StoryDetail />} />
            <Route path="read/:id/:chapter" element={<Reading />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route path="hot" element={<Hot />} />
            <Route path="history" element={<History />} />
            <Route path="following" element={<Following />} />
            <Route path="category/:id" element={<CategoryPage />} />
            <Route path="search" element={<SearchResults />} />
            <Route path="profile" element={<Profile />} />
            <Route path="notifications" element={<NotificationPage />} />
            <Route path="creator" element={<CreatorDashboard />} />
            <Route path="admin" element={<AdminDashboard />} />
            <Route path="collections" element={<CollectionsPage />} />
            <Route path="author/:name" element={<AuthorPage />} />
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
