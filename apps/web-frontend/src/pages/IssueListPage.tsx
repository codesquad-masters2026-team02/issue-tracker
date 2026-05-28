import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import {
  useBulkUpdateIssueStatusMutation,
  useIssueListQuery,
  useLabelListQuery,
  useMilestoneListQuery,
  useUserListQuery,
  type IssueListFilters,
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
type FilterMenu = 'assignees' | 'labels' | 'milestones' | 'authors' | null;

function toTab(value: string | null): Tab {
  return value === 'CLOSED' ? 'CLOSED' : 'OPEN';
}

function quoteQueryValue(value: string) {
  return /\s/.test(value) ? `"${value.replace(/"/g, '\\"')}"` : value;
}

function toFilterToken(key: 'assignee' | 'label' | 'milestone' | 'author', value: string | number) {
  return `${key}:${quoteQueryValue(String(value))}`;
}

function parsePositiveId(value: string | null) {
  if (!value) return undefined;
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : undefined;
}

function parsePageNumber(value: string | null) {
  const pageNumber = Number(value);
  return Number.isInteger(pageNumber) && pageNumber > 0 ? pageNumber : 0;
}

function parsePositiveIds(searchParams: URLSearchParams, key: string) {
  return [...new Set(
    searchParams
      .getAll(key)
      .map((value) => Number(value))
      .filter((value) => Number.isInteger(value) && value > 0),
  )];
}

function createPageNumbers(currentPage: number, totalPages: number) {
  if (totalPages <= 0) return [];

  const visibleCount = Math.min(totalPages, 5);
  const half = Math.floor(visibleCount / 2);
  const maxStart = Math.max(0, totalPages - visibleCount);
  const start = Math.min(Math.max(0, currentPage - half), maxStart);

  return Array.from({ length: visibleCount }, (_, index) => start + index);
}

function tokenizeSearchQuery(value: string) {
  const tokens: string[] = [];
  let current = '';
  let quoted = false;
  let escaped = false;

  for (const char of value) {
    if (escaped) {
      current += char;
      escaped = false;
      continue;
    }

    if (char === '\\' && quoted) {
      escaped = true;
      current += char;
      continue;
    }

    if (char === '"') {
      quoted = !quoted;
      current += char;
      continue;
    }

    if (/\s/.test(char) && !quoted) {
      if (current) {
        tokens.push(current);
        current = '';
      }
      continue;
    }

    current += char;
  }

  if (current) tokens.push(current);
  return tokens;
}

function unquoteQueryValue(value: string) {
  if (value.startsWith('"') && value.endsWith('"') && value.length >= 2) {
    return value.slice(1, -1).replace(/\\"/g, '"');
  }
  return value;
}

export function IssueListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = toTab(searchParams.get('status'));
  const assigneeIds = useMemo(() => parsePositiveIds(searchParams, 'assigneeIds'), [searchParams]);
  const labelIds = useMemo(() => parsePositiveIds(searchParams, 'labelIds'), [searchParams]);
  const milestoneId = parsePositiveId(searchParams.get('milestoneId'));
  const authorId = parsePositiveId(searchParams.get('authorId'));
  const pageNumber = parsePageNumber(searchParams.get('pageNumber'));
  const issueFilters = useMemo<IssueListFilters>(() => ({
    status: tab,
    assigneeIds,
    labelIds,
    milestoneId,
    authorId,
    pageNumber,
  }), [assigneeIds, authorId, labelIds, milestoneId, pageNumber, tab]);
  const { data, isLoading, isError, error } = useIssueListQuery(issueFilters);
  const { data: labels = [] } = useLabelListQuery();
  const { data: users = [], isLoading: isUsersLoading, isError: isUsersError } = useUserListQuery();
  const { data: milestoneList, isLoading: isOpenMilestonesLoading, isError: isOpenMilestonesError } = useMilestoneListQuery('OPEN');
  const { data: closedMilestoneList, isLoading: isClosedMilestonesLoading, isError: isClosedMilestonesError } = useMilestoneListQuery('CLOSED');
  const {
    mutate: bulkUpdateIssueStatus,
    isPending: isBulkStatusPending,
    error: bulkStatusError,
  } = useBulkUpdateIssueStatusMutation();
  const [titleSearch, setTitleSearch] = useState('');
  const [searchInputText, setSearchInputText] = useState('');
  const [selectedIssueIds, setSelectedIssueIds] = useState<Set<number>>(() => new Set());
  const [isStatusMenuOpen, setIsStatusMenuOpen] = useState(false);
  const [openFilterMenu, setOpenFilterMenu] = useState<FilterMenu>(null);
  const selectAllRef = useRef<HTMLInputElement>(null);
  const filtersRef = useRef<HTMLDivElement>(null);
  const milestoneCount = milestoneList?.milestoneCount ?? milestoneList?.milestones.length ?? 0;
  const selectedAssigneeIds = useMemo(() => new Set(assigneeIds), [assigneeIds]);
  const selectedLabelIds = useMemo(() => new Set(labelIds), [labelIds]);
  const hasActiveIssueFilters = assigneeIds.length > 0
    || labelIds.length > 0
    || Boolean(milestoneId)
    || Boolean(authorId);

  const sortedUsers = useMemo(
    () => [...users].sort((a, b) => a.username.localeCompare(b.username, 'ko')),
    [users],
  );
  const sortedLabels = useMemo(
    () => [...labels].sort((a, b) => a.name.localeCompare(b.name, 'ko')),
    [labels],
  );
  const sortedMilestones = useMemo(() => {
    const byId = new Map(
      [...(milestoneList?.milestones ?? []), ...(closedMilestoneList?.milestones ?? [])]
        .map((milestone) => [milestone.id, milestone]),
    );
    return [...byId.values()].sort((a, b) => a.name.localeCompare(b.name, 'ko'));
  }, [closedMilestoneList, milestoneList]);
  const userById = useMemo(
    () => new Map(users.map((user) => [user.id, user])),
    [users],
  );
  const userIdByQueryValue = useMemo(() => {
    const entries = users.flatMap((user) => [
      [user.username.toLowerCase(), user.id] as const,
      [String(user.id), user.id] as const,
    ]);
    return new Map(entries);
  }, [users]);
  const labelById = useMemo(
    () => new Map(labels.map((label) => [label.labelId, label])),
    [labels],
  );
  const labelIdByQueryValue = useMemo(() => {
    const entries = labels.flatMap((label) => [
      [label.name.toLowerCase(), label.labelId] as const,
      [String(label.labelId), label.labelId] as const,
    ]);
    return new Map(entries);
  }, [labels]);
  const milestoneById = useMemo(
    () => new Map(sortedMilestones.map((milestone) => [milestone.id, milestone])),
    [sortedMilestones],
  );
  const milestoneIdByQueryValue = useMemo(() => {
    const entries = sortedMilestones.flatMap((milestone) => [
      [milestone.name.toLowerCase(), milestone.id] as const,
      [String(milestone.id), milestone.id] as const,
    ]);
    return new Map(entries);
  }, [sortedMilestones]);
  const conditionTokens = useMemo(() => {
    const tokens = ['is:issue', tab === 'OPEN' ? 'is:open' : 'is:closed'];

    assigneeIds.forEach((id) => {
      tokens.push(toFilterToken('assignee', userById.get(id)?.username ?? id));
    });
    labelIds.forEach((id) => {
      tokens.push(toFilterToken('label', labelById.get(id)?.name ?? id));
    });
    if (milestoneId) {
      tokens.push(toFilterToken('milestone', milestoneById.get(milestoneId)?.name ?? milestoneId));
    }
    if (authorId) {
      tokens.push(toFilterToken('author', userById.get(authorId)?.username ?? authorId));
    }

    return tokens;
  }, [assigneeIds, authorId, labelById, labelIds, milestoneById, milestoneId, tab, userById]);
  const conditionQueryText = useMemo(
    () => conditionTokens.join(' '),
    [conditionTokens],
  );
  const isMilestonesLoading = isOpenMilestonesLoading || isClosedMilestonesLoading;
  const isMilestonesError = isOpenMilestonesError || isClosedMilestonesError;
  const currentPage = data?.pageNumber ?? pageNumber;
  const totalPages = data?.totalPages ?? 0;
  const pageNumbers = useMemo(
    () => createPageNumbers(currentPage, totalPages),
    [currentPage, totalPages],
  );

  const { openCount, closedCount, rows, hasKeywordSearch } = useMemo(() => {
    const base = data?.issues ?? [];
    const kw = titleSearch.trim().toLowerCase();
    const filtered = kw
      ? base.filter((i) => i.title.toLowerCase().includes(kw))
      : base;
    return {
      openCount: data?.openIssueCount ?? 0,
      closedCount: data?.closedIssueCount ?? 0,
      rows: filtered,
      hasKeywordSearch: kw.length > 0,
    };
  }, [data, titleSearch]);

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

  useEffect(() => {
    if (!openFilterMenu) return;

    const closeOnOutsideClick = (event: MouseEvent) => {
      const target = event.target;
      if (
        target instanceof Node
        && filtersRef.current
        && !filtersRef.current.contains(target)
      ) {
        setOpenFilterMenu(null);
      }
    };

    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, [openFilterMenu]);

  useEffect(() => {
    setSearchInputText([conditionQueryText, titleSearch].filter(Boolean).join(' '));
  }, [conditionQueryText]);

  const handleTabClick = (nextTab: Tab) => {
    const next = new URLSearchParams(searchParams);
    if (nextTab === 'OPEN') {
      next.delete('status');
    } else {
      next.set('status', 'CLOSED');
    }
    next.delete('pageNumber');
    setSearchParams(next);
    setSelectedIssueIds(new Set());
    setIsStatusMenuOpen(false);
    setOpenFilterMenu(null);
  };

  const updateFilters = (updater: (next: URLSearchParams) => void) => {
    const next = new URLSearchParams(searchParams);
    updater(next);
    next.delete('pageNumber');
    setSearchParams(next);
    setSelectedIssueIds(new Set());
  };

  const handleSearchInputChange = (value: string) => {
    setSearchInputText(value);

    const nextAssigneeIds = new Set<number>();
    const nextLabelIds = new Set<number>();
    let nextMilestoneId: number | undefined;
    let nextAuthorId: number | undefined;
    let nextStatus: IssueStatus = 'OPEN';
    const titleParts: string[] = [];

    tokenizeSearchQuery(value).forEach((token) => {
      const separatorIndex = token.indexOf(':');
      if (separatorIndex < 0) {
        titleParts.push(token);
        return;
      }

      const key = token.slice(0, separatorIndex).toLowerCase();
      const rawValue = unquoteQueryValue(token.slice(separatorIndex + 1));
      const lookupValue = rawValue.toLowerCase();

      if (key === 'is') {
        if (lookupValue === 'closed') nextStatus = 'CLOSED';
        if (lookupValue === 'open') nextStatus = 'OPEN';
        return;
      }

      if (key === 'assignee') {
        const id = userIdByQueryValue.get(lookupValue);
        if (id) {
          nextAssigneeIds.add(id);
          return;
        }
      }

      if (key === 'label') {
        const id = labelIdByQueryValue.get(lookupValue);
        if (id) {
          nextLabelIds.add(id);
          return;
        }
      }

      if (key === 'milestone') {
        const id = milestoneIdByQueryValue.get(lookupValue);
        if (id) {
          nextMilestoneId = id;
          return;
        }
      }

      if (key === 'author') {
        const id = userIdByQueryValue.get(lookupValue);
        if (id) {
          nextAuthorId = id;
          return;
        }
      }

      titleParts.push(token);
    });

    const next = new URLSearchParams(searchParams);
    if (nextStatus === 'OPEN') {
      next.delete('status');
    } else {
      next.set('status', 'CLOSED');
    }
    next.delete('assigneeIds');
    next.delete('labelIds');
    next.delete('milestoneId');
    next.delete('authorId');
    next.delete('pageNumber');
    nextAssigneeIds.forEach((id) => next.append('assigneeIds', String(id)));
    nextLabelIds.forEach((id) => next.append('labelIds', String(id)));
    if (nextMilestoneId) next.set('milestoneId', String(nextMilestoneId));
    if (nextAuthorId) next.set('authorId', String(nextAuthorId));

    setTitleSearch(titleParts.join(' '));
    if (next.toString() !== searchParams.toString()) {
      setSearchParams(next);
    }
    setSelectedIssueIds(new Set());
    setIsStatusMenuOpen(false);
    setOpenFilterMenu(null);
  };

  const handlePageChange = (nextPageNumber: number) => {
    if (nextPageNumber < 0 || nextPageNumber === currentPage) return;
    const next = new URLSearchParams(searchParams);
    if (nextPageNumber === 0) {
      next.delete('pageNumber');
    } else {
      next.set('pageNumber', String(nextPageNumber));
    }
    setSearchParams(next);
    setSelectedIssueIds(new Set());
    setIsStatusMenuOpen(false);
    setOpenFilterMenu(null);
  };

  const clearFilterParam = (key: 'assigneeIds' | 'labelIds' | 'milestoneId' | 'authorId') => {
    updateFilters((next) => next.delete(key));
  };

  const toggleListFilter = (key: 'assigneeIds' | 'labelIds', id: number) => {
    updateFilters((next) => {
      const current = parsePositiveIds(next, key);
      const nextIds = current.includes(id)
        ? current.filter((currentId) => currentId !== id)
        : [...current, id];

      next.delete(key);
      nextIds.forEach((nextId) => next.append(key, String(nextId)));
    });
  };

  const toggleSingleFilter = (key: 'milestoneId' | 'authorId', id: number) => {
    updateFilters((next) => {
      const currentId = parsePositiveId(next.get(key));
      if (currentId === id) {
        next.delete(key);
      } else {
        next.set(key, String(id));
      }
    });
    setOpenFilterMenu(null);
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
              value={searchInputText}
              onChange={(e) => handleSearchInputChange(e.target.value)}
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
              <div className="issue-table__filters" ref={filtersRef}>
                <div className="filter-menu">
                  <button
                    type="button"
                    className={`issue-table__filter-btn ${assigneeIds.length > 0 ? 'is-active' : ''}`}
                    aria-expanded={openFilterMenu === 'assignees'}
                    onClick={() => setOpenFilterMenu((open) => (open === 'assignees' ? null : 'assignees'))}
                  >
                    담당자{assigneeIds.length > 0 ? `(${assigneeIds.length})` : ''}
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                  {openFilterMenu === 'assignees' && (
                    <div className="filter-menu__panel" role="menu" aria-label="담당자 목록">
                      <div className="filter-menu__head">담당자</div>
                      {assigneeIds.length > 0 && (
                        <button
                          type="button"
                          className="filter-menu__item filter-menu__item--muted"
                          onClick={() => clearFilterParam('assigneeIds')}
                        >
                          담당자 필터 해제
                        </button>
                      )}
                      {isUsersLoading && <p className="filter-menu__status">불러오는 중...</p>}
                      {isUsersError && <p className="filter-menu__status filter-menu__status--error">사용자 목록을 불러오지 못했습니다.</p>}
                      {!isUsersLoading && !isUsersError && sortedUsers.length === 0 && (
                        <p className="filter-menu__status">등록된 사용자가 없습니다.</p>
                      )}
                      {sortedUsers.map((user) => (
                        <button
                          key={user.id}
                          type="button"
                          className={`filter-menu__item ${selectedAssigneeIds.has(user.id) ? 'is-active' : ''}`}
                          aria-pressed={selectedAssigneeIds.has(user.id)}
                          onClick={() => toggleListFilter('assigneeIds', user.id)}
                        >
                          <img
                            src={user.profileImageUrl || icon('userImageSmall')}
                            alt=""
                            className="filter-menu__avatar"
                            width={20}
                            height={20}
                          />
                          <span>{user.username}</span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="filter-menu">
                  <button
                    type="button"
                    className={`issue-table__filter-btn ${labelIds.length > 0 ? 'is-active' : ''}`}
                    aria-expanded={openFilterMenu === 'labels'}
                    onClick={() => setOpenFilterMenu((open) => (open === 'labels' ? null : 'labels'))}
                  >
                    레이블{labelIds.length > 0 ? `(${labelIds.length})` : ''}
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                  {openFilterMenu === 'labels' && (
                    <div className="filter-menu__panel" role="menu" aria-label="레이블 목록">
                      <div className="filter-menu__head">레이블</div>
                      {labelIds.length > 0 && (
                        <button
                          type="button"
                          className="filter-menu__item filter-menu__item--muted"
                          onClick={() => clearFilterParam('labelIds')}
                        >
                          레이블 필터 해제
                        </button>
                      )}
                      {sortedLabels.length === 0 && <p className="filter-menu__status">등록된 레이블이 없습니다.</p>}
                      {sortedLabels.map((label) => (
                        <button
                          key={label.labelId}
                          type="button"
                          className={`filter-menu__item ${selectedLabelIds.has(label.labelId) ? 'is-active' : ''}`}
                          aria-pressed={selectedLabelIds.has(label.labelId)}
                          onClick={() => toggleListFilter('labelIds', label.labelId)}
                        >
                          <LabelBadge label={label} />
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="filter-menu">
                  <button
                    type="button"
                    className={`issue-table__filter-btn ${milestoneId ? 'is-active' : ''}`}
                    aria-expanded={openFilterMenu === 'milestones'}
                    onClick={() => setOpenFilterMenu((open) => (open === 'milestones' ? null : 'milestones'))}
                  >
                    마일스톤{milestoneId ? '(1)' : ''}
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                  {openFilterMenu === 'milestones' && (
                    <div className="filter-menu__panel filter-menu__panel--wide" role="menu" aria-label="마일스톤 목록">
                      <div className="filter-menu__head">마일스톤</div>
                      {milestoneId && (
                        <button
                          type="button"
                          className="filter-menu__item filter-menu__item--muted"
                          onClick={() => {
                            clearFilterParam('milestoneId');
                            setOpenFilterMenu(null);
                          }}
                        >
                          마일스톤 필터 해제
                        </button>
                      )}
                      {isMilestonesLoading && <p className="filter-menu__status">불러오는 중...</p>}
                      {isMilestonesError && <p className="filter-menu__status filter-menu__status--error">마일스톤 목록을 불러오지 못했습니다.</p>}
                      {!isMilestonesLoading && !isMilestonesError && sortedMilestones.length === 0 && (
                        <p className="filter-menu__status">등록된 마일스톤이 없습니다.</p>
                      )}
                      {sortedMilestones.map((milestone) => (
                        <button
                          key={milestone.id}
                          type="button"
                          className={`filter-menu__item filter-menu__item--stacked ${milestoneId === milestone.id ? 'is-active' : ''}`}
                          aria-pressed={milestoneId === milestone.id}
                          onClick={() => toggleSingleFilter('milestoneId', milestone.id)}
                        >
                          <span className="filter-menu__title">
                            <img src={icon('milestone')} alt="" width={14} height={14} />
                            <strong>{milestone.name}</strong>
                          </span>
                          <span className="filter-menu__meta">
                            {milestone.status === 'OPEN' ? '열림' : '닫힘'} · 열린 이슈 {milestone.openIssueCount ?? 0}개 · 닫힌 이슈 {milestone.closedIssueCount ?? 0}개
                          </span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="filter-menu">
                  <button
                    type="button"
                    className={`issue-table__filter-btn ${authorId ? 'is-active' : ''}`}
                    aria-expanded={openFilterMenu === 'authors'}
                    onClick={() => setOpenFilterMenu((open) => (open === 'authors' ? null : 'authors'))}
                  >
                    작성자{authorId ? '(1)' : ''}
                    <img src={icon('chevronDown')} alt="" width={16} height={16} />
                  </button>
                  {openFilterMenu === 'authors' && (
                    <div className="filter-menu__panel" role="menu" aria-label="작성자 목록">
                      <div className="filter-menu__head">작성자</div>
                      {authorId && (
                        <button
                          type="button"
                          className="filter-menu__item filter-menu__item--muted"
                          onClick={() => {
                            clearFilterParam('authorId');
                            setOpenFilterMenu(null);
                          }}
                        >
                          작성자 필터 해제
                        </button>
                      )}
                      {isUsersLoading && <p className="filter-menu__status">불러오는 중...</p>}
                      {isUsersError && <p className="filter-menu__status filter-menu__status--error">사용자 목록을 불러오지 못했습니다.</p>}
                      {!isUsersLoading && !isUsersError && sortedUsers.length === 0 && (
                        <p className="filter-menu__status">등록된 사용자가 없습니다.</p>
                      )}
                      {sortedUsers.map((user) => (
                        <button
                          key={user.id}
                          type="button"
                          className={`filter-menu__item ${authorId === user.id ? 'is-active' : ''}`}
                          aria-pressed={authorId === user.id}
                          onClick={() => toggleSingleFilter('authorId', user.id)}
                        >
                          <img
                            src={user.profileImageUrl || icon('userImageSmall')}
                            alt=""
                            className="filter-menu__avatar"
                            width={20}
                            height={20}
                          />
                          <span>{user.username}</span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
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
              {hasKeywordSearch || hasActiveIssueFilters
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
                  <span>이 이슈가 {formatRelative(issue.createdAt)}, {issue.author}님에 의해 작성되었습니다</span>
                  {issue.milestone && (
                    <span className="issue-row__milestone">
                      <img src={icon('milestone')} alt="" width={14} height={14} />
                      {issue.milestone.title}
                    </span>
                  )}
                </div>
              </div>
              {(issue.assignees ?? []).length > 0 ? (
                <div className="issue-row__assignees">
                  {issue.assignees.map((assignee) => (
                    <img
                      key={assignee.id}
                      src={assignee.profileImageUrl || icon('userImageSmall')}
                      alt={assignee.username}
                      title={assignee.username}
                      className="issue-row__assignee"
                      width={24}
                      height={24}
                      onClick={(e) => {
                        e.stopPropagation();
                        updateFilters((next) => {
                          next.delete('assigneeIds');
                          next.append('assigneeIds', String(assignee.id));
                        });
                      }}
                    />
                  ))}
                </div>
              ) : null}
            </li>
          ))}
        </ul>
        {totalPages > 1 && (
          <nav className="issue-pagination" aria-label="이슈 페이지">
            <button
              type="button"
              className="issue-pagination__button"
              disabled={data?.first ?? currentPage === 0}
              onClick={() => handlePageChange(currentPage - 1)}
            >
              이전
            </button>
            <div className="issue-pagination__pages">
              {pageNumbers.map((page) => (
                <button
                  key={page}
                  type="button"
                  className={`issue-pagination__page ${page === currentPage ? 'is-active' : ''}`}
                  aria-current={page === currentPage ? 'page' : undefined}
                  onClick={() => handlePageChange(page)}
                >
                  {page + 1}
                </button>
              ))}
            </div>
            <button
              type="button"
              className="issue-pagination__button"
              disabled={data?.last ?? currentPage >= totalPages - 1}
              onClick={() => handlePageChange(currentPage + 1)}
            >
              다음
            </button>
          </nav>
        )}
      </section>
    </div>
  );
}
