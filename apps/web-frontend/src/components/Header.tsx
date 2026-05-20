import { Link } from 'react-router-dom';
import { icon } from '../lib/icons';
import { useAuth } from '../lib/auth';
import './Header.css';

export function Header() {
  const { user } = useAuth();

  return (
    <header className="app-header">
      <div className="app-header__inner">
        <Link to="/" className="app-header__logo">Issue Tracker</Link>
        <div className="app-header__user">
          <span className="app-header__username">{user?.username}</span>
          <img
            src={user?.profileImageUrl ?? icon('userImageSmall')}
            alt=""
            className="app-header__avatar"
            width={32}
            height={32}
          />
        </div>
      </div>
    </header>
  );
}
