import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { LabelBadge } from '../components/LabelBadge';
import {
  useCreateLabelMutation,
  useDeleteLabelMutation,
  useLabelListQuery,
  useUpdateLabelMutation,
  type LabelRequest,
  type LabelResponse,
  type LabelTextColor,
} from '../lib/api';
import { icon } from '../lib/icons';
import './LabelPage.css';

const DEFAULT_FORM: LabelRequest = {
  name: '',
  description: '',
  backgroundColor: '#EFF0F6',
  textColor: 'DARK',
};

function getReadableTextColor(hex: string): LabelTextColor {
  const normalized = hex.replace('#', '');
  if (!/^[A-Fa-f0-9]{6}$/.test(normalized)) return 'DARK';

  const red = Number.parseInt(normalized.slice(0, 2), 16);
  const green = Number.parseInt(normalized.slice(2, 4), 16);
  const blue = Number.parseInt(normalized.slice(4, 6), 16);
  const brightness = (red * 299 + green * 587 + blue * 114) / 1000;
  return brightness < 145 ? 'LIGHT' : 'DARK';
}

function getRandomColor() {
  const value = Math.floor(Math.random() * 0xffffff);
  return `#${value.toString(16).padStart(6, '0').toUpperCase()}`;
}

function toRequest(form: LabelRequest): LabelRequest {
  return {
    name: form.name.trim(),
    description: form.description?.trim() || undefined,
    backgroundColor: form.backgroundColor.toUpperCase(),
    textColor: form.textColor,
  };
}

interface LabelFormProps {
  initialValue?: LabelRequest;
  title: string;
  submitLabel: string;
  isPending?: boolean;
  onCancel: () => void;
  onSubmit: (request: LabelRequest) => void;
}

