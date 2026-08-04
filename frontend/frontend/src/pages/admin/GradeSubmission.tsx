import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams, useLocation } from 'wouter';
import { getGradingData, saveGrades } from '@/services/adminService';
import Editor from '@monaco-editor/react';

export default function GradeSubmission() {
  const params = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const resultId = Number(params.id);
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [grades, setGrades] = useState<Record<number, { marks: number; remarks: string }>>({});

  if (isNaN(resultId)) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Invalid result ID.</div></div>
      </>
    );
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setSaved(false);
    getGradingData(resultId)
      .then((res) => {
        if (!cancelled) {
          setData(res);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [resultId]);

  useEffect(() => {
    if (data?.submissions) {
      const initial: Record<number, { marks: number; remarks: string }> = {};
      data.submissions.forEach((sub: any) => {
        initial[sub.id] = {
          marks: sub.marksAwarded >= 0 ? sub.marksAwarded : 0,
          remarks: sub.adminRemarks ?? '',
        };
      });
      setGrades(initial);
    }
  }, [data]);

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      const gradeMap: Record<string, string> = {};
      Object.entries(grades).forEach(([subIdStr, val]) => {
        gradeMap[`marks_${subIdStr}`] = String(val.marks);
        gradeMap[`remarks_${subIdStr}`] = val.remarks;
      });
      const res = await saveGrades(resultId, gradeMap);
      if ((res as any).success) {
        setSaved(true);
        setTimeout(() => navigate(`/admin/results/${(res as any).examId ?? data?.examId}`), 1200);
      } else {
        alert((res as any).message || 'Save failed');
      }
    } catch (err) {
      alert('Save failed');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading grading data…</div></div>
      </>
    );
  }

  if (!data) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Failed to load grading data.</div></div>
      </>
    );
  }

  const totalAwarded = Object.values(grades).reduce((s, g) => s + g.marks, 0);
  const totalPossible = data.submissions.reduce((s: number, sub: any) => s + (sub.maxMarks ?? 0), 0);

  return (
    <>
      <Navbar />
      {/* Sticky header */}
      <div className="sticky-top bg-white border-bottom shadow-sm px-3 py-2" style={{ zIndex: 100 }}>
        <div className="d-flex align-items-center justify-content-between flex-wrap gap-2">
          <div className="d-flex align-items-center gap-2">
            <Link href={`/admin/results/${data.examId}`} className="btn btn-outline-secondary btn-sm">
              <i className="bi bi-arrow-left"></i>
            </Link>
            <div>
              <h6 className="mb-0 fw-bold">
                <i className="bi bi-pencil-square text-primary me-2"></i>Grade Submission
              </h6>
              <small className="text-muted">
                {data.studentName} &nbsp;|&nbsp; {data.examTitle} &nbsp;|&nbsp; Submitted: {data.submissionTime ? new Date(data.submissionTime).toLocaleString() : 'N/A'}
              </small>
            </div>
          </div>
          <div className="d-flex align-items-center gap-3">
            <span className="fw-bold text-primary">{totalAwarded} / {totalPossible} pts</span>
            <button type="submit" form="gradeForm" className="btn btn-success btn-sm" disabled={saving}>
              <i className="bi bi-save me-1"></i>{saving ? 'Saving…' : 'Save Grades'}
            </button>
          </div>
        </div>
      </div>

      <div className="container mt-4">
        {saved && <div className="alert alert-success">Grades saved successfully!</div>}

        <form id="gradeForm" onSubmit={handleSave}>
          {data.submissions.map((sub: any) => (
            <div key={sub.id} className="card shadow-sm mb-4">
              <div className="card-header d-flex justify-content-between align-items-center">
                <div>
                  <span className="fw-bold">{sub.questionTitle}</span>
<span className={`ms-2 badge ${sub.reviewed ? 'bg-success' : 'bg-warning text-dark'}`}>
                     {sub.reviewed ? 'Graded' : 'Pending'}
                   </span>
                </div>
                <span className="text-muted small">Max: {sub.maxMarks} pts | Lang: {sub.language}</span>
              </div>
              <div className="card-body">
                <div className="row g-4">
                  <div className="col-md-4">
                    <h6 className="fw-semibold">Question</h6>
                    <p className="small text-muted">{sub.questionTitle}</p>
                  </div>
                  <div className="col-md-5">
                    <h6 className="fw-semibold">Submitted Code ({sub.language})</h6>
                    <div style={{ border: '1px solid #dee2e6', borderRadius: 6, overflow: 'hidden' }}>
                      <Editor
                        height="280px"
                        language={sub.language.toLowerCase() === 'java' ? 'java' : sub.language.toLowerCase() === 'python' ? 'python' : 'cpp'}
                        value={sub.sourceCode}
                        options={{ readOnly: true, minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }}
                        theme="vs-dark"
                      />
                    </div>
                  </div>
                  <div className="col-md-3">
                    <h6 className="fw-semibold">Grade</h6>
                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Marks Awarded (max {sub.maxMarks})</label>
                      <input
                        type="number"
                        className="form-control"
                        min={0}
                        max={sub.maxMarks}
                        value={grades[sub.id]?.marks ?? 0}
                        onChange={e => setGrades(prev => ({ ...prev, [sub.id]: { ...prev[sub.id], marks: Number(e.target.value) } }))}
                      />
                    </div>
                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Remarks (optional)</label>
                      <textarea
                        className="form-control"
                        rows={4}
                        placeholder="Feedback for the student…"
                        value={grades[sub.id]?.remarks ?? ''}
                        onChange={e => setGrades(prev => ({ ...prev, [sub.id]: { ...prev[sub.id], remarks: e.target.value } }))}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </form>
      </div>
    </>
  );
}
