import { QuestionFormState } from '@/types/question';
import { LANGUAGE_BOILERPLATES } from '@/utils/boilerplates';
import MonacoCodeEditor from './MonacoCodeEditor';
import AdvancedFields from './AdvancedFields';
import TestCaseEditor from './TestCaseEditor';
import { TestCaseItem } from '@/types/question';

interface QuestionFormProps {
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

export default function QuestionForm({
  form,
  testCases,
  showAdvanced,
  saving,
  onFormChange,
  onStarterCodeChange,
  onToggleAdvanced,
  onAddTestCase,
  onRemoveTestCase,
  onUpdateTestCase,
  onSubmit,
}: QuestionFormProps) {
  return (
    <form onSubmit={onSubmit}>
      <div className="row g-3 mb-3">
        <div className="col-md-6">
          <label className="form-label fw-semibold" htmlFor="questionTitle">
            Title <span className="text-danger">*</span>
          </label>
          <input
            id="questionTitle"
            type="text"
            name="title"
            className="form-control"
            placeholder="Question title"
            value={form.title}
            onChange={onFormChange}
            required
          />
        </div>
        <div className="col-md-3">
          <label className="form-label fw-semibold">Difficulty</label>
          <select name="difficulty" className="form-select" value={form.difficulty} onChange={onFormChange}>
            <option>Easy</option>
            <option>Medium</option>
            <option>Hard</option>
          </select>
        </div>
        <div className="col-md-3">
          <label className="form-label fw-semibold" htmlFor="questionMarks">
            Marks <span className="text-danger">*</span>
          </label>
          <input
            id="questionMarks"
            type="number"
            name="marks"
            className="form-control"
            min={1}
            value={form.marks}
            onChange={onFormChange}
            required
          />
        </div>
      </div>

      <div className="mb-3">
        <label className="form-label fw-semibold">
          Language <span className="text-danger">*</span>
        </label>
        <select name="language" className="form-select mb-2" value={form.language} onChange={onFormChange}>
          <option>Java</option>
          <option>Python</option>
          <option>C</option>
          <option>C++</option>
        </select>
        <MonacoCodeEditor language={form.language} value={form.starterCode} onChange={onStarterCodeChange} />
        <small className="text-muted mt-1 d-block">
          <i className="bi bi-info-circle me-1"></i>
          Edit the boilerplate code template above. This will be the starting code for students.
        </small>
      </div>

      <div className="mb-3">
        <label className="form-label fw-semibold" htmlFor="problemStatement">
          Problem Statement <span className="text-danger">*</span>
        </label>
        <textarea
          id="problemStatement"
          name="problemStatement"
          className="form-control"
          rows={4}
          placeholder="Describe the problem in detail..."
          value={form.problemStatement}
          onChange={onFormChange}
          required
        />
      </div>

      <div className="mb-3">
        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={onToggleAdvanced}>
          <i className={`bi bi-chevron-${showAdvanced ? 'up' : 'down'} me-1`}></i>
          {showAdvanced ? 'Hide' : 'Show'} Advanced Fields
        </button>
      </div>

      {showAdvanced && <AdvancedFields form={form} onChange={onFormChange} />}

      <TestCaseEditor
        testCases={testCases}
        onAdd={onAddTestCase}
        onRemove={onRemoveTestCase}
        onUpdate={onUpdateTestCase}
      />

      <div className="d-flex gap-2 mt-4">
        <button type="submit" className="btn btn-success" disabled={saving}>
          <i className="bi bi-save me-2"></i>
          {saving ? 'Saving...' : 'Save Question'}
        </button>
      </div>
    </form>
  );
}
