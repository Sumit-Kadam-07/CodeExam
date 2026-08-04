import { get, post, put, del } from './api';
import { ApiResponse, ExamDTO, QuestionDTO, UserDTO, ResultDTO, CodeSubmissionDTO } from '@/types';

export interface DashboardData {
  totalStudents: number;
  totalExams: number;
  totalQuestions: number;
  totalSubmissions: number;
  chartLabels: string[];
  chartData: number[];
  recentResults: Array<{
    id: number;
    examId: number | null;
    studentName: string;
    examName: string;
    scoreAchieved: number;
    totalMarks: number;
    submissionTime: string | null;
  }>;
}

export async function getAdminDashboard(): Promise<DashboardData> {
  return get<DashboardData>('/admin/dashboard');
}

export async function getAllExams(): Promise<ExamDTO[]> {
  return get<ExamDTO[]>('/admin/exams');
}

export async function getExam(examId: number): Promise<ExamDTO> {
  return get<ExamDTO>(`/admin/exams/${examId}`);
}

export async function createExam(data: { title: string; description?: string; durationInMinutes: number }): Promise<ExamDTO> {
  return post<ExamDTO>('/admin/exams', data);
}

export async function updateExam(examId: number, data: { title: string; description?: string; durationInMinutes: number }): Promise<ExamDTO> {
  const res = await put<ApiResponse<ExamDTO>>(`/admin/exams/${examId}`, data);
  if (res.success && res.data) return res.data;
  throw new Error(res.message || 'Failed to update exam');
}

export async function deleteExam(examId: number): Promise<{ success: boolean; message: string }> {
  return del<{ success: boolean; message: string }>(`/admin/exams/${examId}`);
}

export async function publishExam(examId: number): Promise<{ success: boolean; message: string }> {
  return post(`/admin/exams/${examId}/publish`, {});
}

export async function unpublishExam(examId: number): Promise<{ success: boolean; message: string }> {
  return post(`/admin/exams/${examId}/unpublish`, {});
}

export async function getExamQuestions(examId: number): Promise<QuestionDTO[]> {
  return get<QuestionDTO[]>(`/admin/exams/${examId}/questions`);
}

export async function addQuestion(examId: number, data: {
  title: string;
  problemStatement: string;
  language?: string;
  difficulty?: string;
  marks?: number;
  starterCode?: string;
  executionTimeout?: number;
  memoryLimit?: number;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  sampleInput?: string;
  sampleOutput?: string;
  explanation?: string;
  expectedSolution?: string;
  testCases: Array<{
    input: string;
    expectedOutput: string;
    weight?: number;
    sample?: boolean;
  }>;
}): Promise<{ success: boolean; message: string; questionId: number }> {
  return post(`/admin/exams/${examId}/questions`, data);
}

export async function updateQuestion(questionId: number, data: {
  title?: string;
  problemStatement?: string;
  language?: string;
  difficulty?: string;
  marks?: number;
  starterCode?: string;
  executionTimeout?: number;
  memoryLimit?: number;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  sampleInput?: string;
  sampleOutput?: string;
  explanation?: string;
  expectedSolution?: string;
  testCases?: Array<{
    input: string;
    expectedOutput: string;
    weight?: number;
    sample?: boolean;
  }>;
}): Promise<{ success: boolean; message: string }> {
  return put<{ success: boolean; message: string }>(`/admin/questions/${questionId}`, data);
}

export async function deleteQuestion(questionId: number, examId?: number): Promise<{ success: boolean; message: string }> {
  const url = examId ? `/admin/questions/${questionId}?examId=${examId}` : `/admin/questions/${questionId}`;
  return del<{ success: boolean; message: string }>(url);
}

export async function getQuestion(questionId: number): Promise<QuestionDTO> {
  return get<QuestionDTO>(`/admin/questions/${questionId}`);
}

export async function getStudents(): Promise<UserDTO[]> {
  return get<UserDTO[]>('/admin/students');
}

export async function deleteStudent(studentId: number): Promise<{ success: boolean; message: string }> {
  return del<{ success: boolean; message: string }>(`/admin/students/${studentId}`);
}

export async function resetStudentPassword(studentId: number, newPassword: string): Promise<{ success: boolean; message: string }> {
  return post(`/admin/students/${studentId}/reset-password`, { newPassword });
}

export async function getExamResults(examId: number): Promise<Array<{
  id: number;
  studentName: string;
  examName: string;
  scoreAchieved: number;
  totalMarks: number;
  percentage: number;
  submissionTime: string | null;
  allReviewed: boolean;
}>> {
  return get(`/admin/exams/${examId}/results`);
}

export async function getGradingData(resultId: number): Promise<{
  resultId: number;
  studentName: string;
  studentUsername: string;
  examTitle: string;
  examId: number;
  scoreAchieved: number;
  totalMarks: number;
  submissions: Array<{
    id: number;
    questionId: number | null;
    questionTitle: string;
    language: string;
    sourceCode: string;
    marksAwarded: number;
    maxMarks: number;
    reviewed: boolean;
    adminRemarks?: string;
    status?: string;
    executionTimeMs?: number;
    compilationError?: string;
    runtimeError?: string;
    passedTestCases?: number;
    totalTestCases?: number;
  }>;
}> {
  return get(`/admin/results/${resultId}/grade`);
}

export async function saveGrades(resultId: number, grades: Record<string, string>): Promise<{ success: boolean; message: string; examId: number }> {
  return post(`/admin/results/${resultId}/grade`, { grades });
}

export async function getGlobalLeaderboard(): Promise<{
  entries: Array<{
    studentUsername: string;
    studentName: string;
    totalScore: number;
    avgPercentage: number;
    avgPercentageFormatted: string;
    gradedExams: number;
    examsTaken: number;
    rank: number;
    initials: string;
  }>;
  topThree: any[];
  totalStudents: number;
  totalExams: number;
  publishedExams: number;
}> {
  return get('/admin/leaderboard');
}

export async function getExamLeaderboard(examId: number): Promise<{
  exam: { id: number; title: string };
  entries: Array<any>;
  topThree: any[];
  avgScore: number;
  avgPercentage: number;
  highScore: number;
  passRate: number;
  gradedCount: number;
  pendingCount: number;
  totalCount: number;
  dist: any[];
}> {
  return get(`/admin/leaderboard/${examId}`);
}