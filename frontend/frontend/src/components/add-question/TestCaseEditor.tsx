import { TestCaseItem } from '@/types/question';

interface TestCaseEditorProps {
  testCases: TestCaseItem[];
  onAdd: () => void;
  onRemove: (id: number) => void;
  onUpdate: (id: number, field: string, value: string | number | boolean) => void;
}

export default function TestCaseEditor({ testCases, onAdd, onRemove, onUpdate }: TestCaseEditorProps) {
  return (
    <>
      <hr />
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h6 className="fw-bold mb-0">
          <i className="bi bi-list-check me-2"></i>Test Cases
        </h6>
        <button type="button" className="btn btn-sm btn-outline-success" onClick={onAdd}>
          <i className="bi bi-plus me-1"></i>Add Test Case
        </button>
      </div>

      {testCases.map((tc, i) => (
        <div key={tc.id} className="card bg-light mb-2">
          <div className="card-body p-3">
            <div className="d-flex justify-content-between mb-2">
              <div className="d-flex align-items-center gap-2">
                <span className="fw-semibold small">Test Case #{i + 1}</span>
                <div className="form-check form-check-inline mb-0">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id={`tc-sample-${tc.id}`}
                    checked={tc.sample}
                    onChange={(e) => onUpdate(tc.id, 'sample', e.target.checked)}
                  />
                  <label className="form-check-label small" htmlFor={`tc-sample-${tc.id}`}>Sample</label>
                </div>
              </div>
              {testCases.length > 1 && (
                <button type="button" className="btn btn-sm btn-outline-danger" onClick={() => onRemove(tc.id)}>
                  <i className="bi bi-trash"></i>
                </button>
              )}
            </div>
            <div className="row g-2">
              <div className="col-md-5">
                <label className="form-label small">Input</label>
                <textarea
                  className="form-control form-control-sm"
                  rows={2}
                  value={tc.inputData}
                  onChange={(e) => onUpdate(tc.id, 'inputData', e.target.value)}
                />
              </div>
              <div className="col-md-5">
                <label className="form-label small">Expected Output</label>
                <textarea
                  className="form-control form-control-sm"
                  rows={2}
                  value={tc.expectedOutput}
                  onChange={(e) => onUpdate(tc.id, 'expectedOutput', e.target.value)}
                />
              </div>
              <div className="col-md-2">
                <label className="form-label small">Weight</label>
                <input
                  type="number"
                  className="form-control form-control-sm"
                  min={1}
                  value={tc.weight}
                  onChange={(e) => onUpdate(tc.id, 'weight', Number(e.target.value))}
                />
              </div>
            </div>
          </div>
        </div>
      ))}
    </>
  );
}
