export interface QuestionFormState {
  title: string;
  difficulty: string;
  marks: number;
  problemStatement: string;
  inputFormat: string;
  outputFormat: string;
  constraints: string;
  sampleInput: string;
  sampleOutput: string;
  explanation: string;
  expectedSolution: string;
  language: string;
  starterCode: string;
}

export interface TestCaseItem {
  id: number;
  inputData: string;
  expectedOutput: string;
  weight: number;
  sample: boolean;
}

export const DEFAULT_QUESTION_FORM: QuestionFormState = {
  title: '',
  difficulty: 'Easy',
  marks: 10,
  problemStatement: '',
  inputFormat: '',
  outputFormat: '',
  constraints: '',
  sampleInput: '',
  sampleOutput: '',
  explanation: '',
  expectedSolution: '',
  language: 'Java',
  starterCode: `import java.util.*;\n\npublic class Main {\n\n    public static void main(String[] args) {\n\n    }\n\n}`,
};

export const DEFAULT_TEST_CASES: TestCaseItem[] = [
  { id: Date.now(), inputData: '', expectedOutput: '', weight: 1, sample: false },
];
