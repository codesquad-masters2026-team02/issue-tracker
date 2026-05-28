/**
 * packages/api-spec/openapi.yaml 의 스펙을 그대로 미러링한 얇은 클라이언트.
 * orval 로 generated/index.ts 가 만들어지면, 이 파일을 그쪽으로 교체하면 된다.
 */
import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from 'axios';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { getAccessToken, setAccessToken } from './authToken';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  withCredentials: true,
});

interface AuthRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
  _skipAuthRefresh?: boolean;
}

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AuthRequestConfig | undefined;

    if (
      error.response?.status === 401
      && originalRequest
      && !originalRequest._retry
      && !originalRequest._skipAuthRefresh
    ) {
      originalRequest._retry = true;
      try {
        const token = await refreshAccessToken();
        originalRequest.headers.Authorization = `Bearer ${token.accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        setAccessToken(null);
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  },
);

// ----- Schemas (openapi.yaml 와 동일) -----
export type IssueStatus = 'OPEN' | 'CLOSED';

export interface LabelSummaryResponse {
  labelId: number;
  name: string;
  backgroundColor: string;
  textColor: LabelTextColor;
}

export interface MilestoneReferenceResponse {
  milestoneId: number;
  title: string;
}

export interface MilestoneSummaryResponse {
  id: number;
  name: string;
  openIssueCount: number;
  closedIssueCount: number;
}

export interface AssigneeSummaryResponse {
  id: number;
  username: string;
  profileImageUrl?: string | null;
}

export interface IssueSummaryResponse {
  issueNumber: number;
  title: string;
  status: IssueStatus;
  author: string;
  createdAt: string; // ISO date-time
  labels: LabelSummaryResponse[];
  milestone?: MilestoneReferenceResponse | null;
  assignees: UserInfoResponse[];
}

export interface IssueDetailResponse {
  issueNumber: number;
  title: string;
  status: IssueStatus;
  createdAt: string; // ISO date-time
  authorUsername: string;
  labels: LabelSummaryResponse[];
  milestone?: MilestoneSummaryResponse | null;
  assignees: AssigneeSummaryResponse[];
}

export interface IssueSearchResponse {
  openIssueCount: number;
  closedIssueCount: number;
  pageNumber: number;
  pageSize: number;
  totalIssueCount: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  issues: IssueSummaryResponse[];
}

export interface IssueListFilters {
  status?: IssueStatus;
  assigneeIds?: number[];
  labelIds?: number[];
  milestoneId?: number;
  authorId?: number;
  pageNumber?: number;
}

export interface IssueRequest {
  title: string;
  content: string;
  labelIds?: number[];
  milestoneId?: number | null;
  userIds?: number[];
  attachmentIds?: string[];
}


export interface BulkIssueRequest {
  issueIds: number[];
  status: IssueStatus;
}

export interface IssueTitleUpdateRequest {
  title: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  password: string;
}

export interface AccessTokenResponse {
  accessToken: string;
}

export interface UserInfoResponse {
  id: number;
  username: string;
  profileImageUrl?: string | null;
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

export interface AttachmentSummaryResponse {
  attachmentId: string;
  filename: string;
  contentType: string;
  publicUrl: string;
}

export interface CommentRequest {
  content: string;
  attachmentIds?: string[];
}

export interface CommentResponse {
  id: number;
  type: CommentType;
  content: string;
  created_at: string; // ISO date-time
  username: string;
  attachments?: AttachmentSummaryResponse[];
}

export interface CommentListResponse {
  issueNumber: number;
  comment_count: number;
  comments: CommentResponse[];
}

export interface PresignResponse {
  uploadUrl: string;
  attachmentId: string;
  publicUrl: string;
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

export function getApiErrorMessage(caught: unknown, fallback = '오류가 발생했습니다.') {
  const maybeApiError = caught as {
    response?: {
      data?: {
        error?: ErrorDto;
      };
    };
    message?: string;
  };

  const apiError = maybeApiError.response?.data?.error;
  if (apiError?.code && apiError.message) {
    return `${apiError.code}: ${apiError.message}`;
  }
  return apiError?.message ?? maybeApiError.message ?? fallback;
}

// ----- Endpoints -----
export async function signIn(body: LoginRequest): Promise<AccessTokenResponse> {
  const { data } = await api.post<ApiResponse<AccessTokenResponse>>(
    '/api/users/signin',
    body,
    { _skipAuthRefresh: true } as AuthRequestConfig,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '로그인하지 못했습니다.');
  }
  setAccessToken(data.data.accessToken);
  return data.data;
}

export async function signUp(body: SignupRequest): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    '/api/users/signup',
    body,
    { _skipAuthRefresh: true } as AuthRequestConfig,
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '회원가입하지 못했습니다.');
  }
}

export async function refreshAccessToken(): Promise<AccessTokenResponse> {
  const { data } = await api.post<ApiResponse<AccessTokenResponse>>(
    '/api/users/refresh',
    undefined,
    { _skipAuthRefresh: true } as AuthRequestConfig,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '로그인이 필요합니다.');
  }
  setAccessToken(data.data.accessToken);
  return data.data;
}

export async function fetchMyInfo(): Promise<UserInfoResponse> {
  const { data } = await api.get<ApiResponse<UserInfoResponse>>('/api/users/me');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '사용자 정보를 불러오지 못했습니다.');
  }
  return data.data;
}

export async function signInWithGithub(code: string): Promise<AccessTokenResponse> {
  const { data } = await api.post<ApiResponse<AccessTokenResponse>>(
    '/api/auth/github',
    { code },
    { _skipAuthRefresh: true } as AuthRequestConfig,
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? 'GitHub 로그인에 실패했습니다.');
  }
  setAccessToken(data.data.accessToken);
  return data.data;
}

export async function uploadProfileImage(file: File): Promise<PresignResponse> {
  const { data } = await api.post<ApiResponse<PresignResponse>>(
    '/api/attachments/presign/profile',
    { filename: file.name, contentType: file.type, size: file.size },
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '업로드 URL을 가져오지 못했습니다.');
  }
  const res = await fetch(data.data.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: file,
  });
  if (!res.ok) {
    throw new Error('파일 업로드에 실패했습니다.');
  }
  return data.data;
}

export async function editProfileImage(imageUrl: string): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>('/api/users/edit', { imageUrl });
  if (!data.success) {
    throw new Error(data.error?.message ?? '프로필 이미지를 변경하지 못했습니다.');
  }
}

export async function signOut(): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>('/api/users/logout');
  if (!data.success) {
    throw new Error(data.error?.message ?? '로그아웃하지 못했습니다.');
  }
}

function normalizeIssueFilters(filters: IssueListFilters = {}): Required<IssueListFilters> {
  return {
    status: filters.status ?? 'OPEN',
    assigneeIds: filters.assigneeIds ?? [],
    labelIds: filters.labelIds ?? [],
    milestoneId: filters.milestoneId ?? 0,
    authorId: filters.authorId ?? 0,
    pageNumber: filters.pageNumber ?? 0,
  };
}

function toIssueFilterParams(filters: IssueListFilters = {}) {
  const normalized = normalizeIssueFilters(filters);
  const params = new URLSearchParams();

  params.set('status', normalized.status);
  normalized.assigneeIds.forEach((id) => params.append('assigneeIds', String(id)));
  normalized.labelIds.forEach((id) => params.append('labelIds', String(id)));
  if (normalized.milestoneId > 0) params.set('milestoneId', String(normalized.milestoneId));
  if (normalized.authorId > 0) params.set('authorId', String(normalized.authorId));
  if (normalized.pageNumber > 0) params.set('pageNumber', String(normalized.pageNumber));

  return params;
}

async function fetchIssues(filters: IssueListFilters = {}): Promise<IssueSearchResponse> {
  const { data } = await api.get<ApiResponse<IssueSearchResponse>>(
    '/api/issues',
    { params: toIssueFilterParams(filters) },
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈 목록을 불러오지 못했습니다.');
  }
  return data.data;
}

async function fetchIssueDetail(id: number): Promise<IssueDetailResponse> {
  const { data } = await api.get<ApiResponse<IssueDetailResponse>>(`/api/issues/${id}`);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈 상세를 불러오지 못했습니다.');
  }
  return data.data;
}

async function createIssue(body: IssueRequest): Promise<IssueDetailResponse> {
  const { data } = await api.post<ApiResponse<IssueDetailResponse>>('/api/issues', body);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이슈를 생성하지 못했습니다.');
  }
  return data.data;
}

async function updateIssueStatus(
  issueNumber: number,
  body: { status: IssueStatus },
): Promise<void> {
  const { data } = await api.patch<ApiResponse<void>>(`/api/issues/${issueNumber}`, body);
  if (!data.success) {
    throw new Error(data.error?.message ?? '이슈 상태를 수정하지 못했습니다.');
  }
}

async function updateIssueTitle(
  issueNumber: number,
  body: IssueTitleUpdateRequest,
): Promise<void> {
  const { data } = await api.patch<ApiResponse<void>>(`/api/issues/${issueNumber}/title`, body);
  if (!data.success) {
    throw new Error(data.error?.message ?? '이슈 제목을 수정하지 못했습니다.');
  }
}

async function bulkUpdateIssueStatus(body: BulkIssueRequest): Promise<void> {
  const { data } = await api.patch<ApiResponse<void>>('/api/issues/status', body);
  if (!data.success) {
    throw new Error(data.error?.message ?? '이슈 상태를 수정하지 못했습니다.');
  }
}

async function fetchLabels(): Promise<LabelResponse[]> {
  const { data } = await api.get<ApiResponse<LabelsResponse>>('/api/labels');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '레이블 목록을 불러오지 못했습니다.');
  }
  return data.data.labels ?? [];
}

async function fetchUsers(): Promise<UserInfoResponse[]> {
  const { data } = await api.get<ApiResponse<UserInfoResponse[]>>('/api/users');
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '사용자 목록을 불러오지 못했습니다.');
  }
  return data.data;
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

async function fetchMilestones(status: MilestoneStatus): Promise<MilestoneListResponse> {
  const { data } = await api.get<ApiResponse<MilestoneListResponse>>(
    '/api/milestones',
    { params: { status } },
  );
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

async function requestPresign(
  filename: string,
  contentType: string,
  size: number,
): Promise<PresignResponse> {
  const { data } = await api.post<ApiResponse<PresignResponse>>(
    '/api/attachments/presign',
    { filename, contentType, size },
  );
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '업로드 URL을 가져오지 못했습니다.');
  }
  return data.data;
}

export async function fetchAttachmentPresignedUrl(attachmentId: string): Promise<string> {
  const { data } = await api.get<ApiResponse<string>>(`/api/attachments/${attachmentId}/url`);
  if (!data.success || !data.data) {
    throw new Error(data.error?.message ?? '이미지 URL을 가져오지 못했습니다.');
  }
  return data.data;
}

export async function uploadFile(file: File): Promise<PresignResponse> {
  const presign = await requestPresign(file.name, file.type, file.size);
  const res = await fetch(presign.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: file,
  });
  if (!res.ok) {
    throw new Error('파일 업로드에 실패했습니다.');
  }
  return presign;
}

async function deleteComment(commentId: number): Promise<void> {
  const { data } = await api.delete<ApiResponse<void>>(`/api/comments/${commentId}`);
  if (!data.success) {
    throw new Error(data.error?.message ?? '코멘트를 삭제하지 못했습니다.');
  }
}

async function addIssueLabels(issueNumber: number, labelIds: number[]): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/labels`,
    { labelIds },
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '레이블을 추가하지 못했습니다.');
  }
}

