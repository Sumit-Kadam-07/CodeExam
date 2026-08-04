import { get, post, put, del } from './api';
import { QuestionDTO } from '@/types';

export async function getExamQuestions(examId: number): Promise<QuestionDTO[]> {
  return get<QuestionDTO[]>(`/admin/exams/${examId}/questions`);
}

export async function getQuestion(questionId: number): Promise<QuestionDTO> {
  return get<QuestionDTO>(`/admin/questions/${questionId}`);
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
