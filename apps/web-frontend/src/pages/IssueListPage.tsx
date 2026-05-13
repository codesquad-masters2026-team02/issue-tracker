import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  useIssueListQuery,
  useLabelListQuery,
  type IssueResponse,
} from '../lib/api';
import { icon } from '../lib/icons';
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

type Tab = 'OPEN' | 'CLOSED';

export function IssueListPage() {
  const { data, isLoading, isError, error } = useIssueListQuery();
  const { data: labels = [] } = useLabelListQuery();
  const [tab, setTab] = useState<Tab>('OPEN');
  const [keyword, setKeyword] = useState('is:issue is:open');

  const { openCount, closedCount, rows } = useMemo(() => {
    const all: IssueResponse[] = data ?? [];
    const open = all.filter((i) => i.status === 'OPEN');
    const closed = all.filter((i) => i.status === 'CLOSED');
    const base = tab === 'OPEN' ? open : closed;
    const kw = keyword.replace(/is:issue|is:open|is:closed/gi, '').trim().toLowerCase();
    const filtered = kw
      ? base.filter((i) => i.title.toLowerCase().includes(kw))
      : base;
    return { openCount: open.length, closedCount: closed.length, rows: filtered };
  }, [data, tab, keyword]);

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
            <button type="button" className="chip-group__item">
              <img src={icon('milestone')} alt="" width={16} height={16} />
              마일스톤(0)
            </button>
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
            <input type="checkbox" />
          </label>
          <div className="issue-table__tabs">
            <button
              type="button"
              className={`issue-table__tab ${tab === 'OPEN' ? 'is-active' : ''}`}
              onClick={() => setTab('OPEN')}
            >
              <img src={icon('alertCircle')} alt="" width={16} height={16} />
              열린 이슈({openCount})
            </button>
            <button
              type="button"
              className={`issue-table__tab ${tab === 'CLOSED' ? 'is-active' : ''}`}
              onClick={() => setTab('CLOSED')}
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
          {rows.map((issue) => (
            <li key={issue.issueNumber} className="issue-row">
              <input type="checkbox" />
              <img
                src={icon(issue.status === 'OPEN' ? 'alertCircle' : 'archive')}
                alt={issue.status === 'OPEN' ? '열린 이슈' : '닫힌 이슈'}
                width={20}
                height={20}
                className="issue-row__status"
              />
              <div className="issue-row__main">
                <Link to={`/issues/${issue.issueNumber}`} className="issue-row__title">
                  {issue.title}
                </Link>
                <div className="issue-row__meta">
                  <span>#{issue.issueNumber}</span>
                  <span>이 이슈가 {formatRelative(issue.createdAt)}에 작성되었습니다</span>
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
