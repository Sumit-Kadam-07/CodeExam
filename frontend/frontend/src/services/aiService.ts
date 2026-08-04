import apiClient from '@/lib/interceptors';
import { AiGenerateRequest, AiGenerateResult } from '@/types/ai';

export async function generateAiQuestion(data: AiGenerateRequest): Promise<AiGenerateResult> {
  const res = await apiClient.post('/ai/generate', data);
  return res.data;
}
