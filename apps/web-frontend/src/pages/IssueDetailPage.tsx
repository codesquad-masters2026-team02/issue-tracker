import { useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import Markdown, { defaultUrlTransform } from 'react-markdown';
import { AttachableTextarea } from '../components/AttachableTextarea';
import { LabelBadge } from '../components/LabelBadge';
import {
  fetchAttachmentPresignedUrl,
  getApiErrorMessage,
  useAddIssueAssigneesMutation,
  useAddIssueLabelsMutation,
  useCreateCommentMutation,
  useDeleteCommentMutation,
  useIssueCommentsQuery,
  useIssueDetailQuery,
  useLabelListQuery,
  useMilestoneListQuery,
  useRemoveIssueAssigneesMutation,
  useRemoveIssueLabelsMutation,
  useRemoveIssueMilestoneMutation,
  useSetIssueMilestoneMutation,
  useUpdateIssueStatusMutation,
  useUpdateIssueTitleMutation,
  useUserListQuery,
  type CommentResponse,
  type IssueStatus,
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

const ATTACHMENT_PREFIX = 'attachment:';

function AuthFileLink({ attachmentId, filename }: { attachmentId: string; filename: string }) {
  const [pending, setPending] = useState(false);

  const handleClick = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (pending) return;
    setPending(true);
    try {
      const presignedUrl = await fetchAttachmentPresignedUrl(attachmentId);
      const a = document.createElement('a');
      a.href = presignedUrl;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } finally {
      setPending(false);
    }
  };

  return (
    <a href="#" onClick={handleClick} className="comment-content__file">
      <img src={icon('paperclip')} alt="" width={14} height={14} />
      {pending ? '다운로드 중…' : filename}
    </a>
  );
}

function AuthImage({ attachmentId, alt }: { attachmentId: string; alt: string }) {
  const [src, setSrc] = useState<string | null>(null);

  useEffect(() => {
    fetchAttachmentPresignedUrl(attachmentId)
      .then(setSrc)
      .catch(() => setSrc(null));
  }, [attachmentId]);

  if (!src) return <span className="comment-content__image-placeholder" />;
  return (
    <a href={src} target="_blank" rel="noopener noreferrer">
      <img src={src} alt={alt} className="comment-content__image" />
    </a>
  );
}

const markdownComponents = {
  img: ({ src, alt }: { src?: string; alt?: string }) => {
    if (src?.startsWith(ATTACHMENT_PREFIX)) {
      const id = src.slice(ATTACHMENT_PREFIX.length);
      return <AuthImage attachmentId={id} alt={alt ?? ''} />;
    }
    return <img src={src} alt={alt} className="comment-content__image" />;
  },
  a: ({ href, children }: { href?: string; children?: React.ReactNode }) => {
    if (href?.startsWith(ATTACHMENT_PREFIX)) {
      const id = href.slice(ATTACHMENT_PREFIX.length);
      return <AuthFileLink attachmentId={id} filename={String(children)} />;
    }
    return <a href={href} target="_blank" rel="noopener noreferrer">{children}</a>;
  },
};

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
          {comment.username}
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
          <div className="comment-card__content">
            <Markdown
              components={markdownComponents}
              urlTransform={(url) => url.startsWith(ATTACHMENT_PREFIX) ? url : defaultUrlTransform(url)}
            >
              {comment.content}
            </Markdown>
          </div>
        ) : (
          <p className="comment-card__placeholder">내용이 없습니다.</p>
        )}
      </div>
    </article>
  );
}

type SidebarMenu = 'assignees' | 'labels' | 'milestone' | null;

