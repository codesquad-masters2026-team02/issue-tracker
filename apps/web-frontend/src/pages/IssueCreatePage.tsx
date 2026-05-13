import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LabelBadge } from '../components/LabelBadge';
import {
  useCreateIssueMutation,
  useLabelListQuery,
  type LabelResponse,
} from '../lib/api';
import { icon } from '../lib/icons';
import './IssueCreatePage.css';

function sortLabels(labels: LabelResponse[]) {
  return [...labels].sort((a, b) => a.name.localeCompare(b.name, 'ko'));
}

export function IssueCreatePage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useCreateIssueMutation();
  const {
    data: labels = [],
    isLoading: isLabelsLoading,
    isError: isLabelsError,
  } = useLabelListQuery();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [selectedLabelIds, setSelectedLabelIds] = useState<number[]>([]);
  const [isLabelMenuOpen, setIsLabelMenuOpen] = useState(false);
  const labelDropdownRef = useRef<HTMLDivElement>(null);

  const canSubmit = title.trim().length > 0 && !isPending;
  const sortedLabels = sortLabels(labels);
  const selectedLabels = sortedLabels.filter((label) => selectedLabelIds.includes(label.labelId));

  const toggleLabel = (labelId: number) => {
    setSelectedLabelIds((current) => (
      current.includes(labelId)
        ? current.filter((id) => id !== labelId)
        : [...current, labelId]
    ));
  };

  useEffect(() => {
    if (!isLabelMenuOpen) return;

    const closeOnOutsideClick = (event: MouseEvent) => {
      const target = event.target;
      if (
        target instanceof Node
        && labelDropdownRef.current
        && !labelDropdownRef.current.contains(target)
      ) {
        setIsLabelMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, [isLabelMenuOpen]);

  const handleSubmit = () => {
    if (!canSubmit) return;
    mutate(
      { title: title.trim(), content, labelIds: selectedLabelIds },
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

        <aside className="sidebar-card">
          <div className="sidebar-card__section">
            <div className="sidebar-card__head">
              <span>담당자</span>
              <button type="button" aria-label="담당자 추가" className="sidebar-card__plus">
                <img src={icon('plus')} alt="" width={16} height={16} />
              </button>
            </div>
            <p className="sidebar-card__placeholder">담당자가 없습니다.</p>
          </div>

          <div
            className="sidebar-card__section sidebar-card__section--dropdown"
            ref={labelDropdownRef}
          >
            <div className="sidebar-card__head">
              <span>레이블</span>
              <button
                type="button"
                aria-label="레이블 추가"
                aria-expanded={isLabelMenuOpen}
                className="sidebar-card__plus"
                onClick={() => setIsLabelMenuOpen((open) => !open)}
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

            {isLabelMenuOpen && (
              <div className="label-select" role="listbox" aria-multiselectable="true">
                <div className="label-select__head">레이블 선택</div>
                {isLabelsLoading && (
                  <p className="label-select__status">불러오는 중…</p>
                )}
                {isLabelsError && (
                  <p className="label-select__status label-select__status--error">
                    레이블을 불러오지 못했습니다.
                  </p>
                )}
                {!isLabelsLoading && !isLabelsError && sortedLabels.length === 0 && (
                  <p className="label-select__status">등록된 레이블이 없습니다.</p>
                )}
                {sortedLabels.map((label) => {
                  const isSelected = selectedLabelIds.includes(label.labelId);
                  return (
                    <button
                      key={label.labelId}
                      type="button"
                      className="label-select__item"
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

          <div className="sidebar-card__section">
            <div className="sidebar-card__head">
              <span>마일스톤</span>
              <button type="button" aria-label="마일스톤 추가" className="sidebar-card__plus">
                <img src={icon('plus')} alt="" width={16} height={16} />
              </button>
            </div>
            <p className="sidebar-card__placeholder">마일스톤이 없습니다.</p>
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
