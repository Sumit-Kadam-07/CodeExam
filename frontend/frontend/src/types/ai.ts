export interface AiGenerateRequest {
  topic: string;
  language: string;
  difficulty: string;
  marks: number;
}

export interface AiTestCase {
  input: string;
  expectedOutput: string;
  weight: number;
  sample: boolean;
}

export interface AiGenerateResponse {
  title: string;
  problemStatement: string;
  inputFormat: string;
  outputFormat: string;
  constraints: string;
  sampleInput: string;
  sampleOutput: string;
  explanation: string;
  starterCode: string;
  expectedSolution: string;
  difficulty: string;
  marks: number;
  language: string;
  testCases: AiTestCase[];
}

export interface AiGenerateResult {
  success: boolean;
  message: string;
  data: AiGenerateResponse;
}
