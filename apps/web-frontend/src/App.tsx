import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { IssueListPage } from './pages/IssueListPage';
import { IssueCreatePage } from './pages/IssueCreatePage';
import { IssueDetailPage } from './pages/IssueDetailPage';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<IssueListPage />} />
        <Route path="/issues/new" element={<IssueCreatePage />} />
        <Route path="/issues/:id" element={<IssueDetailPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