async function removeIssueLabels(issueNumber: number, labelIds: number[]): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/labels/remove`,
    { labelIds },
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '레이블을 제거하지 못했습니다.');
  }
}

async function setIssueMilestone(issueNumber: number, milestoneId: number): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/milestones`,
    { milestoneId },
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '마일스톤을 설정하지 못했습니다.');
  }
}

async function removeIssueMilestone(issueNumber: number): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/milestones/remove`,
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '마일스톤을 제거하지 못했습니다.');
  }
}

async function addIssueAssignees(issueNumber: number, userIds: number[]): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/assignees`,
    { userIds },
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '담당자를 추가하지 못했습니다.');
  }
}

async function removeIssueAssignees(issueNumber: number, userIds: number[]): Promise<void> {
  const { data } = await api.post<ApiResponse<void>>(
    `/api/issues/${issueNumber}/assignees/remove`,
    { userIds },
  );
  if (!data.success) {
    throw new Error(data.error?.message ?? '담당자를 제거하지 못했습니다.');
  }
}

// ----- Hooks -----
export const issueKeys = {
  all: ['issues'] as const,
  list: (filters: IssueListFilters = {}) => [...issueKeys.all, 'list', normalizeIssueFilters(filters)] as const,
  detail: (id: number) => [...issueKeys.all, 'detail', id] as const,
  comments: (id: number) => [...issueKeys.detail(id), 'comments'] as const,
};

