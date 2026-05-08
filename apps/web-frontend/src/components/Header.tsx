import { Link } from 'react-router-dom';
import { icon } from '../lib/icons';
import './Header.css';

export function Header() {
  return (
    <header className="app-header">
      <div className="app-header__inner">
        <Link to="/" className="app-header__logo">Issue Tracker</Link>
        <img
          src={icon('userImageSmall')}
          alt="user avatar"
          className="app-header__avatar"
          width={32}
          height={32}
        />
      </div>
    </header>
  );
}
