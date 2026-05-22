import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { icon } from '../lib/icons';
import { useAuth } from '../lib/auth';
import './Header.css';

export function Header() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isProfileOpen) return undefined;

    const closeOnOutsideClick = (event: MouseEvent) => {
      if (!profileRef.current?.contains(event.target as Node)) {
        setIsProfileOpen(false);
      }
    };
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setIsProfileOpen(false);
    };

    document.addEventListener('mousedown', closeOnOutsideClick);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideClick);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [isProfileOpen]);

  const handleLogout = async () => {
    await logout();
    setIsProfileOpen(false);
    navigate('/login', { replace: true });
  };

  return (
    <header className="app-header">
      <div className="app-header__inner">
        <Link to="/" className="app-header__logo">Issue Tracker</Link>
        <div className="app-header__user" ref={profileRef}>
          <span className="app-header__username">{user?.username}</span>
          <button
            type="button"
            className="app-header__avatar-button"
            aria-label="프로필 메뉴 열기"
            aria-expanded={isProfileOpen}
            onClick={() => setIsProfileOpen((current) => !current)}
          >
            <img
              src={user?.profileImageUrl ?? icon('userImageSmall')}
              alt=""
              className="app-header__avatar"
              width={32}
              height={32}
            />
          </button>
          {isProfileOpen && (
            <div className="profile-menu" role="dialog" aria-label="프로필 메뉴">
              <div className="profile-menu__row">
                <span className="profile-menu__label">사용자</span>
                <strong className="profile-menu__value">{user?.username}</strong>
              </div>
              <button type="button" className="profile-menu__logout" onClick={handleLogout}>
                로그아웃
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
