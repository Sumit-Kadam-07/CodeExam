import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getResultDetail } from '@/services/studentService';

function formatDateTime(iso: string): string {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString();
}

export default function ReviewExam() {
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
        <div className="container mt-4"><div className="text-center p-4">Loading review…</div></div>
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

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Link href="/student/my-results" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-code-slash text-primary me-2"></i>Review Submission
          </h4>
        </div>
        <p className="text-muted mb-4">
          <strong>{exam?.title || 'Exam'}</strong> — Submitted: {formatDateTime(result.submissionTime)}
        </p>

        {(data.submissions || []).map((sub: any, i: number) => (
          <div key={sub.id} className="card shadow-sm mb-4">
            <div className="card-header d-flex justify-content-between align-items-center">
              <div>
                <span className="fw-bold">Q{i + 1}: {sub.question?.title || 'Question'}</span>
                <span className={`ms-2 badge ${sub.question?.difficulty === 'Easy' ? 'bg-success' : sub.question?.difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'}`}>
                  {sub.question?.difficulty || '—'}
                </span>
              </div>
              {sub.reviewed
                ? <span className="badge bg-success">{sub.marksAwarded} / {sub.question?.marks ?? 0} pts</span>
                : <span className="badge bg-warning text-dark">Pending</span>}
            </div>
            <div className="card-body">
              <div className="row g-3 mb-3">
                <div className="col-12">
                  <h6 className="fw-semibold">Problem</h6>
                  <p className="small text-muted">{sub.question?.problemStatement || ''}</p>
                </div>
              </div>
              <h6 className="fw-semibold">Submitted Code ({sub.language})</h6>
              <pre
                className="p-3 rounded"
                style={{
                  background: '#212529',
                  color: '#f8f9fa',
                  fontSize: 13,
                  fontFamily: 'Consolas, "Courier New", monospace',
                  overflowX: 'auto',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-all',
                }}
              >
                {sub.sourceCode}
              </pre>
              {sub.reviewed && sub.adminRemarks && (
                <div className="mt-3">
                  <h6 className="fw-semibold">Instructor Remarks</h6>
                  <div className="alert alert-info py-2 small">{sub.adminRemarks}</div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
