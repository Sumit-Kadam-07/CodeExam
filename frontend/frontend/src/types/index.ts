export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

export interface UserDTO {
  id: number;
  username: string;
  fullName: string;
  role: string;
  email?: string;
  mobileNumber?: string;
  profilePicUrl?: string | null;
}

export interface TestCaseDTO {
  id: number;
  inputData: string;
  expectedOutput: string;
  weight: number;
}

export interface QuestionDTO {
  id: number;
  title: string;
  problemStatement: string;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  sampleInput?: string;
  sampleOutput?: string;
  explanation?: string;
  expectedSolution?: string;
  marks: number;
  difficulty: string;
  testCases: TestCaseDTO[];
  language?: string;
  starterCode?: string;
  executionTimeout?: number;
  memoryLimit?: number;
}

export interface ExamDTO {
  id: number;
  title: string;
  description?: string;
  durationInMinutes: number;
  published: boolean;
  questions: QuestionDTO[];
  totalMarks: number;
}

export interface CodeSubmissionDTO {
  id: number;
  question: QuestionDTO;
  sourceCode: string;
  language: string;
  marksAwarded: number;
  adminRemarks?: string;
  reviewed: boolean;
  status?: string;
  executionTimeMs?: number;
  compilationError?: string;
  runtimeError?: string;
  passedTestCases?: number;
  totalTestCases?: number;
}

export interface ResultDTO {
  id: number;
  exam: ExamDTO;
  submissionTime: string;
  scoreAchieved: number;
  totalMarks: number;
  allReviewed: boolean;
  submissions: CodeSubmissionDTO[];
}

export interface ExamSubmitPayload {
  examId: number;
  answers: Array<{
    questionId: number;
    sourceCode: string;
    language: string;
  }>;
}

export interface ExamSubmitResponse {
  success: boolean;
  resultId?: number;
  message: string;
}

export interface CodeRunRequest {
  language: string;
  sourceCode: string;
  input?: string;
}

export interface CodeExecutionResponse {
  success: boolean;
  stdout?: string;
  compilationError?: string;
  compilationOutput?: string;
  runtimeError?: string;
  executionTimeMs: number;
}