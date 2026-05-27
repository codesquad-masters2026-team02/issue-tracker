import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { icon } from '../lib/icons';
import { useAuth } from '../lib/auth';
import { uploadProfileImage, editProfileImage } from '../lib/api';
import './Header.css';

const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];
const MAX_SIZE = 5 * 1024 * 1024;

export function Header() {
  const navigate = useNavigate();
  const { user, logout, refreshUser } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [preview, setPreview] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const profileRef = useRef<HTMLDivElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

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

  useEffect(() => {
    if (!isModalOpen) return undefined;

    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') closeModal();
    };
    document.addEventListener('keydown', closeOnEscape);
    return () => document.removeEventListener('keydown', closeOnEscape);
  }, [isModalOpen]);

  const handleLogout = async () => {
    await logout();
    setIsProfileOpen(false);
    navigate('/login', { replace: true });
  };

  const openModal = () => {
    setIsProfileOpen(false);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setPreview(null);
    setSelectedFile(null);
    setError(null);
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setError(null);

    if (!ALLOWED_TYPES.includes(file.type)) {
      setError('PNG, JPG, GIF, WEBP 형식만 지원합니다.');
      return;
    }
    if (file.size > MAX_SIZE) {
      setError('파일 크기는 5MB 이하만 가능합니다.');
      return;
    }

    setSelectedFile(file);
    const objectUrl = URL.createObjectURL(file);
    setPreview(objectUrl);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    setIsUploading(true);
    setError(null);

    try {
      const presign = await uploadProfileImage(selectedFile);
      await editProfileImage(presign.publicUrl);
      await refreshUser();
      closeModal();
    } catch {
      setError('업로드에 실패했습니다.');
    } finally {
      setIsUploading(false);
    }
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
              <div className="profile-menu__item">
                <button type="button" className="profile-menu__edit" onClick={openModal}>
                  프로필 사진 수정
                </button>
              </div>
              <div className="profile-menu__item">
                <button type="button" className="profile-menu__logout" onClick={handleLogout}>
                  로그아웃
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {isModalOpen && (
        <div className="profile-modal-backdrop" onMouseDown={(e) => {
          if (e.target === e.currentTarget) closeModal();
        }}>
          <div className="profile-modal" ref={modalRef} role="dialog" aria-label="프로필 사진 수정">
            <h2 className="profile-modal__title">프로필 사진 수정</h2>

            <div className="profile-modal__preview">
              <img
                src={preview ?? user?.profileImageUrl ?? icon('userImageSmall')}
                alt="프로필 미리보기"
                className="profile-modal__image"
              />
            </div>

            <input
              ref={fileInputRef}
              type="file"
              accept="image/png,image/jpeg,image/gif,image/webp"
              onChange={handleFileSelect}
              hidden
            />

            <button
              type="button"
              className="profile-modal__select-btn"
              onClick={() => fileInputRef.current?.click()}
            >
              이미지 선택
            </button>

            {error && <p className="profile-modal__error">{error}</p>}

            <div className="profile-modal__actions">
              <button type="button" className="profile-modal__cancel" onClick={closeModal}>
                취소
              </button>
              <button
                type="button"
                className="profile-modal__submit"
                disabled={!selectedFile || isUploading}
                onClick={handleUpload}
              >
                {isUploading ? '업로드 중...' : '변경'}
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