export const labelKeys = {
  all: ['labels'] as const,
  list: () => [...labelKeys.all, 'list'] as const,
};

export const milestoneKeys = {
  all: ['milestones'] as const,
  list: (status: MilestoneStatus) => [...milestoneKeys.all, 'list', status] as const,
};

export const userKeys = {
  all: ['users'] as const,
  list: () => [...userKeys.all, 'list'] as const,
};

export function useIssueListQuery(filters: IssueListFilters = {}) {
  return useQuery({
    queryKey: issueKeys.list(filters),
    queryFn: () => fetchIssues(filters),
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
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useUpdateIssueStatusMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: { status: IssueStatus }) => updateIssueStatus(issueNumber, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useUpdateIssueTitleMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: IssueTitleUpdateRequest) => updateIssueTitle(issueNumber, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}

export function useBulkUpdateIssueStatusMutation() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: bulkUpdateIssueStatus,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.all });
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
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

export function useUserListQuery() {
  return useQuery({
    queryKey: userKeys.list(),
    queryFn: fetchUsers,
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

export function useMilestoneListQuery(status: MilestoneStatus = 'OPEN') {
  return useQuery({
    queryKey: milestoneKeys.list(status),
    queryFn: () => fetchMilestones(status),
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

export function useAddIssueLabelsMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (labelIds: number[]) => addIssueLabels(issueNumber, labelIds),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}

export function useRemoveIssueLabelsMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (labelIds: number[]) => removeIssueLabels(issueNumber, labelIds),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}

export function useSetIssueMilestoneMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (milestoneId: number) => setIssueMilestone(issueNumber, milestoneId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useRemoveIssueMilestoneMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => removeIssueMilestone(issueNumber),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
      qc.invalidateQueries({ queryKey: milestoneKeys.all });
    },
  });
}

export function useAddIssueAssigneesMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (userIds: number[]) => addIssueAssignees(issueNumber, userIds),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}

export function useRemoveIssueAssigneesMutation(issueNumber: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (userIds: number[]) => removeIssueAssignees(issueNumber, userIds),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: issueKeys.detail(issueNumber) });
      qc.invalidateQueries({ queryKey: issueKeys.all });
    },
  });
}
