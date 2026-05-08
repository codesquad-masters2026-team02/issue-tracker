import { useParams } from 'react-router-dom';
import { useIssueDetailQuery } from '../lib/api';
import { icon } from '../lib/icons';
import './IssueDetailPage.css';

function formatRelative(iso: string) {
  const created = new Date(iso).getTime();
  if (Number.isNaN(created)) return '';
  const diffMin = Math.max(0, Math.floor((Date.now() - created) / 60000));
  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  return `${Math.floor(diffHour / 24)}일 전`;
}

export function IssueDetailPage() {
  const params = useParams<{ id: string }>();
  const id = Number(params.id);
  const { data: issue, isLoading, isError, error } = useIssueDetailQuery(id);

  if (isLoading) return <p className="issue-detail__status">불러오는 중…</p>;
  if (isError) {
    return (
      <p className="issue-detail__status issue-detail__status--error">
        {(error as Error)?.message ?? '이슈를 불러오지 못했습니다.'}
      </p>
    );
  }
  if (!issue) return null;

  const isOpen = issue.status === 'OPEN';

  return (
    <div className="issue-detail">
      {/* 헤더 */}
      <header className="issue-detail__header">
        <h1 className="issue-detail__title">
          {issue.title}
          <span className="issue-detail__number">#{issue.issueNumber}</span>
        </h1>
        <div className="issue-detail__header-actions">
          <button type="button" className="btn btn--outline">
            <img src={icon('edit')} alt="" width={16} height={16} />
            제목 편집
          </button>
          {/* TODO: 상태 변경 API 추가되면 onClick 으로 toggle */}
          <button type="button" className="btn btn--outline" disabled title="상태 변경 API 미구현">
            {isOpen ? (
              <>
                <img src={icon('archive')} alt="" width={16} height={16} />
                이슈 닫기
              </>
            ) : (
              <>
                <img src={icon('alertCircle')} alt="" width={16} height={16} />
                이슈 열기
              </>
            )}
          </button>
        </div>
      </header>

      {/* 상태 표시줄 */}
      <div className="issue-detail__status-bar">
        <span className={`status-badge ${isOpen ? 'status-badge--open' : 'status-badge--closed'}`}>
          <img
            src={icon(isOpen ? 'checkOnCircle' : 'checkOffCircle')}
            alt=""
            width={14}
            height={14}
          />
          {isOpen ? '열린 이슈' : '닫힌 이슈'}
        </span>
        <span className="issue-detail__status-text">
          이 이슈가 {formatRelative(issue.createdAt)}에 작성되었습니다
        </span>
        <span className="issue-detail__status-text">코멘트 0개</span>
      </div>
      <hr className="issue-detail__rule" />

      <div className="issue-detail__body">
        {/* 좌: 콘텐츠 */}
        <section className="issue-detail__content">
          <article className="comment-card">
            <header className="comment-card__head">
              <img
                src={icon('userImageSmall')}
                alt=""
                width={32}
                height={32}
                className="comment-card__avatar"
              />
              <strong className="comment-card__author">작성자</strong>
              <span className="comment-card__time">{formatRelative(issue.createdAt)}</span>
              <div className="comment-card__actions">
                <span className="author-tag">작성자</span>
                <button type="button" className="comment-card__action" disabled>
                  <img src={icon('edit')} alt="" width={16} height={16} />
                  편집
                </button>
                <button type="button" className="comment-card__action" disabled>
                  <img src={icon('smile')} alt="" width={16} height={16} />
                  반응
                </button>
              </div>
            </header>
            <div className="comment-card__body">
              {/* TODO: API 의 IssueResponse 에 content 필드 추가되면 본문 렌더 */}
              <p className="comment-card__placeholder">
                본문 내용이 API 스펙에 아직 포함되어 있지 않습니다.
              </p>
            </div>
          </article>

          {/* 새 코멘트 */}
          <div className="textarea-wrap">
            <textarea
              className="text-area"
              placeholder="코멘트를 입력하세요"
              disabled
            />
            <hr className="textarea-wrap__divider" />
            <button type="button" className="textarea-wrap__attach" disabled>
              <img src={icon('paperclip')} alt="" width={16} height={16} />
              파일 첨부하기
            </button>
          </div>
          <div className="new-comment__footer">
            <button type="button" className="btn btn--primary" disabled title="코멘트 API 미구현">
              <img src={icon('plus')} alt="" width={16} height={16} />
              코멘트 작성
            </button>
          </div>
        </section>

        {/* 우: 사이드바 (단일 카드) + 이슈 삭제 버튼 */}
        <aside className="issue-detail__aside">
          <div className="sidebar-card">
            {(['담당자', '레이블'] as const).map((label) => (
              <div key={label} className="sidebar-card__section">
                <div className="sidebar-card__head">
                  <span>{label}</span>
                  <button type="button" aria-label={`${label} 변경`} className="sidebar-card__plus">
                    <img src={icon('plus')} alt="" width={16} height={16} />
                  </button>
                </div>
              </div>
            ))}
            <div className="sidebar-card__section">
              <div className="sidebar-card__head">
                <span>마일스톤</span>
                <button type="button" aria-label="마일스톤 변경" className="sidebar-card__plus">
                  <img src={icon('plus')} alt="" width={16} height={16} />
                </button>
              </div>
              <div className="progress">
                <div className="progress__track">
                  <div className="progress__fill" style={{ width: '0%' }} />
                </div>
                <span className="progress__label">0%</span>
              </div>
            </div>
          </div>

          <button
            type="button"
            className="btn btn--danger issue-detail__delete"
            disabled
            title="삭제 API 미구현"
          >
            <img src={icon('trash')} alt="" width={16} height={16} />
            이슈 삭제
          </button>
        </aside>
      </div>
    </div>
  );
}
