import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getResultDetail } from '@/services/studentService';
import Editor from '@monaco-editor/react';

function formatDateTime(iso: string): string {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString();
}

export default function ResultDetail() {
  const params = useParams<{ id: string }>();
  const resultId = Number(params.id);
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

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
    getResultDetail(resultId)
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

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading result…</div></div>
      </>
    );
  }

  if (!data) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Result not found.</div></div>
      </>
    );
  }

  const result = data.result;
  const exam = data.exam;
  const pct = result.totalMarks > 0 ? ((result.scoreAchieved / result.totalMarks) * 100).toFixed(1) : '0';

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Link href="/student/my-results" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-file-earmark-check text-primary me-2"></i>Result Details
          </h4>
        </div>

        {/* Summary card */}
        <div className="card shadow-sm mb-4">
          <div className="card-body p-4">
            <div className="row align-items-center">
              <div className="col-md-8">
                <h5 className="fw-bold">{exam?.title || 'Exam'}</h5>
                <p className="text-muted mb-1">
                  <i className="bi bi-calendar3 me-1"></i>Submitted: {formatDateTime(result.submissionTime)}
                </p>
              </div>
              <div className="col-md-4 text-md-end">
                {result.allReviewed ? (
                  <div>
                    <div className={`badge fs-5 ${Number(pct) >= 75 ? 'bg-success' : Number(pct) >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>
                      {pct}%
                    </div>
                    <div className="text-muted small mt-1">{result.scoreAchieved} / {result.totalMarks} marks</div>
                  </div>
                ) : (
                  <span className="badge bg-warning text-dark fs-6">
                    <i className="bi bi-hourglass-split me-1"></i>Pending Review
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Per-question detail */}
        {(data.submissions || []).map((sub: any, i: number) => (
          <div key={sub.id} className="card shadow-sm mb-4">
            <div className="card-header d-flex justify-content-between align-items-center">
              <div>
                <span className="fw-bold">Q{i + 1}: {sub.question?.title || 'Question'}</span>
                <span className={`ms-2 badge ${sub.question?.difficulty === 'Easy' ? 'bg-success' : sub.question?.difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'}`}>
                  {sub.question?.difficulty || '—'}
                </span>
              </div>
              <div>
                {sub.reviewed
                  ? <span className="badge bg-success">{sub.marksAwarded} / {sub.question?.marks ?? 0} pts</span>
                  : <span className="badge bg-warning text-dark">Pending</span>}
              </div>
            </div>
            <div className="card-body">
              <div className="row g-4">
                {/* Problem statement */}
                <div className="col-md-4">
                  <h6 className="fw-semibold text-muted">Problem Statement</h6>
                  <p className="small">{sub.question?.problemStatement || ''}</p>
                </div>
                {/* Code */}
                <div className="col-md-5">
                  <h6 className="fw-semibold text-muted">Your Code ({sub.language})</h6>
                  <div style={{ border: '1px solid #dee2e6', borderRadius: 6, overflow: 'hidden' }}>
                    <Editor
                      height="220px"
                      language={sub.language.toLowerCase() === 'java' ? 'java' : sub.language.toLowerCase() === 'python' ? 'python' : 'cpp'}
                      value={sub.sourceCode}
                      options={{ readOnly: true, minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }}
                      theme="vs-dark"
                    />
                  </div>
                </div>
                {/* Remarks */}
                <div className="col-md-3">
                  <h6 className="fw-semibold text-muted">Instructor Remarks</h6>
                  {sub.reviewed && sub.adminRemarks ? (
                    <div className="alert alert-info py-2 small">{sub.adminRemarks}</div>
                  ) : sub.reviewed ? (
                    <p className="text-muted small">No remarks provided.</p>
                  ) : (
                    <span className="badge bg-warning text-dark">Not yet reviewed</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
