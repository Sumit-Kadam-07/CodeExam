import { get, post, put } from './api';
import { apiClient } from '@/lib/axios';
import { ExamDTO, QuestionDTO, ResultDTO, UserDTO, CodeSubmissionDTO } from '@/types';

export interface DashboardData {
  student: UserDTO;
  pastResults: ResultDTO[];
  totalTaken: number;
  averageScore: number;
  highestScore: number;
  chartLabels: string[];
  chartData: number[];
  availableExams: ExamDTO[];
}

export async function getStudentDashboard(): Promise<DashboardData> {
  return get<DashboardData>('/student/dashboard');
}

export async function getAvailableExams(): Promise<ExamDTO[]> {
  return get<ExamDTO[]>('/student/exams');
}

export async function getExamQuestions(examId: number): Promise<QuestionDTO[]> {
  return get<QuestionDTO[]>(`/student/exams/${examId}/questions`);
}

export async function getStudentResults(): Promise<ResultDTO[]> {
  return get<ResultDTO[]>('/student/results');
}

export async function getResultDetail(resultId: number): Promise<{ result: ResultDTO; exam: ExamDTO; allReviewed: boolean; submissions: CodeSubmissionDTO[] }> {
  return get(`/student/result/${resultId}`);
}

export async function getStudentProfile(): Promise<UserDTO> {
  return get<UserDTO>('/student/profile');
}

export async function getExamLeaderboard(examId: number) {
  return get(`/student/leaderboard/${examId}`);
}

export async function updateProfile(data: { fullName?: string; mobileNumber?: string }): Promise<{ success: boolean; message: string }> {
  return put<{ success: boolean; message: string }>('/student/profile', data);
}

export async function changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }): Promise<{ success: boolean; message: string }> {
  return put<{ success: boolean; message: string }>('/student/password', data);
}

export async function uploadProfilePicture(file: File): Promise<{ success: boolean; message: string; profilePicUrl: string }> {
  const formData = new FormData();
  formData.append('file', file);
  const resp = await apiClient.post('/student/profile/picture', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return resp.data;
}