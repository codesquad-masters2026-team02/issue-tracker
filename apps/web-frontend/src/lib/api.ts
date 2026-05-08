/**
 * packages/api-spec/openapi.yaml 의 스펙을 그대로 미러링한 얇은 클라이언트.
 * orval 로 generated/index.ts 가 만들어지면, 이 파일을 그쪽으로 교체하면 된다.
 */
import axios from 'axios';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';

export const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// ----- Schemas (openapi.yaml 와 동일) -----
export type IssueStatus = 'OPEN' | 'CLOSED';

export interface IssueResponse {
  issueNumber: number;
  title: string;
  status: IssueStatus;
  createdAt: string; // ISO date-time
}

export interface IssueRequest {
  title: string;
  content: string;
}

export interface ErrorDto {
  code?: string;
  message?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ErrorDto;
}

// ----- Endpoints -----
async function fetchIssues(): Promise<IssueResponse[]> {
  const { data } = await api.get<ApiResponse<IssueResponse[]>>('/api/issues');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈 목록을 불러오지 못했습니다.');
  }
  return data.data;
}

async function fetchIssueDetail(id: number): Promise<IssueResponse> {
  const { data } = await api.get<ApiResponse<IssueResponse>>(`/api/issues/${id}`);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈 상세를 불러오지 못했습니다.');
  }
  return data.data;
}

async function createIssue(body: IssueRequest): Promise<IssueResponse> {
  const { data } = await api.post<ApiResponse<IssueResponse>>('/api/issues', body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈를 생성하지 못했습니다.');
  }
  return data.data;
}

// ----- Hooks -----
export const issueKeys = {
  all: ['issues'] as const,
  list: () => [...issueKeys.all, 'list'] as const,
  detail: (id: number) => [...issueKeys.all, 'detail', id] as const,
};

export function useIssueListQuery() {
  return useQuery({
    queryKey: issueKeys.list(),
    queryFn: fetchIssues,
  });
}

export function useIssueDetailQuery(id: number) {
  return useQuery({
    queryKey: issueKeys.detail(id),
    queryFn: () => fetchIssueDetail(id),
    enabled: Number.isFinite(id) && id > 0,
  });
}

export function useCreateIssueMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createIssue,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}
