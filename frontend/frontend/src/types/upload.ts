export interface UploadQuestionResult {
  success: boolean;
  message: string;
  savedCount: number;
  questionIds: number[];
  failedCount: number;
  skipped: number;
  errors?: string[];
  data?: {
    originalFilename: string;
    fileType: string;
    extractedText: string;
    processingStatus: string;
    aiResult: {
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
      testCases: Array<{
        input: string;
        expectedOutput: string;
        weight: number;
        sample: boolean;
      }>;
    };
  };
}
