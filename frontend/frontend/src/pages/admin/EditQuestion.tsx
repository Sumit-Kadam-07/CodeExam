import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams, useLocation } from 'wouter';
import { getQuestion, updateQuestion, deleteQuestion } from '@/services/adminService';

export default function EditQuestion() {
  const params = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const questionId = Number(params.id);
  const [question, setQuestion] = useState<any>(null);
  const [parentExamId, setParentExamId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

const [form, setForm] = useState({
     title: '', difficulty: 'Easy', marks: 10,
     problemStatement: '', inputFormat: '', outputFormat: '',
     constraints: '', sampleInput: '', sampleOutput: '',
     explanation: '', expectedSolution: '', starterCode: '', language: 'Java',
   });
  const [testCases, setTestCases] = useState<any[]>([]);
  const [aiLoading, setAiLoading] = useState(false);

  if (isNaN(questionId)) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Invalid question ID.</div></div>
      </>
    );
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getQuestion(questionId)
      .then((q) => {
        if (!cancelled) {
          setQuestion(q);
          setParentExamId((q as any).exam?.id ?? null);
setForm({
             title: q.title,
             difficulty: q.difficulty,
             marks: q.marks,
             problemStatement: q.problemStatement,
             inputFormat: q.inputFormat ?? '',
             outputFormat: q.outputFormat ?? '',
             constraints: q.constraints ?? '',
             sampleInput: q.sampleInput ?? '',
             sampleOutput: q.sampleOutput ?? '',
             explanation: q.explanation ?? '',
             expectedSolution: q.expectedSolution ?? '',
             starterCode: q.starterCode ?? '',
             language: q.language ?? 'Java',
           });
          setTestCases(q.testCases?.length ? q.testCases : [{ id: Date.now(), inputData: '', expectedOutput: '', weight: 1, sample: false }]);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [questionId]);

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  function addTestCase() {
    setTestCases([...testCases, { id: Date.now(), inputData: '', expectedOutput: '', weight: 1 }]);
  }

  function removeTestCase(id: number) {
    if (testCases.length > 1) setTestCases(testCases.filter(tc => tc.id !== id));
  }

  function updateTestCase(id: number, field: string, value: string | number) {
    setTestCases(testCases.map(tc => tc.id === id ? { ...tc, [field]: value } : tc));
  }

  function handleAiDraft() {
    setAiLoading(true);
    setTimeout(() => {
      setForm(prev => ({
        ...prev,
        problemStatement: prev.problemStatement || `Write a program that ${prev.title.toLowerCase()}.`,
        inputFormat: 'First line contains an integer N.',
        outputFormat: 'Print the result on a single line.',
        constraints: '1 ≤ N ≤ 1000',
        sampleInput: '5',
        sampleOutput: '120',
      }));
      setAiLoading(false);
    }, 1200);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
await updateQuestion(questionId, {
         title: form.title,
         problemStatement: form.problemStatement,
         language: form.language,
         difficulty: form.difficulty,
         marks: Number(form.marks),
         starterCode: form.starterCode,
         inputFormat: form.inputFormat,
         outputFormat: form.outputFormat,
         constraints: form.constraints,
         sampleInput: form.sampleInput,
         sampleOutput: form.sampleOutput,
         explanation: form.explanation,
         expectedSolution: form.expectedSolution,
         testCases: testCases.map(tc => ({
           input: tc.inputData,
           expectedOutput: tc.expectedOutput,
           weight: tc.weight,
           sample: tc.sample,
         })),
       });
      if (parentExamId) {
        navigate(`/admin/exam/manage-questions/${parentExamId}`);
      }
    } catch (err) {
      setError('Failed to update question.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete this question? This cannot be undone.')) return;
    try {
      const res = await deleteQuestion(questionId, parentExamId ?? undefined);
      if ((res as any).success) {
        if (parentExamId) navigate(`/admin/exam/manage-questions/${parentExamId}`);
      } else {
        alert((res as any).message || 'Delete failed');
      }
    } catch (error: any) {
      alert(error?.response?.data?.message || error?.message || 'Failed to delete question. Please try again.');
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading question…</div></div>
      </>
    );
  }

  const parentExamIdStr = parentExamId ? String(parentExamId) : '';

  return (
    <>
      <Navbar />
      <div className="container mt-4" style={{ maxWidth: 860 }}>
        <div className="d-flex align-items-center gap-2 mb-3">
          {parentExamIdStr ? (
            <Link href={`/admin/exam/manage-questions/${parentExamIdStr}`} className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-arrow-left"></i>
            </Link>
          ) : (
            <Link href="/admin/manage-exams" className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-arrow-left"></i>
            </Link>
          )}
          <h4 className="fw-bold mb-0">
            <i className="bi bi-pencil-square text-primary me-2"></i>Edit Question
          </h4>
          <button type="button" className="btn btn-outline-info btn-sm ms-auto" onClick={handleAiDraft} disabled={aiLoading}>
            {aiLoading
              ? <><span className="spinner-border spinner-border-sm me-1"></span>Generating…</>
              : <><i className="bi bi-stars me-1"></i>AI Draft</>}
          </button>
          {parentExamIdStr && (
            <button type="button" className="btn btn-outline-danger btn-sm" onClick={handleDelete}>
              <i className="bi bi-trash me-1"></i>Delete
            </button>
          )}
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        <div className="card shadow-sm">
          <div className="card-body p-4">
            <form onSubmit={handleSubmit}>
<div className="row g-3 mb-3">
                 <div className="col-md-6">
                   <label className="form-label fw-semibold">Title <span className="text-danger">*</span></label>
                   <input type="text" name="title" className="form-control" value={form.title} onChange={handleChange} required />
                 </div>
                 <div className="col-md-3">
                   <label className="form-label fw-semibold">Difficulty</label>
                   <select name="difficulty" className="form-select" value={form.difficulty} onChange={handleChange}>
                     <option>Easy</option><option>Medium</option><option>Hard</option>
                   </select>
                 </div>
                 <div className="col-md-3">
                   <label className="form-label fw-semibold">Marks <span className="text-danger">*</span></label>
                   <input type="number" name="marks" className="form-control" min={1} value={form.marks} onChange={handleChange} required />
                 </div>
               </div>

               <div className="mb-3">
                 <label className="form-label fw-semibold">Language</label>
                 <select name="language" className="form-select" value={form.language} onChange={handleChange}>
                   <option>Java</option><option>C</option><option>C++</option><option>Python</option>
                 </select>
               </div>

               <div className="mb-3">
                 <label className="form-label fw-semibold">Starter Code</label>
                 <textarea name="starterCode" className="form-control font-monospace" rows={8}
                   value={form.starterCode} onChange={handleChange} placeholder="Paste the starter code for students here..." />
                 <small className="text-muted mt-1 d-block">
                   <i className="bi bi-info-circle me-1"></i>
                   This is the starting code template students will see.
                 </small>
               </div>

               <div className="mb-3">
                 <label className="form-label fw-semibold">Problem Statement <span className="text-danger">*</span></label>
                 <textarea name="problemStatement" className="form-control" rows={4}
                   value={form.problemStatement} onChange={handleChange} required />
               </div>

              <div className="row g-3 mb-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold">Input Format</label>
                  <textarea name="inputFormat" className="form-control" rows={2}
                    value={form.inputFormat} onChange={handleChange} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold">Output Format</label>
                  <textarea name="outputFormat" className="form-control" rows={2}
                    value={form.outputFormat} onChange={handleChange} />
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold">Constraints</label>
                  <input type="text" name="constraints" className="form-control"
                    value={form.constraints} onChange={handleChange} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold">Sample Input</label>
                  <textarea name="sampleInput" className="form-control" rows={2}
                    value={form.sampleInput} onChange={handleChange} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold">Sample Output</label>
                  <textarea name="sampleOutput" className="form-control" rows={2}
                    value={form.sampleOutput} onChange={handleChange} />
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold">Explanation</label>
                  <textarea name="explanation" className="form-control" rows={2}
                    value={form.explanation} onChange={handleChange} />
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold">Expected Solution</label>
                  <textarea name="expectedSolution" className="form-control" rows={3}
                    value={form.expectedSolution} onChange={handleChange} />
                </div>
              </div>

              <hr />
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h6 className="fw-bold mb-0"><i className="bi bi-list-check me-2"></i>Test Cases</h6>
                <button type="button" className="btn btn-sm btn-outline-success" onClick={addTestCase}>
                  <i className="bi bi-plus me-1"></i>Add Test Case
                </button>
              </div>
              {testCases.map((tc, i) => (
                <div key={tc.id} className="card bg-light mb-2">
                  <div className="card-body p-3">
                    <div className="d-flex justify-content-between mb-2">
                      <span className="fw-semibold small">Test Case #{i + 1}</span>
                      <button type="button" className="btn btn-sm btn-outline-danger"
                        onClick={() => removeTestCase(tc.id)} disabled={testCases.length === 1}>
                        <i className="bi bi-trash"></i>
                      </button>
                    </div>
                    <div className="row g-2">
                      <div className="col-md-5">
                        <label className="form-label small">Input</label>
                        <textarea className="form-control form-control-sm" rows={2}
                          value={tc.inputData} onChange={e => updateTestCase(tc.id, 'inputData', e.target.value)} />
                      </div>
                      <div className="col-md-5">
                        <label className="form-label small">Expected Output</label>
                        <textarea className="form-control form-control-sm" rows={2}
                          value={tc.expectedOutput} onChange={e => updateTestCase(tc.id, 'expectedOutput', e.target.value)} />
                      </div>
                      <div className="col-md-2">
                        <label className="form-label small">Weight</label>
                        <input type="number" className="form-control form-control-sm" min={1}
                          value={tc.weight} onChange={e => updateTestCase(tc.id, 'weight', Number(e.target.value))} />
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              <div className="d-flex gap-2 mt-4">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  <i className="bi bi-save me-2"></i>{saving ? 'Saving…' : 'Save Changes'}
                </button>
                {parentExamIdStr ? (
                  <Link href={`/admin/exam/manage-questions/${parentExamIdStr}`} className="btn btn-secondary">
                    Cancel
                  </Link>
                ) : (
                  <Link href="/admin/manage-exams" className="btn btn-secondary">
                    Cancel
                  </Link>
                )}
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}
