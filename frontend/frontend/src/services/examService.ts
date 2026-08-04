import { post } from './api';
import { ExamSubmitPayload, ExamSubmitResponse, CodeRunRequest, CodeExecutionResponse } from '@/types';

export async function submitExam(payload: ExamSubmitPayload): Promise<ExamSubmitResponse> {
  const res = await post<ExamSubmitResponse>('/exam/submit', payload);
  return res;
}

export async function runCode(req: CodeRunRequest): Promise<CodeExecutionResponse> {
  const res = await post<CodeExecutionResponse>('/code/run', req);
  return res;
}

export async function compileCode(req: CodeRunRequest): Promise<CodeExecutionResponse> {
  const res = await post<CodeExecutionResponse>('/code/compile', req);
  return res;
}