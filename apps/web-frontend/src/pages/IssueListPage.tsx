import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  useBulkUpdateIssueStatusMutation,
  useIssueListQuery,
  useLabelListQuery,
  useMilestoneListQuery,
  type IssueStatus,
} from '../lib/api';
import { icon } from '../lib/icons';
import { LabelBadge } from '../components/LabelBadge';
import './IssueListPage.css';

function formatRelative(iso: string) {
  const created = new Date(iso).getTime();
  if (Number.isNaN(created)) return '';
  const diffMin = Math.max(0, Math.floor((Date.now() - created) / 60000));
  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  const diffDay = Math.floor(diffHour / 24);
  return `${diffDay}일 전`;
}

type Tab = IssueStatus;

function toTab(value: string | null): Tab {
  return value === 'CLOSED' ? 'CLOSED' : 'OPEN';
}

function toKeyword(status: Tab) {
  return `is:issue ${status === 'OPEN' ? 'is:open' : 'is:closed'}`;
}

export function IssueListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = toTab(searchParams.get('status'));
  const { data, isLoading, isError, error } = useIssueListQuery(tab);
  const { data: labels = [] } = useLabelListQuery();
  const { data: milestoneList } = useMilestoneListQuery();
  const {
    mutate: bulkUpdateIssueStatus,
    isPending: isBulkStatusPending,
    error: bulkStatusError,
  } = useBulkUpdateIssueStatusMutation();
  const [keyword, setKeyword] = useState(() => toKeyword(tab));
  const [selectedIssueIds, setSelectedIssueIds] = useState<Set<number>>(() => new Set());
  const [isStatusMenuOpen, setIsStatusMenuOpen] = useState(false);
  const selectAllRef = useRef<HTMLInputElement>(null);
  const milestoneCount = milestoneList?.milestoneCount ?? milestoneList?.milestones.length ?? 0;

  const { openCount, closedCount, rows } = useMemo(() => {
    const base = data?.issues ?? [];
    const kw = keyword.replace(/is:issue|is:open|is:closed/gi, '').trim().toLowerCase();
    const filtered = kw
      ? base.filter((i) => i.title.toLowerCase().includes(kw))
      : base;
    return {
      openCount: data?.openIssueCount ?? 0,
      closedCount: data?.closedIssueCount ?? 0,
      rows: filtered,
    };
  }, [data, keyword]);

  const visibleIssueIds = useMemo(
    () => rows.map((issue) => issue.issueNumber),
    [rows],
  );
  const selectedCount = selectedIssueIds.size;
  const isAllVisibleSelected = rows.length > 0
    && visibleIssueIds.every((issueNumber) => selectedIssueIds.has(issueNumber));
  const isPartiallySelected = selectedCount > 0 && !isAllVisibleSelected;

  useEffect(() => {
    if (!selectAllRef.current) return;
    selectAllRef.current.indeterminate = isPartiallySelected;
  }, [isPartiallySelected]);

  useEffect(() => {
    const visibleIds = new Set(visibleIssueIds);
    setSelectedIssueIds((current) => {
      const next = new Set([...current].filter((issueId) => visibleIds.has(issueId)));
      return next.size === current.size ? current : next;
    });
  }, [visibleIssueIds]);

  const handleTabClick = (nextTab: Tab) => {
    setSearchParams(nextTab === 'OPEN' ? {} : { status: 'CLOSED' });
    setKeyword(toKeyword(nextTab));
    setSelectedIssueIds(new Set());
    setIsStatusMenuOpen(false);
  };

  const handleSelectAllChange = () => {
    setSelectedIssueIds(isAllVisibleSelected ? new Set() : new Set(visibleIssueIds));
  };

  const toggleIssueSelection = (issueNumber: number) => {
    setSelectedIssueIds((current) => {
      const next = new Set(current);
      if (next.has(issueNumber)) {
        next.delete(issueNumber);
      } else {
        next.add(issueNumber);
      }
      return next;
    });
  };

  const handleBulkStatusChange = (status: IssueStatus) => {
    if (selectedIssueIds.size === 0 || isBulkStatusPending) return;

    bulkUpdateIssueStatus(
      { issueIds: [...selectedIssueIds], status },
      {
        onSuccess: () => {
          setSelectedIssueIds(new Set());
          setIsStatusMenuOpen(false);
        },
      },
    );
  };

  return (
    <div className="issue-list">
      {/* 상단 바 */}
      <div className="issue-list__topbar">
        {/* 필터 + 검색이 하나의 pill */}
        <div className="search-pill">
          <button type="button" className="search-pill__filter">
            <span>필터</span>
            <img src={icon('chevronDown')} alt="" width={16} height={16} />
          </button>
          <div className="search-pill__divider" />
          <div className="search-pill__input">
            <img src={icon('search')} alt="" width={16} height={16} />
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="is:issue is:open"
            />
          </div>
        </div>

        <div className="issue-list__actions">
          {/* 레이블 + 마일스톤이 하나의 outline pill */}
          <div className="chip-group">
            <Link to="/labels" className="chip-group__item">
              <img src={icon('label')} alt="" width={16} height={16} />
              레이블({labels.length})
            </Link>
            <div className="chip-group__divider" />
            <Link to="/milestones" className="chip-group__item">
              <img src={icon('milestone')} alt="" width={16} height={16} />
              마일스톤({milestoneCount})
            </Link>
          </div>
          <Link to="/issues/new" className="btn btn--primary">
            <img src={icon('plus')} alt="" width={16} height={16} />
            이슈 작성
          </Link>
        </div>
      </div>

      {/* 테이블 */}
      <section className="issue-table">
        <header className="issue-table__head">
          <label className="issue-table__select-all">
            <input
              ref={selectAllRef}
              type="checkbox"
              checked={isAllVisibleSelected}
              disabled={rows.length === 0 || isBulkStatusPending}
              onChange={handleSelectAllChange}
            />
          </label>
          {selectedCount > 0 ? (
            <>
              <div className="issue-table__selection-summary">
                {selectedCount}개 이슈 선택
              </div>
              <div className="issue-table__bulk-actions">
                <div className="status-menu">
                  <button
                    type="button"
                    className="status-menu__trigger"
                    disabled={isBulkStatusPending}
                    aria-expanded={isStatusMenuOpen}
                    onClick={() => setIsStatusMenuOpen((current) => !current)}
                  >
                    상태 수정
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                  {isStatusMenuOpen && (
                    <div className="status-menu__panel">
                      <button
                        type="button"
                        className="status-menu__item"
                        disabled={tab === 'OPEN' || isBulkStatusPending}
                        onClick={() => handleBulkStatusChange('OPEN')}
                      >
                        선택한 이슈 열기
                      </button>
                      <button
                        type="button"
                        className="status-menu__item"
                        disabled={tab === 'CLOSED' || isBulkStatusPending}
                        onClick={() => handleBulkStatusChange('CLOSED')}
                      >
                        선택한 이슈 닫기
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </>
          ) : (
            <>
              <div className="issue-table__tabs">
                <button
                  type="button"
                  className={`issue-table__tab ${tab === 'OPEN' ? 'is-active' : ''}`}
                  onClick={() => handleTabClick('OPEN')}
                >
                  <img src={icon('alertCircle')} alt="" width={16} height={16} />
                  열린 이슈({openCount})
                </button>
                <button
                  type="button"
                  className={`issue-table__tab ${tab === 'CLOSED' ? 'is-active' : ''}`}
                  onClick={() => handleTabClick('CLOSED')}
                >
                  <img src={icon('archive')} alt="" width={16} height={16} />
                  닫힌 이슈({closedCount})
                </button>
              </div>
              <div className="issue-table__filters">
                {(['담당자', '레이블', '마일스톤', '작성자'] as const).map((label) => (
                  <button key={label} type="button" className="issue-table__filter-btn">
                    {label}
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                ))}
              </div>
            </>
          )}
        </header>

        <ul className="issue-table__body">
          {isLoading && <li className="issue-table__empty">불러오는 중…</li>}
          {isError && (
            <li className="issue-table__empty issue-table__empty--error">
              {(error as Error)?.message ?? '오류가 발생했습니다.'}
            </li>
          )}
          {!isLoading && !isError && rows.length === 0 && (
            <li className="issue-table__empty">
              {keyword.trim()
                ? '검색과 일치하는 결과가 없습니다.'
                : '등록된 이슈가 없습니다.'}
            </li>
          )}
          {bulkStatusError && (
            <li className="issue-table__empty issue-table__empty--error">
              {(bulkStatusError as Error).message}
            </li>
          )}
          {rows.map((issue) => (
            <li key={issue.issueNumber} className="issue-row">
              <input
                type="checkbox"
                checked={selectedIssueIds.has(issue.issueNumber)}
                disabled={isBulkStatusPending}
                onChange={() => toggleIssueSelection(issue.issueNumber)}
              />
              <img
                src={icon(issue.status === 'OPEN' ? 'alertCircle' : 'archive')}
                alt={issue.status === 'OPEN' ? '열린 이슈' : '닫힌 이슈'}
                width={20}
                height={20}
                className="issue-row__status"
              />
              <div className="issue-row__main">
                <div className="issue-row__title-line">
                  <Link to={`/issues/${issue.issueNumber}`} className="issue-row__title">
                    {issue.title}
                  </Link>
                  {(issue.labels ?? []).map((label) => (
                    <LabelBadge key={label.labelId} label={label} />
                  ))}
                </div>
                <div className="issue-row__meta">
                  <span>#{issue.issueNumber}</span>
                  <span>이 이슈가 {formatRelative(issue.createdAt)}에 작성되었습니다</span>
                  {issue.milestone && (
                    <span className="issue-row__milestone">
                      <img src={icon('milestone')} alt="" width={14} height={14} />
                      {issue.milestone.title}
                    </span>
                  )}
                </div>
              </div>
              <img
                src={icon('userImageSmall')}
                alt=""
                className="issue-row__assignee"
                width={24}
                height={24}
              />
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
