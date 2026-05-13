import { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  useCreateCommentMutation,
  useDeleteCommentMutation,
  useIssueCommentsQuery,
  useIssueDetailQuery,
  type CommentResponse,
} from '../lib/api';
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

interface CommentCardProps {
  comment: CommentResponse;
  isIssueBody?: boolean;
  isDeleting?: boolean;
  onDelete?: (comment: CommentResponse) => void;
}

function CommentCard({
  comment,
  isIssueBody = false,
  isDeleting = false,
  onDelete,
}: CommentCardProps) {
  return (
    <article className="comment-card">
      <header className="comment-card__head">
        <img
          src={icon('userImageSmall')}
          alt=""
          width={32}
          height={32}
          className="comment-card__avatar"
        />
        <strong className="comment-card__author">
          {isIssueBody ? '작성자' : '댓글 작성자'}
        </strong>
        <span className="comment-card__time">{formatRelative(comment.created_at)}</span>
        <div className="comment-card__actions">
          {isIssueBody && <span className="author-tag">작성자</span>}
          <button type="button" className="comment-card__action" disabled>
            <img src={icon('edit')} alt="" width={16} height={16} />
            편집
          </button>
          <button type="button" className="comment-card__action" disabled>
            <img src={icon('smile')} alt="" width={16} height={16} />
            반응
          </button>
          {!isIssueBody && onDelete && (
            <button
              type="button"
              className="comment-card__action comment-card__action--danger"
              disabled={isDeleting}
              onClick={() => onDelete(comment)}
            >
              <img src={icon('trash')} alt="" width={16} height={16} />
              {isDeleting ? '삭제 중…' : '삭제'}
            </button>
          )}
        </div>
      </header>
      <div className="comment-card__body">
        {comment.content.trim() ? (
          <p className="comment-card__content">{comment.content}</p>
        ) : (
          <p className="comment-card__placeholder">내용이 없습니다.</p>
        )}
      </div>
    </article>
  );
}

export function IssueDetailPage() {
  const params = useParams<{ id: string }>();
  const id = Number(params.id);
  const { data: issue, isLoading, isError, error } = useIssueDetailQuery(id);
  const {
    data: commentList,
    isLoading: isCommentsLoading,
    isError: isCommentsError,
    error: commentsError,
  } = useIssueCommentsQuery(id);
  const {
    mutate: createComment,
    isPending: isCommentPending,
    error: createCommentError,
  } = useCreateCommentMutation(id);
  const {
    mutate: deleteComment,
    isPending: isDeletePending,
    error: deleteCommentError,
  } = useDeleteCommentMutation(id);
  const [newComment, setNewComment] = useState('');
  const [deletingCommentId, setDeletingCommentId] = useState<number | null>(null);

  const { issueBodyComment, discussionComments } = useMemo(() => {
    const comments = commentList?.comments ?? [];
    return {
      issueBodyComment: comments.find((comment) => comment.type === 'ISSUE_BODY'),
      discussionComments: comments.filter((comment) => comment.type === 'DISCUSSION'),
    };
  }, [commentList]);

  const canSubmitComment = newComment.trim().length > 0 && !isCommentPending;

  const handleCommentSubmit = () => {
    if (!canSubmitComment) return;
    createComment(
      { content: newComment.trim() },
      {
        onSuccess: () => setNewComment(''),
      },
    );
  };

  const handleCommentDelete = (comment: CommentResponse) => {
    if (isDeletePending) return;
    if (!window.confirm('코멘트를 삭제하시겠습니까?')) return;

    setDeletingCommentId(comment.id);
    deleteComment(comment.id, {
      onSettled: () => setDeletingCommentId(null),
    });
  };

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
        <span className="issue-detail__status-text">
          코멘트 {discussionComments.length}개
        </span>
      </div>
      <hr className="issue-detail__rule" />

      <div className="issue-detail__body">
        {/* 좌: 콘텐츠 */}
        <section className="issue-detail__content">
          {isCommentsLoading && (
            <p className="issue-detail__status">코멘트를 불러오는 중…</p>
          )}
          {isCommentsError && (
            <p className="issue-detail__status issue-detail__status--error">
              {(commentsError as Error)?.message ?? '코멘트를 불러오지 못했습니다.'}
            </p>
          )}
          {!isCommentsLoading && !isCommentsError && issueBodyComment && (
            <CommentCard comment={issueBodyComment} isIssueBody />
          )}
          {!isCommentsLoading && !isCommentsError && !issueBodyComment && (
            <article className="comment-card">
              <div className="comment-card__body">
                <p className="comment-card__placeholder">본문 내용이 없습니다.</p>
              </div>
            </article>
          )}
          {!isCommentsLoading && !isCommentsError && discussionComments.map((comment) => (
            <CommentCard
              key={comment.id}
              comment={comment}
              isDeleting={deletingCommentId === comment.id}
              onDelete={handleCommentDelete}
            />
          ))}
          {deleteCommentError && (
            <p className="comment-thread__error">
              {(deleteCommentError as Error).message}
            </p>
          )}

          {/* 새 코멘트 */}
          <div className="textarea-wrap">
            <textarea
              className="text-area"
              placeholder="코멘트를 입력하세요"
              value={newComment}
              disabled={isCommentPending}
              onChange={(e) => setNewComment(e.target.value)}
            />
            {newComment.length > 0 && (
              <div className="textarea-wrap__counter">
                띄어쓰기 포함 {newComment.length}자
              </div>
            )}
            <hr className="textarea-wrap__divider" />
            <button type="button" className="textarea-wrap__attach" disabled>
              <img src={icon('paperclip')} alt="" width={16} height={16} />
              파일 첨부하기
            </button>
          </div>
          {createCommentError && (
            <p className="new-comment__error">
              {(createCommentError as Error).message}
            </p>
          )}
          <div className="new-comment__footer">
            <button
              type="button"
              className="btn btn--primary"
              disabled={!canSubmitComment}
              onClick={handleCommentSubmit}
            >
              <img src={icon('plus')} alt="" width={16} height={16} />
              {isCommentPending ? '작성 중…' : '코멘트 작성'}
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
