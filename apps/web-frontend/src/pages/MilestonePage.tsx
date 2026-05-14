import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  useCreateMilestoneMutation,
  useDeleteMilestoneMutation,
  useLabelListQuery,
  useMilestoneListQuery,
  useUpdateMilestoneMutation,
  useUpdateMilestoneStatusMutation,
  type MilestoneStatus,
  type MilestoneRequest,
  type MilestoneResponse,
} from '../lib/api';
import { icon } from '../lib/icons';
import './MilestonePage.css';

const DEFAULT_FORM: MilestoneRequest = {
  name: '',
  dueDate: '',
  description: '',
};

function formatMilestoneDate(value?: string | null) {
  if (!value) return '';
  const dotMatch = value.match(/^(\d{4})\.\s?(\d{1,2})\.\s?(\d{1,2})$/);
  if (dotMatch) {
    return `${dotMatch[1]}. ${dotMatch[2].padStart(2, '0')}. ${dotMatch[3].padStart(2, '0')}`;
  }
  const dashMatch = value.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
  if (dashMatch) {
    return `${dashMatch[1]}. ${dashMatch[2].padStart(2, '0')}. ${dashMatch[3].padStart(2, '0')}`;
  }
  return value;
}

function toApiDateValue(value?: string) {
  const formatted = formatMilestoneDate(value?.trim());
  return /^\d{4}\. \d{2}\. \d{2}$/.test(formatted) ? formatted : undefined;
}

function toRequest(form: MilestoneRequest): MilestoneRequest {
  return {
    name: form.name.trim(),
    description: form.description?.trim() || undefined,
    dueDate: toApiDateValue(form.dueDate),
  };
}

function toFormValue(milestone: MilestoneResponse): MilestoneRequest {
  return {
    name: milestone.name,
    description: milestone.description ?? '',
    dueDate: formatMilestoneDate(milestone.dueDate),
  };
}

function getProgress(milestone: MilestoneResponse) {
  const openCount = milestone.openIssueCount ?? 0;
  const closedCount = milestone.closedIssueCount ?? 0;
  const total = openCount + closedCount;
  if (total === 0) return 0;
  return Math.round((closedCount / total) * 100);
}

interface MilestoneFormProps {
  initialValue?: MilestoneRequest;
  title: string;
  submitLabel: string;
  isPending?: boolean;
  onCancel: () => void;
  onSubmit: (request: MilestoneRequest) => void;
}

function MilestoneForm({
  initialValue = DEFAULT_FORM,
  title,
  submitLabel,
  isPending = false,
  onCancel,
  onSubmit,
}: MilestoneFormProps) {
  const [form, setForm] = useState<MilestoneRequest>(initialValue);
  const isDueDateValid = !form.dueDate
    || /^\d{4}\.\s?\d{1,2}\.\s?\d{1,2}$/.test(form.dueDate)
    || /^\d{4}-\d{1,2}-\d{1,2}$/.test(form.dueDate);
  const canSubmit = form.name.trim().length > 0 && isDueDateValid && !isPending;

  return (
    <div className="milestone-form">
      <h2 className="milestone-form__title">{title}</h2>

      <div className="milestone-form__fields">
        <label className="milestone-input">
          <span className="milestone-input__prefix">이름</span>
          <input
            value={form.name}
            placeholder="마일스톤의 이름을 입력하세요"
            onChange={(e) => setForm((current) => ({ ...current, name: e.target.value }))}
          />
        </label>
        <label className="milestone-input">
          <span className="milestone-input__prefix">완료일</span>
          <input
            value={form.dueDate ?? ''}
            inputMode="numeric"
            placeholder="YYYY. MM. DD"
            maxLength={12}
            onChange={(e) => setForm((current) => ({ ...current, dueDate: e.target.value }))}
            onBlur={() => setForm((current) => ({
              ...current,
              dueDate: formatMilestoneDate(current.dueDate),
            }))}
          />
        </label>
        <label className="milestone-input">
          <span className="milestone-input__prefix">설명(선택)</span>
          <input
            value={form.description ?? ''}
            placeholder="마일스톤에 대한 설명을 입력하세요"
            onChange={(e) => setForm((current) => ({
              ...current,
              description: e.target.value,
            }))}
          />
        </label>
      </div>

      <div className="milestone-form__actions">
        <button type="button" className="btn btn--outline milestone-form__cancel" onClick={onCancel}>
          <img src={icon('xSquare')} alt="" width={16} height={16} />
          취소
        </button>
        <button
          type="button"
          className="btn btn--primary milestone-form__submit"
          disabled={!canSubmit}
          onClick={() => onSubmit(toRequest(form))}
        >
          <img src={icon('plus')} alt="" width={16} height={16} />
          {isPending ? '저장 중…' : submitLabel}
        </button>
      </div>
    </div>
  );
}