export function IssueDetailPage() {
  const params = useParams<{ id: string }>();
  const id = Number(params.id);
  const { data: issue, isLoading, isError, error } = useIssueDetailQuery(id);
  const { data: allUsers = [], isLoading: isUsersLoading, isError: isUsersError } = useUserListQuery();
  const { data: allLabels = [], isLoading: isLabelsLoading, isError: isLabelsError } = useLabelListQuery();
  const {
    data: milestoneList,
    isLoading: isMilestonesLoading,
    isError: isMilestonesError,
  } = useMilestoneListQuery('OPEN');
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
  const addAssignees = useAddIssueAssigneesMutation(id);
  const removeAssignees = useRemoveIssueAssigneesMutation(id);
  const addLabels = useAddIssueLabelsMutation(id);
  const removeLabels = useRemoveIssueLabelsMutation(id);
  const setMilestone = useSetIssueMilestoneMutation(id);
  const removeMilestone = useRemoveIssueMilestoneMutation(id);
  const updateIssueStatus = useUpdateIssueStatusMutation(id);
  const updateIssueTitle = useUpdateIssueTitleMutation(id);
  const [newComment, setNewComment] = useState('');
  const [newCommentAttachmentIds, setNewCommentAttachmentIds] = useState<string[]>([]);
  const [deletingCommentId, setDeletingCommentId] = useState<number | null>(null);
  const [openSidebarMenu, setOpenSidebarMenu] = useState<SidebarMenu>(null);
  const [isTitleEditing, setIsTitleEditing] = useState(false);
  const [titleDraft, setTitleDraft] = useState('');
  const sidebarRef = useRef<HTMLElement>(null);

  const { issueBodyComment, discussionComments } = useMemo(() => {
    const comments = commentList?.comments ?? [];
    return {
      issueBodyComment: comments.find((comment) => comment.type === 'ISSUE_BODY'),
      discussionComments: comments.filter((comment) => comment.type === 'DISCUSSION'),
    };
  }, [commentList]);

  const canSubmitComment = newComment.trim().length > 0 && !isCommentPending;
  const sortedUsers = useMemo(
    () => [...allUsers].sort((a, b) => a.username.localeCompare(b.username, 'ko')),
    [allUsers],
  );
  const sortedAllLabels = useMemo(
    () => [...allLabels].sort((a, b) => a.name.localeCompare(b.name, 'ko')),
    [allLabels],
  );
  const sortedMilestones = useMemo(
    () => [...(milestoneList?.milestones ?? [])].sort((a, b) => a.name.localeCompare(b.name, 'ko')),
    [milestoneList],
  );

  useEffect(() => {
    if (!openSidebarMenu) return;

    const closeOnOutsideClick = (event: MouseEvent) => {
      const target = event.target;
      if (
        target instanceof Node
        && sidebarRef.current
        && !sidebarRef.current.contains(target)
      ) {
        setOpenSidebarMenu(null);
      }
    };

    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, [openSidebarMenu]);

  useEffect(() => {
    if (!issue || isTitleEditing) return;
    setTitleDraft(issue.title);
  }, [isTitleEditing, issue]);

  const handleAttach = (_publicUrl: string, filename: string, attachmentId: string) => {
    const isImage = /\.(png|jpe?g|gif|webp)$/i.test(filename);
    const marker = isImage
      ? `![${filename}](attachment:${attachmentId})`
      : `[${filename}](attachment:${attachmentId})`;
    setNewComment((prev) => `${prev}${prev ? '\n' : ''}${marker}`);
    setNewCommentAttachmentIds((prev) => [...prev, attachmentId]);
  };

  const handleCommentSubmit = () => {
    if (!canSubmitComment) return;
    createComment(
      {
        content: newComment.trim(),
        attachmentIds: newCommentAttachmentIds.length > 0 ? newCommentAttachmentIds : undefined,
      },
      {
        onSuccess: () => {
          setNewComment('');
          setNewCommentAttachmentIds([]);
        },
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

  const sidebarMutationError = addAssignees.error
    ?? removeAssignees.error
    ?? addLabels.error
    ?? removeLabels.error
    ?? setMilestone.error
    ?? removeMilestone.error;
  const issueStatusError = updateIssueStatus.error;
  const issueTitleError = updateIssueTitle.error;

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
  const labels = issue.labels ?? [];
  const assignees = issue.assignees ?? [];
  const milestone = issue.milestone ?? null;
  const assignedUserIds = new Set(assignees.map((assignee) => assignee.id));
  const selectedLabelIds = new Set(labels.map((label) => label.labelId));
  const milestoneTotalCount = milestone
    ? milestone.openIssueCount + milestone.closedIssueCount
    : 0;
  const milestoneProgress = milestoneTotalCount > 0 && milestone
    ? Math.round((milestone.closedIssueCount / milestoneTotalCount) * 100)
    : 0;
  const isAssigneeUpdating = addAssignees.isPending || removeAssignees.isPending;
  const isLabelUpdating = addLabels.isPending || removeLabels.isPending;
  const isMilestoneUpdating = setMilestone.isPending || removeMilestone.isPending;
  const isIssueStatusUpdating = updateIssueStatus.isPending;
  const isIssueTitleUpdating = updateIssueTitle.isPending;
  const canSaveTitle = titleDraft.trim().length > 0
    && titleDraft.trim() !== issue.title
    && !isIssueTitleUpdating;

  const handleTitleEditStart = () => {
    setTitleDraft(issue.title);
    setIsTitleEditing(true);
  };

  const handleTitleEditCancel = () => {
    setTitleDraft(issue.title);
    setIsTitleEditing(false);
  };

  const handleTitleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canSaveTitle) return;
    updateIssueTitle.mutate(
      { title: titleDraft.trim() },
      { onSuccess: () => setIsTitleEditing(false) },
    );
  };

  const handleIssueStatusToggle = () => {
    if (isIssueStatusUpdating) return;
    const nextStatus: IssueStatus = isOpen ? 'CLOSED' : 'OPEN';
    updateIssueStatus.mutate({ status: nextStatus });
  };

  const handleAssigneeToggle = (userId: number) => {
    if (isAssigneeUpdating) return;
    if (assignedUserIds.has(userId)) {
      removeAssignees.mutate([userId]);
    } else {
      addAssignees.mutate([userId]);
    }
  };

  const handleLabelToggle = (labelId: number) => {
    if (isLabelUpdating) return;
    if (selectedLabelIds.has(labelId)) {
      removeLabels.mutate([labelId]);
    } else {
      addLabels.mutate([labelId]);
    }
  };

  const handleMilestoneSelect = (milestoneId: number) => {
    if (isMilestoneUpdating) return;
    setMilestone.mutate(milestoneId, {
      onSuccess: () => setOpenSidebarMenu(null),
    });
  };

  const handleMilestoneRemove = () => {
    if (isMilestoneUpdating) return;
    removeMilestone.mutate(undefined, {
      onSuccess: () => setOpenSidebarMenu(null),
    });
  };

  return (
    <div className="issue-detail">
      {/* 헤더 */}
      <header className="issue-detail__header">
        {isTitleEditing ? (
          <form className="issue-detail__title-form" onSubmit={handleTitleSubmit}>
            <input
              className="issue-detail__title-input"
              value={titleDraft}
              disabled={isIssueTitleUpdating}
              autoFocus
              onChange={(event) => setTitleDraft(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Escape') {
                  handleTitleEditCancel();
                }
              }}
            />
            <span className="issue-detail__number">#{issue.issueNumber}</span>
            <div className="issue-detail__title-actions">
              <button type="submit" className="btn btn--primary" disabled={!canSaveTitle}>
                {isIssueTitleUpdating ? '저장 중…' : '저장'}
              </button>
              <button
                type="button"
                className="btn btn--outline"
                disabled={isIssueTitleUpdating}
                onClick={handleTitleEditCancel}
              >
                취소
              </button>
            </div>
          </form>
        ) : (
          <h1 className="issue-detail__title">
            {issue.title}
            <span className="issue-detail__number">#{issue.issueNumber}</span>
          </h1>
        )}
        <div className="issue-detail__header-actions">
          <button
            type="button"
            className="btn btn--outline"
            disabled={isTitleEditing || isIssueTitleUpdating}
            onClick={handleTitleEditStart}
          >
            <img src={icon('edit')} alt="" width={16} height={16} />
            제목 편집
          </button>
          <button
            type="button"
            className="btn btn--outline"
            disabled={isIssueStatusUpdating}
            onClick={handleIssueStatusToggle}
          >
            {isOpen ? (
              <>
                <img src={icon('archive')} alt="" width={16} height={16} />
                {isIssueStatusUpdating ? '닫는 중…' : '이슈 닫기'}
              </>
            ) : (
              <>
                <img src={icon('alertCircle')} alt="" width={16} height={16} />
                {isIssueStatusUpdating ? '여는 중…' : '이슈 열기'}
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
          이 이슈가 {formatRelative(issue.createdAt)}에 {issue.authorUsername}님에 의해 작성되었습니다
        </span>
        <span className="issue-detail__status-text">
          코멘트 {discussionComments.length}개
        </span>
      </div>
      <hr className="issue-detail__rule" />
      {issueStatusError && (
        <p className="issue-detail__status-error">
          {getApiErrorMessage(issueStatusError, '이슈 상태를 수정하지 못했습니다.')}
        </p>
      )}
      {issueTitleError && (
        <p className="issue-detail__status-error">
          {getApiErrorMessage(issueTitleError, '이슈 제목을 수정하지 못했습니다.')}
        </p>
      )}

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
              {getApiErrorMessage(deleteCommentError, '코멘트를 삭제하지 못했습니다.')}
            </p>
          )}

          {/* 새 코멘트 */}
          <AttachableTextarea
            value={newComment}
            onChange={setNewComment}
            onAttach={handleAttach}
            placeholder="코멘트를 입력하세요"
            disabled={isCommentPending}
          />
          {createCommentError && (
            <p className="new-comment__error">
              {getApiErrorMessage(createCommentError, '코멘트를 작성하지 못했습니다.')}
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
        <aside className="issue-detail__aside" ref={sidebarRef}>
          <div className="sidebar-card">
            <div className="sidebar-card__section sidebar-card__section--dropdown">
              <div className="sidebar-card__head">
                <span>담당자</span>
                <button
                  type="button"
                  aria-label="담당자 변경"
                  aria-expanded={openSidebarMenu === 'assignees'}
                  className="sidebar-card__plus"
                  onClick={() => setOpenSidebarMenu((open) => (open === 'assignees' ? null : 'assignees'))}
                >
                  <img src={icon('plus')} alt="" width={16} height={16} />
                </button>
              </div>
              {assignees.length > 0 ? (
                <div className="sidebar-card__items">
                  {assignees.map((assignee) => (
                    <div key={assignee.id} className="sidebar-user">
                      <img
                        src={assignee.profileImageUrl || icon('userImageSmall')}
                        alt=""
                        width={24}
                        height={24}
                        className="sidebar-user__avatar"
                      />
                      <span>{assignee.username}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="sidebar-card__placeholder">담당자가 없습니다.</p>
              )}
              {openSidebarMenu === 'assignees' && (
                <div className="sidebar-picker" role="listbox" aria-multiselectable="true">
                  <div className="sidebar-picker__head">담당자 선택</div>
                  {isUsersLoading && <p className="sidebar-picker__status">불러오는 중…</p>}
                  {isUsersError && (
                    <p className="sidebar-picker__status sidebar-picker__status--error">
                      담당자 목록을 불러오지 못했습니다.
                    </p>
                  )}
                  {!isUsersLoading && !isUsersError && sortedUsers.length === 0 && (
                    <p className="sidebar-picker__status">등록된 사용자가 없습니다.</p>
                  )}
                  {sortedUsers.map((user) => {
                    const isSelected = assignedUserIds.has(user.id);
                    return (
                      <button
                        key={user.id}
                        type="button"
                        className="sidebar-picker__item"
                        role="option"
                        aria-selected={isSelected}
                        disabled={isAssigneeUpdating}
                        onClick={() => handleAssigneeToggle(user.id)}
                      >
                        <img
                          src={icon(isSelected ? 'checkBoxActive' : 'checkBoxInitial')}
                          alt=""
                          width={16}
                          height={16}
                        />
                        <span className="sidebar-picker__user">
                          <img
                            src={user.profileImageUrl || icon('userImageSmall')}
                            alt=""
                            width={24}
                            height={24}
                          />
                          {user.username}
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
            <div className="sidebar-card__section sidebar-card__section--dropdown">
              <div className="sidebar-card__head">
                <span>레이블</span>
                <button
                  type="button"
                  aria-label="레이블 변경"
                  aria-expanded={openSidebarMenu === 'labels'}
                  className="sidebar-card__plus"
                  onClick={() => setOpenSidebarMenu((open) => (open === 'labels' ? null : 'labels'))}
                >
                  <img src={icon('plus')} alt="" width={16} height={16} />
                </button>
              </div>
              <div className="sidebar-card__items sidebar-card__items--labels">
                {labels.length > 0 ? (
                  labels.map((label) => (
                    <LabelBadge key={label.labelId} label={label} />
                  ))
                ) : (
                  <p className="sidebar-card__placeholder">레이블이 없습니다.</p>
                )}
              </div>
              {openSidebarMenu === 'labels' && (
                <div className="sidebar-picker" role="listbox" aria-multiselectable="true">
                  <div className="sidebar-picker__head">레이블 선택</div>
                  {isLabelsLoading && <p className="sidebar-picker__status">불러오는 중…</p>}
                  {isLabelsError && (
                    <p className="sidebar-picker__status sidebar-picker__status--error">
                      레이블을 불러오지 못했습니다.
                    </p>
                  )}
                  {!isLabelsLoading && !isLabelsError && sortedAllLabels.length === 0 && (
                    <p className="sidebar-picker__status">등록된 레이블이 없습니다.</p>
                  )}
                  {sortedAllLabels.map((label) => {
                    const isSelected = selectedLabelIds.has(label.labelId);
                    return (
                      <button
                        key={label.labelId}
                        type="button"
                        className="sidebar-picker__item"
                        role="option"
                        aria-selected={isSelected}
                        disabled={isLabelUpdating}
                        onClick={() => handleLabelToggle(label.labelId)}
                      >
                        <img
                          src={icon(isSelected ? 'checkBoxActive' : 'checkBoxInitial')}
                          alt=""
                          width={16}
                          height={16}
                        />
                        <LabelBadge label={label} />
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
            <div className="sidebar-card__section sidebar-card__section--dropdown">
              <div className="sidebar-card__head">
                <span>마일스톤</span>
                <button
                  type="button"
                  aria-label="마일스톤 변경"
                  aria-expanded={openSidebarMenu === 'milestone'}
                  className="sidebar-card__plus"
                  onClick={() => setOpenSidebarMenu((open) => (open === 'milestone' ? null : 'milestone'))}
                >
                  <img src={icon('plus')} alt="" width={16} height={16} />
                </button>
              </div>
              {milestone ? (
                <>
                  <div className="sidebar-milestone">
                    <img src={icon('milestone')} alt="" width={16} height={16} />
                    <span>{milestone.name}</span>
                  </div>
                  <div className="progress">
                    <div className="progress__track">
                      <div className="progress__fill" style={{ width: `${milestoneProgress}%` }} />
                    </div>
                    <span className="progress__label">{milestoneProgress}%</span>
                  </div>
                  <p className="sidebar-milestone__counts">
                    열린 이슈 {milestone.openIssueCount}개 · 닫힌 이슈 {milestone.closedIssueCount}개
                  </p>
                </>
              ) : (
                <p className="sidebar-card__placeholder">마일스톤이 없습니다.</p>
              )}
              {openSidebarMenu === 'milestone' && (
                <div className="sidebar-picker" role="listbox">
                  <div className="sidebar-picker__head">마일스톤 선택</div>
                  {milestone && (
                    <button
                      type="button"
                      className="sidebar-picker__item sidebar-picker__item--danger"
                      disabled={isMilestoneUpdating}
                      onClick={handleMilestoneRemove}
                    >
                      <img src={icon('xSquare')} alt="" width={16} height={16} />
                      마일스톤 제거
                    </button>
                  )}
                  {isMilestonesLoading && <p className="sidebar-picker__status">불러오는 중…</p>}
                  {isMilestonesError && (
                    <p className="sidebar-picker__status sidebar-picker__status--error">
                      마일스톤을 불러오지 못했습니다.
                    </p>
                  )}
                  {!isMilestonesLoading && !isMilestonesError && sortedMilestones.length === 0 && (
                    <p className="sidebar-picker__status">열린 마일스톤이 없습니다.</p>
                  )}
                  {sortedMilestones.map((candidate) => {
                    const isSelected = milestone?.id === candidate.id;
                    return (
                      <button
                        key={candidate.id}
                        type="button"
                        className="sidebar-picker__item"
                        role="option"
                        aria-selected={isSelected}
                        disabled={isMilestoneUpdating}
                        onClick={() => handleMilestoneSelect(candidate.id)}
                      >
                        <img
                          src={icon(isSelected ? 'checkBoxActive' : 'checkBoxInitial')}
                          alt=""
                          width={16}
                          height={16}
                        />
                        <span className="sidebar-picker__milestone">
                          <strong>{candidate.name}</strong>
                          <span>
                            열린 이슈 {candidate.openIssueCount ?? 0}개 · 닫힌 이슈 {candidate.closedIssueCount ?? 0}개
                          </span>
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
          {sidebarMutationError && (
            <p className="issue-detail__sidebar-error">
              {getApiErrorMessage(sidebarMutationError, '사이드바 정보를 수정하지 못했습니다.')}
            </p>
          )}

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
