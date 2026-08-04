import { QuestionFormState } from '@/types/question';

interface AdvancedFieldsProps {
  form: QuestionFormState;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}

export default function AdvancedFields({ form, onChange }: AdvancedFieldsProps) {
  return (
    <div className="row g-3 mb-3">
      <div className="col-md-6">
        <label className="form-label fw-semibold">Input Format</label>
        <textarea name="inputFormat" className="form-control" rows={2} value={form.inputFormat} onChange={onChange} />
      </div>
      <div className="col-md-6">
        <label className="form-label fw-semibold">Output Format</label>
        <textarea name="outputFormat" className="form-control" rows={2} value={form.outputFormat} onChange={onChange} />
      </div>
      <div className="col-12">
        <label className="form-label fw-semibold">Constraints</label>
        <input type="text" name="constraints" className="form-control" placeholder="e.g. 1 <= N <= 100" value={form.constraints} onChange={onChange} />
      </div>
      <div className="col-md-6">
        <label className="form-label fw-semibold">Sample Input</label>
        <textarea name="sampleInput" className="form-control" rows={2} value={form.sampleInput} onChange={onChange} />
      </div>
      <div className="col-md-6">
        <label className="form-label fw-semibold">Sample Output</label>
        <textarea name="sampleOutput" className="form-control" rows={2} value={form.sampleOutput} onChange={onChange} />
      </div>
      <div className="col-12">
        <label className="form-label fw-semibold">Explanation</label>
        <textarea name="explanation" className="form-control" rows={2} value={form.explanation} onChange={onChange} />
      </div>
      <div className="col-12">
        <label className="form-label fw-semibold">Expected Solution</label>
        <textarea name="expectedSolution" className="form-control" rows={3} value={form.expectedSolution} onChange={onChange} />
      </div>
    </div>
  );
}
