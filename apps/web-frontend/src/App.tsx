import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { IssueListPage } from './pages/IssueListPage';
import { IssueCreatePage } from './pages/IssueCreatePage';
import { IssueDetailPage } from './pages/IssueDetailPage';
import { LabelPage } from './pages/LabelPage';
import { MilestonePage } from './pages/MilestonePage';
import { LoginPage } from './pages/LoginPage';
import { GithubCallbackPage } from './pages/GithubCallbackPage';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/oauth/github/callback" element={<GithubCallbackPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<IssueListPage />} />
          <Route path="/issues/new" element={<IssueCreatePage />} />
          <Route path="/issues/:id" element={<IssueDetailPage />} />
          <Route path="/labels" element={<LabelPage />} />
          <Route path="/milestones" element={<MilestonePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