interface MilestoneRowProps {
  milestone: MilestoneResponse;
  isDeleting: boolean;
  isStatusUpdating: boolean;
  onEdit: () => void;
  onDelete: () => void;
  onStatusToggle: () => void;
}

function MilestoneRow({
  milestone,
  isDeleting,
  isStatusUpdating,
  onEdit,
  onDelete,
  onStatusToggle,
}: MilestoneRowProps) {
  const progress = getProgress(milestone);
  const isOpen = milestone.status === 'OPEN';

  return (
    <>
      <div className="milestone-row__main">
        <div className="milestone-row__title">
          <img src={icon('milestone')} alt="" width={16} height={16} />
          <strong>{milestone.name}</strong>
        </div>
        <div className="milestone-row__due">
          <img src={icon('calendar')} alt="" width={16} height={16} />
          <span>{formatMilestoneDate(milestone.dueDate) || '완료일 없음'}</span>
        </div>
        <p className="milestone-row__description">
          {milestone.description || '설명이 없습니다.'}
        </p>
      </div>

      <div className="milestone-row__progress">
        <div className="milestone-progress">
          <div className="milestone-progress__track">
            <div className="milestone-progress__fill" style={{ width: `${progress}%` }} />
          </div>
          <span>{progress}%</span>
        </div>
        <div className="milestone-row__counts">
          <span>열린 이슈 {milestone.openIssueCount ?? 0}</span>
          <span>닫힌 이슈 {milestone.closedIssueCount ?? 0}</span>
        </div>
      </div>

      <div className="milestone-row__actions">
        <button
          type="button"
          className="milestone-row__action"
          disabled={isStatusUpdating}
          onClick={onStatusToggle}
        >
          {isStatusUpdating ? '변경 중…' : isOpen ? '닫기' : '열기'}
        </button>
        <button type="button" className="milestone-row__action" onClick={onEdit}>
          <img src={icon('edit')} alt="" width={16} height={16} />
          편집
        </button>
        <button
          type="button"
          className="milestone-row__action milestone-row__action--danger"
          disabled={isDeleting}
          onClick={onDelete}
        >
          <img src={icon('trash')} alt="" width={16} height={16} />
          {isDeleting ? '삭제 중…' : '삭제'}
        </button>
      </div>
    </>
  );
}