function LabelForm({
  initialValue = DEFAULT_FORM,
  title,
  submitLabel,
  isPending = false,
  onCancel,
  onSubmit,
}: LabelFormProps) {
  const [form, setForm] = useState<LabelRequest>(initialValue);
  const canSubmit = form.name.trim().length > 0
    && /^#[A-Fa-f0-9]{6}$/.test(form.backgroundColor)
    && !isPending;

  const updateColor = (backgroundColor: string) => {
    setForm((current) => ({
      ...current,
      backgroundColor,
      textColor: getReadableTextColor(backgroundColor),
    }));
  };

  return (
    <div className="label-form">
      <h2 className="label-form__title">{title}</h2>

      <div className="label-form__body">
        <div className="label-form__preview">
          <LabelBadge
            label={{
              name: form.name.trim() || 'Label',
              backgroundColor: form.backgroundColor,
              textColor: form.textColor,
            }}
          />
        </div>

        <div className="label-form__fields">
          <label className="label-input">
            <span className="label-input__prefix">이름</span>
            <input
              value={form.name}
              placeholder="레이블의 이름을 입력하세요"
              onChange={(e) => setForm((current) => ({ ...current, name: e.target.value }))}
            />
          </label>
          <label className="label-input">
            <span className="label-input__prefix">설명(선택)</span>
            <input
              value={form.description}
              placeholder="레이블에 대한 설명을 입력하세요"
              onChange={(e) => setForm((current) => ({ ...current, description: e.target.value }))}
            />
          </label>

          <div className="label-form__inline">
            <label className="label-input label-input--color">
              <span className="label-input__prefix">배경 색상</span>
              <input
                value={form.backgroundColor}
                maxLength={7}
                onChange={(e) => updateColor(e.target.value)}
              />
              <button
                type="button"
                className="label-icon-btn"
                aria-label="색상 새로고침"
                onClick={() => updateColor(getRandomColor())}
              >
                <img src={icon('refreshCcw')} alt="" width={16} height={16} />
              </button>
            </label>
            <label className="label-text-select">
              <span>텍스트 색상</span>
              <select
                value={form.textColor}
                aria-label="텍스트 색상"
                onChange={(e) => setForm((current) => ({
                  ...current,
                  textColor: e.target.value as LabelTextColor,
                }))}
              >
                <option value="DARK">어두운 글자</option>
                <option value="LIGHT">밝은 글자</option>
              </select>
              <img src={icon('chevronDown')} alt="" width={16} height={16} />
            </label>
          </div>
        </div>
      </div>

      <div className="label-form__actions">
        <button type="button" className="btn btn--outline label-form__cancel" onClick={onCancel}>
          <img src={icon('xSquare')} alt="" width={16} height={16} />
          취소
        </button>
        <button
          type="button"
          className="btn btn--primary label-form__submit"
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

function toFormValue(label: LabelResponse): LabelRequest {
  return {
    name: label.name,
    description: label.description ?? '',
    backgroundColor: label.backgroundColor,
    textColor: label.textColor,
  };
}

export function LabelPage() {
  const { data: labels = [], isLoading, isError, error } = useLabelListQuery();
  const createLabel = useCreateLabelMutation();
  const updateLabel = useUpdateLabelMutation();
  const deleteLabel = useDeleteLabelMutation();
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingLabelId, setEditingLabelId] = useState<number | null>(null);

  const sortedLabels = useMemo(
    () => [...labels].sort((a, b) => a.name.localeCompare(b.name, 'ko')),
    [labels],
  );
  const mutationError = createLabel.error ?? updateLabel.error ?? deleteLabel.error;

  const handleDelete = (label: LabelResponse) => {
    if (!window.confirm(`'${label.name}' 레이블을 삭제하시겠습니까?`)) return;
    deleteLabel.mutate(label.labelId);
  };

  return (
    <div className="label-page">
      <div className="label-page__topbar">
        <div className="label-tabs" aria-label="관리 탭">
          <button type="button" className="label-tabs__item is-active">
            <img src={icon('label')} alt="" width={16} height={16} />
            레이블({labels.length})
          </button>
          <Link to="/milestones" className="label-tabs__item">
            <img src={icon('milestone')} alt="" width={16} height={16} />
            마일스톤(0)
          </Link>
        </div>
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => {
            setEditingLabelId(null);
            setIsCreateOpen(true);
          }}
        >
          <img src={icon('plus')} alt="" width={16} height={16} />
          레이블 추가
        </button>
      </div>

      {isCreateOpen && (
        <LabelForm
          title="새로운 레이블 추가"
          submitLabel="완료"
          isPending={createLabel.isPending}
          onCancel={() => setIsCreateOpen(false)}
          onSubmit={(request) => {
            createLabel.mutate(request, {
              onSuccess: () => setIsCreateOpen(false),
            });
          }}
        />
      )}

      {mutationError && (
        <p className="label-page__error">{(mutationError as Error).message}</p>
      )}

      <section className="label-list" aria-label="레이블 목록">
        {isLoading && <p className="label-list__status">불러오는 중…</p>}
        {isError && (
          <p className="label-list__status label-list__status--error">
            {(error as Error)?.message ?? '레이블 목록을 불러오지 못했습니다.'}
          </p>
        )}
        {!isLoading && !isError && sortedLabels.length === 0 && (
          <p className="label-list__status">등록된 레이블이 없습니다.</p>
        )}
        {sortedLabels.map((label) => (
          <div key={label.labelId} className="label-row">
            {editingLabelId === label.labelId ? (
              <LabelForm
                initialValue={toFormValue(label)}
                title="레이블 편집"
                submitLabel="편집 완료"
                isPending={updateLabel.isPending}
                onCancel={() => setEditingLabelId(null)}
                onSubmit={(request) => {
                  updateLabel.mutate(
                    { id: label.labelId, body: request },
                    { onSuccess: () => setEditingLabelId(null) },
                  );
                }}
              />
            ) : (
              <>
                <div className="label-row__badge">
                  <LabelBadge label={label} />
                </div>
                <p className="label-row__description">
                  {label.description || '설명이 없습니다.'}
                </p>
                <div className="label-row__actions">
                  <button
                    type="button"
                    className="label-row__action"
                    onClick={() => {
                      setIsCreateOpen(false);
                      setEditingLabelId(label.labelId);
                    }}
                  >
                    <img src={icon('edit')} alt="" width={16} height={16} />
                    편집
                  </button>
                  <button
                    type="button"
                    className="label-row__action label-row__action--danger"
                    disabled={deleteLabel.isPending}
                    onClick={() => handleDelete(label)}
                  >
                    <img src={icon('trash')} alt="" width={16} height={16} />
                    삭제
                  </button>
                </div>
              </>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}
