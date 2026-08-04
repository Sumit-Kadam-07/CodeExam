import QuestionForm from './QuestionForm';
import { QuestionFormState, TestCaseItem } from '@/types/question';

interface ManualQuestionTabProps {
  form: QuestionFormState;
  testCases: TestCaseItem[];
  showAdvanced: boolean;
  saving: boolean;
  onFormChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  onStarterCodeChange: (value: string) => void;
  onToggleAdvanced: () => void;
  onAddTestCase: () => void;
  onRemoveTestCase: (id: number) => void;
  onUpdateTestCase: (id: number, field: string, value: string | number | boolean) => void;
  onSubmit: (e: React.FormEvent) => void;
}

export default function ManualQuestionTab(props: ManualQuestionTabProps) {
  return <QuestionForm {...props} />;
}