export function MilestonePage() {
  const { data: labels = [] } = useLabelListQuery();
  const {
    data,
    isLoading,
    isError,
    error,
  } = useMilestoneListQuery();
  const createMilestone = useCreateMilestoneMutation();
  const updateMilestone = useUpdateMilestoneMutation();
  const updateMilestoneStatus = useUpdateMilestoneStatusMutation();
  const deleteMilestone = useDeleteMilestoneMutation();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingMilestoneId, setEditingMilestoneId] = useState<number | null>(null);
  const [deletingMilestoneId, setDeletingMilestoneId] = useState<number | null>(null);
  const [updatingStatusMilestoneId, setUpdatingStatusMilestoneId] = useState<number | null>(null);

  const milestones = data?.milestones ?? [];
  const milestoneCount = data?.milestoneCount ?? milestones.length;
  const openMilestoneCount = data?.openMilestoneCount ?? 0;
  const closedMilestoneCount = data?.closedMilestoneCount ?? 0;
  const sortedMilestones = useMemo(
    () => [...milestones].sort((a, b) => {
      if (a.status !== b.status) return a.status === 'OPEN' ? -1 : 1;
      return a.name.localeCompare(b.name, 'ko');
    }),
    [milestones],
  );
  const mutationError = createMilestone.error
    ?? updateMilestone.error
    ?? updateMilestoneStatus.error
    ?? deleteMilestone.error;

  const handleDelete = (milestone: MilestoneResponse) => {
    if (!window.confirm(`'${milestone.name}' 마일스톤을 삭제하시겠습니까?`)) return;
    setDeletingMilestoneId(milestone.id);
    deleteMilestone.mutate(milestone.id, {
      onSettled: () => setDeletingMilestoneId(null),
    });
  };

  const handleStatusToggle = (milestone: MilestoneResponse) => {
    const nextStatus: MilestoneStatus = milestone.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    setUpdatingStatusMilestoneId(milestone.id);
    updateMilestoneStatus.mutate(
      { id: milestone.id, body: { status: nextStatus } },
      { onSettled: () => setUpdatingStatusMilestoneId(null) },
    );
  };

  return (
    <div className="milestone-page">
      <div className="milestone-page__topbar">
        <div className="milestone-tabs" aria-label="관리 탭">
          <Link to="/labels" className="milestone-tabs__item">
            <img src={icon('label')} alt="" width={16} height={16} />
            레이블({labels.length})
          </Link>
          <button type="button" className="milestone-tabs__item is-active">
            <img src={icon('milestone')} alt="" width={16} height={16} />
            마일스톤({milestoneCount})
          </button>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => {
            setEditingMilestoneId(null);
            setIsCreateOpen(true);
          }}
        >
          <img src={icon('plus')} alt="" width={16} height={16} />
          마일스톤 추가
        </button>
      </div>

      {isCreateOpen && (
        <MilestoneForm
          title="새로운 마일스톤 추가"
          submitLabel="완료"
          isPending={createMilestone.isPending}
          onCancel={() => setIsCreateOpen(false)}
          onSubmit={(request) => {
            createMilestone.mutate(request, {
              onSuccess: () => setIsCreateOpen(false),
            });
          }}
        />
      )}

      {mutationError && (
        <p className="milestone-page__error">{(mutationError as Error).message}</p>
      )}

      <section className="milestone-list" aria-label="마일스톤 목록">
        <header className="milestone-list__head">
          <div className="milestone-list__counts" aria-label="마일스톤 상태별 개수">
            <span className="milestone-list__count is-active">
              <img src={icon('milestone')} alt="" width={16} height={16} />
              열린 마일스톤({openMilestoneCount})
            </span>
            <span className="milestone-list__count">
              <img src={icon('archive')} alt="" width={16} height={16} />
              닫힌 마일스톤({closedMilestoneCount})
            </span>
          </div>
        </header>
        {isLoading && <p className="milestone-list__status">불러오는 중…</p>}
        {isError && (
          <p className="milestone-list__status milestone-list__status--error">
            {(error as Error)?.message ?? '마일스톤 목록을 불러오지 못했습니다.'}
          </p>
        )}
        {!isLoading && !isError && sortedMilestones.length === 0 && (
          <p className="milestone-list__status">등록된 마일스톤이 없습니다.</p>
        )}
        {sortedMilestones.map((milestone) => (
          <div key={milestone.id} className="milestone-row">
            {editingMilestoneId === milestone.id ? (
              <MilestoneForm
                initialValue={toFormValue(milestone)}
                title="마일스톤 편집"
                submitLabel="편집 완료"
                isPending={updateMilestone.isPending}
                onCancel={() => setEditingMilestoneId(null)}
                onSubmit={(request) => {
                  updateMilestone.mutate(
                    { id: milestone.id, body: request },
                    { onSuccess: () => setEditingMilestoneId(null) },
                  );
                }}
              />
            ) : (
              <MilestoneRow
                milestone={milestone}
                isDeleting={deletingMilestoneId === milestone.id}
                isStatusUpdating={updatingStatusMilestoneId === milestone.id}
                onEdit={() => {
                  setIsCreateOpen(false);
                  setEditingMilestoneId(milestone.id);
                }}
                onDelete={() => handleDelete(milestone)}
                onStatusToggle={() => handleStatusToggle(milestone)}
              />
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
