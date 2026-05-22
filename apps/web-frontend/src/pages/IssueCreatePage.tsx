import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AttachableTextarea } from '../components/AttachableTextarea';
import { LabelBadge } from '../components/LabelBadge';
import {
  useCreateIssueMutation,
  useLabelListQuery,
  useMilestoneListQuery,
  useUserListQuery,
  type LabelResponse,
} from '../lib/api';
import { icon } from '../lib/icons';
import './IssueCreatePage.css';

function sortLabels(labels: LabelResponse[]) {
  return [...labels].sort((a, b) => a.name.localeCompare(b.name, 'ko'));
}

type SidebarMenu = 'assignees' | 'labels' | 'milestone' | null;

export function IssueCreatePage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useCreateIssueMutation();
  const {
    data: users = [],
    isLoading: isUsersLoading,
    isError: isUsersError,
  } = useUserListQuery();
  const {
    data: labels = [],
    isLoading: isLabelsLoading,
    isError: isLabelsError,
  } = useLabelListQuery();
  const {
    data: milestoneList,
    isLoading: isMilestonesLoading,
    isError: isMilestonesError,
  } = useMilestoneListQuery('OPEN');

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [attachmentIds, setAttachmentIds] = useState<string[]>([]);
  const [selectedAssigneeIds, setSelectedAssigneeIds] = useState<number[]>([]);
  const [selectedLabelIds, setSelectedLabelIds] = useState<number[]>([]);
  const [selectedMilestoneId, setSelectedMilestoneId] = useState<number | null>(null);
  const [openSidebarMenu, setOpenSidebarMenu] = useState<SidebarMenu>(null);
  const sidebarRef = useRef<HTMLElement>(null);

  const canSubmit = title.trim().length > 0 && !isPending;
  const sortedUsers = [...users].sort((a, b) => a.username.localeCompare(b.username, 'ko'));
  const sortedLabels = sortLabels(labels);
  const sortedMilestones = [...(milestoneList?.milestones ?? [])]
    .sort((a, b) => a.name.localeCompare(b.name, 'ko'));
  const selectedAssignees = sortedUsers.filter((user) => selectedAssigneeIds.includes(user.id));
  const selectedLabels = sortedLabels.filter((label) => selectedLabelIds.includes(label.labelId));
  const selectedMilestone = sortedMilestones.find((milestone) => milestone.id === selectedMilestoneId);

  const toggleAssignee = (userId: number) => {
    setSelectedAssigneeIds((current) => (
      current.includes(userId)
        ? current.filter((id) => id !== userId)
        : [...current, userId]
    ));
  };

  const toggleLabel = (labelId: number) => {
    setSelectedLabelIds((current) => (
      current.includes(labelId)
        ? current.filter((id) => id !== labelId)
        : [...current, labelId]
    ));
  };

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

  const handleAttach = (_publicUrl: string, filename: string, attachmentId: string) => {
    const isImage = /\.(png|jpe?g|gif|webp)$/i.test(filename);
    const marker = isImage
      ? `![${filename}](attachment:${attachmentId})`
      : `[${filename}](attachment:${attachmentId})`;
    setContent((prev) => `${prev}${prev ? '\n' : ''}${marker}`);
    setAttachmentIds((prev) => [...prev, attachmentId]);
  };

  const handleSubmit = () => {
    if (!canSubmit) return;
    mutate(
      {
        title: title.trim(),
        content,
        labelIds: selectedLabelIds,
        milestoneId: selectedMilestoneId,
        userIds: selectedAssigneeIds,
        attachmentIds: attachmentIds.length > 0 ? attachmentIds : undefined,
      },
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

            <AttachableTextarea
              value={content}
              onChange={setContent}
              onAttach={handleAttach}
              placeholder="코멘트를 입력하세요"
              disabled={isPending}
            />
          </div>
        </section>

        <aside className="sidebar-card" ref={sidebarRef}>
          <div className="sidebar-card__section sidebar-card__section--dropdown">
            <div className="sidebar-card__head">
              <span>담당자</span>
              <button
                type="button"
                aria-label="담당자 추가"
                aria-expanded={openSidebarMenu === 'assignees'}
                className="sidebar-card__plus"
                onClick={() => setOpenSidebarMenu((open) => (open === 'assignees' ? null : 'assignees'))}
              >
                <img src={icon('plus')} alt="" width={16} height={16} />
              </button>
            </div>
            {selectedAssignees.length > 0 ? (
              <div className="selected-users">
                {selectedAssignees.map((user) => (
                  <div key={user.id} className="selected-user">
                    <img
                      src={user.profileImageUrl || icon('userImageSmall')}
                      alt=""
                      width={24}
                      height={24}
                    />
                    <span>{user.username}</span>
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
                  const isSelected = selectedAssigneeIds.includes(user.id);
                  return (
                    <button
                      key={user.id}
                      type="button"
                      className="sidebar-picker__item"
                      role="option"
                      aria-selected={isSelected}
                      onClick={() => toggleAssignee(user.id)}
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

          <div
            className="sidebar-card__section sidebar-card__section--dropdown"
          >
            <div className="sidebar-card__head">
              <span>레이블</span>
              <button
                type="button"
                aria-label="레이블 추가"
                aria-expanded={openSidebarMenu === 'labels'}
                className="sidebar-card__plus"
                onClick={() => setOpenSidebarMenu((open) => (open === 'labels' ? null : 'labels'))}
              >
                <img src={icon('plus')} alt="" width={16} height={16} />
              </button>
            </div>

            {selectedLabels.length > 0 ? (
              <div className="selected-labels">
                {selectedLabels.map((label) => (
                  <LabelBadge key={label.labelId} label={label} />
                ))}
              </div>
            ) : (
              <p className="sidebar-card__placeholder">레이블이 없습니다.</p>
            )}

            {openSidebarMenu === 'labels' && (
              <div className="sidebar-picker" role="listbox" aria-multiselectable="true">
                <div className="sidebar-picker__head">레이블 선택</div>
                {isLabelsLoading && (
                  <p className="sidebar-picker__status">불러오는 중…</p>
                )}
                {isLabelsError && (
                  <p className="sidebar-picker__status sidebar-picker__status--error">
                    레이블을 불러오지 못했습니다.
                  </p>
                )}
                {!isLabelsLoading && !isLabelsError && sortedLabels.length === 0 && (
                  <p className="sidebar-picker__status">등록된 레이블이 없습니다.</p>
                )}
                {sortedLabels.map((label) => {
                  const isSelected = selectedLabelIds.includes(label.labelId);
                  return (
                    <button
                      key={label.labelId}
                      type="button"
                      className="sidebar-picker__item"
                      role="option"
                      aria-selected={isSelected}
                      onClick={() => toggleLabel(label.labelId)}
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
                aria-label="마일스톤 추가"
                aria-expanded={openSidebarMenu === 'milestone'}
                className="sidebar-card__plus"
                onClick={() => setOpenSidebarMenu((open) => (open === 'milestone' ? null : 'milestone'))}
              >
                <img src={icon('plus')} alt="" width={16} height={16} />
              </button>
            </div>
            {selectedMilestone ? (
              <div className="selected-milestone">
                <img src={icon('milestone')} alt="" width={16} height={16} />
                <span>{selectedMilestone.name}</span>
              </div>
            ) : (
              <p className="sidebar-card__placeholder">마일스톤이 없습니다.</p>
            )}
            {openSidebarMenu === 'milestone' && (
              <div className="sidebar-picker" role="listbox">
                <div className="sidebar-picker__head">마일스톤 선택</div>
                {selectedMilestone && (
                  <button
                    type="button"
                    className="sidebar-picker__item sidebar-picker__item--danger"
                    onClick={() => setSelectedMilestoneId(null)}
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
                {sortedMilestones.map((milestone) => {
                  const isSelected = selectedMilestoneId === milestone.id;
                  return (
                    <button
                      key={milestone.id}
                      type="button"
                      className="sidebar-picker__item"
                      role="option"
                      aria-selected={isSelected}
                      onClick={() => setSelectedMilestoneId(milestone.id)}
                    >
                      <img
                        src={icon(isSelected ? 'checkBoxActive' : 'checkBoxInitial')}
                        alt=""
                        width={16}
                        height={16}
                      />
                      <span className="sidebar-picker__milestone">
                        <strong>{milestone.name}</strong>
                        <span>
                          열린 이슈 {milestone.openIssueCount ?? 0}개 · 닫힌 이슈 {milestone.closedIssueCount ?? 0}개
                        </span>
                      </span>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
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
