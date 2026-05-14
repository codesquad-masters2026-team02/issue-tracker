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
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
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
  labelIds?: number[];
}

export type LabelTextColor = 'DARK' | 'LIGHT';

export interface LabelRequest {
  name: string;
  description?: string;
  backgroundColor: string;
  textColor: LabelTextColor;
}

export interface LabelResponse {
  labelId: number;
  name: string;
  description?: string;
  backgroundColor: string;
  textColor: LabelTextColor;
}

export interface LabelsResponse {
  labels: LabelResponse[];
}

export interface MilestoneRequest {
  name: string;
  description?: string;
  dueDate?: string;
}

export type MilestoneStatus = 'OPEN' | 'CLOSED';

export interface MilestoneStatusUpdateRequest {
  status: MilestoneStatus;
}

export interface MilestoneResponse {
  id: number;
  name: string;
  description?: string;
  dueDate?: string | null;
  status: MilestoneStatus;
  openIssueCount: number;
  closedIssueCount: number;
}

export interface MilestoneListResponse {
  milestoneCount: number;
  openMilestoneCount: number;
  closedMilestoneCount: number;
  milestones: MilestoneResponse[];
}

export type CommentType = 'ISSUE_BODY' | 'DISCUSSION';

export interface CommentRequest {
  content: string;
}

export interface CommentResponse {
  id: number;
  type: CommentType;
  content: string;
  created_at: string; // ISO date-time
}

export interface CommentListResponse {
  issueNumber: number;
  comment_count: number;
  comments: CommentResponse[];
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

async function fetchLabels(): Promise<LabelResponse[]> {
  const { data } = await api.get<ApiResponse<LabelsResponse>>('/api/labels');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '레이블 목록을 불러오지 못했습니다.');
  }
  return data.data.labels ?? [];
}

async function createLabel(body: LabelRequest): Promise<LabelResponse> {
  const { data } = await api.post<ApiResponse<LabelResponse>>('/api/labels', body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '레이블을 생성하지 못했습니다.');
  }
  return data.data;
}

async function updateLabel(id: number, body: LabelRequest): Promise<LabelResponse> {
  const { data } = await api.put<ApiResponse<LabelResponse>>(`/api/labels/${id}`, body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '레이블을 수정하지 못했습니다.');
  }
  return data.data;
}

async function deleteLabel(id: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<void>>(`/api/labels/${id}`);
  if (!data.success) {
    throw new Error(data.error?.message ?? '레이블을 삭제하지 못했습니다.');
  }
}

async function fetchMilestones(): Promise<MilestoneListResponse> {
  const { data } = await api.get<ApiResponse<MilestoneListResponse>>('/api/milestones');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '마일스톤 목록을 불러오지 못했습니다.');
  }
  return data.data;
}

async function createMilestone(body: MilestoneRequest): Promise<MilestoneResponse> {
  const { data } = await api.post<ApiResponse<MilestoneResponse>>('/api/milestones', body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '마일스톤을 생성하지 못했습니다.');
  }
  return data.data;
}

async function updateMilestone(
  id: number,
  body: MilestoneRequest,
): Promise<MilestoneResponse> {
  const { data } = await api.put<ApiResponse<MilestoneResponse>>(`/api/milestones/${id}`, body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '마일스톤을 수정하지 못했습니다.');
  }
  return data.data;
}

async function updateMilestoneStatus(
  id: number,
  body: MilestoneStatusUpdateRequest,
): Promise<MilestoneResponse> {
  const { data } = await api.patch<ApiResponse<MilestoneResponse>>(
    `/api/milestones/${id}`,
    body,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '마일스톤 상태를 수정하지 못했습니다.');
  }
  return data.data;
}

async function deleteMilestone(id: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<void>>(`/api/milestones/${id}`);
  if (!data.success) {
    throw new Error(data.error?.message ?? '마일스톤을 삭제하지 못했습니다.');
  }
}

async function fetchIssueComments(issueNumber: number): Promise<CommentListResponse> {
  const { data } = await api.get<ApiResponse<CommentListResponse>>(
    `/api/issues/${issueNumber}/comments`,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '코멘트 목록을 불러오지 못했습니다.');
  }
  return data.data;
}

async function createComment(
  issueNumber: number,
  body: CommentRequest,
): Promise<CommentResponse> {
  const { data } = await api.post<ApiResponse<CommentResponse>>(
    `/api/issues/${issueNumber}/comments`,
    body,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '코멘트를 작성하지 못했습니다.');
  }
  return data.data;
}

async function deleteComment(commentId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<void>>(`/api/comments/${commentId}`);
  if (!data.success) {
    throw new Error(data.error?.message ?? '코멘트를 삭제하지 못했습니다.');
  }
}

// ----- Hooks -----
export const issueKeys = {
  all: ['issues'] as const,
  list: () => [...issueKeys.all, 'list'] as const,
  detail: (id: number) => [...issueKeys.all, 'detail', id] as const,
  comments: (id: number) => [...issueKeys.detail(id), 'comments'] as const,
};

export const labelKeys = {
  all: ['labels'] as const,
  list: () => [...labelKeys.all, 'list'] as const,
};

export const milestoneKeys = {
  all: ['milestones'] as const,
  list: () => [...milestoneKeys.all, 'list'] as const,
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

export function useLabelListQuery() {
  return useQuery({
    queryKey: labelKeys.list(),
    queryFn: fetchLabels,
    staleTime: Infinity,
    gcTime: 1000 * 60 * 30,
  });
}

export function useCreateLabelMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createLabel,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: labelKeys.all });
    },
  });
}

export function useUpdateLabelMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: LabelRequest }) => updateLabel(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: labelKeys.all });
    },
  });
}

export function useDeleteLabelMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteLabel,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: labelKeys.all });
    },
  });
}

export function useMilestoneListQuery() {
  return useQuery({
    queryKey: milestoneKeys.list(),
    queryFn: fetchMilestones,
    staleTime: Infinity,
    gcTime: 1000 * 60 * 30,
  });
}

export function useCreateMilestoneMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: createMilestone,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useUpdateMilestoneMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: MilestoneRequest }) => (
      updateMilestone(id, body)
    ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useUpdateMilestoneStatusMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: MilestoneStatusUpdateRequest }) => (
      updateMilestoneStatus(id, body)
    ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useDeleteMilestoneMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteMilestone,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useIssueCommentsQuery(issueNumber: number) {
  return useQuery({
    queryKey: issueKeys.comments(issueNumber),
    queryFn: () => fetchIssueComments(issueNumber),
    enabled: Number.isFinite(issueNumber) && issueNumber > 0,
  });
}

export function useCreateCommentMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CommentRequest) => createComment(issueNumber, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.comments(issueNumber) });
    },
  });
}

export function useDeleteCommentMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteComment,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.comments(issueNumber) });
    },
  });
}
