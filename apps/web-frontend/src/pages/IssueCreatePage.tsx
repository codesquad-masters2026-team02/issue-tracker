import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateIssueMutation } from '../lib/api';
import { icon } from '../lib/icons';
import './IssueCreatePage.css';

export function IssueCreatePage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useCreateIssueMutation();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  const canSubmit = title.trim().length > 0 && !isPending;

  const handleSubmit = () => {
    if (!canSubmit) return;
    mutate(
      { title: title.trim(), content },
      {
        onSuccess: (issue) => navigate(`/issues/${issue.issueNumber}`),
      },
    );
  };

  return (
    <div className="issue-create">
      <h1 className="issue-create__heading">새로운 이슈 작성</h1>
      <hr className="issue-create__rule" />

      <div className="issue-create__body">
        <section className="issue-create__main">
          <img
            src={icon('userImageLarge')}
            alt=""
            className="issue-create__avatar"
            width={48}
            height={48}
          />
          <div className="issue-create__form">
            <input
              className="text-input"
              placeholder="제목"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />

            <div className="textarea-wrap">
              <textarea
                className="text-area"
                placeholder="코멘트를 입력하세요"
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
              {content.length > 0 && (
                <div className="textarea-wrap__counter">
                  띄어쓰기 포함 {content.length}자
                </div>
              )}
              <hr className="textarea-wrap__divider" />
              <button type="button" className="textarea-wrap__attach">
                <img src={icon('paperclip')} alt="" width={16} height={16} />
                파일 첨부하기
              </button>
            </div>
          </div>
        </section>

        {/* 사이드바: 하나의 카드 안에 3개 섹션 */}
        <aside className="sidebar-card">
          {(['담당자', '레이블', '마일스톤'] as const).map((label) => (
            <div key={label} className="sidebar-card__section">
              <div className="sidebar-card__head">
                <span>{label}</span>
                <button type="button" aria-label={`${label} 추가`} className="sidebar-card__plus">
                  <img src={icon('plus')} alt="" width={16} height={16} />
                </button>
              </div>
            </div>
          ))}
        </aside>
      </div>

      {error && (
        <p className="issue-create__error">
          {(error as Error).message}
        </p>
      )}

      <hr className="issue-create__rule" />
      <div className="issue-create__footer">
        <button
          type="button"
          className="btn btn--ghost"
          onClick={() => navigate(-1)}
        >
          <img src={icon('xSquare')} alt="" width={16} height={16} />
          작성 취소
        </button>
        <button
          type="button"
          className="btn btn--primary"
          disabled={!canSubmit}
          onClick={handleSubmit}
        >
          {isPending ? '저장 중…' : '완료'}
        </button>
      </div>
    </div>
  );
}
