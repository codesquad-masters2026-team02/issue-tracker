import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { IssueListPage } from './pages/IssueListPage';
import { IssueCreatePage } from './pages/IssueCreatePage';
import { IssueDetailPage } from './pages/IssueDetailPage';
import { LabelPage } from './pages/LabelPage';

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<IssueListPage />} />
        <Route path="/issues/new" element={<IssueCreatePage />} />
        <Route path="/issues/:id" element={<IssueDetailPage />} />
        <Route path="/labels" element={<LabelPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
