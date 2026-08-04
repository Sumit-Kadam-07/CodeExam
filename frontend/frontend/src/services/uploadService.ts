import apiClient from '@/lib/interceptors';
import { UploadQuestionResult } from '@/types/upload';

export async function uploadAndGenerate(
  file: File,
  examId: number,
  language: string,
  difficulty: string,
  marks: number
): Promise<UploadQuestionResult> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('examId', examId.toString());
  formData.append('language', language);
  formData.append('difficulty', difficulty);
  formData.append('marks', marks.toString());

  const res = await apiClient.post('/ai/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}
