import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';
import { getStudentResults } from '@/services/studentService';

function formatDateTime(iso: string): string {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleString();
}

export default function MyResults() {
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getStudentResults()
      .then((data) => {
        if (!cancelled) {
          setResults(data || []);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <h2 className="fw-bold mb-4">
          <i className="bi bi-bar-chart-fill text-primary me-2"></i>My Results
        </h2>

        {loading ? (
          <div className="text-center p-4">Loading results…</div>
        ) : results.length === 0 ? (
          <div className="alert alert-info">You have not submitted any exams yet.</div>
        ) : (
          <div className="row g-4">
            {results.map((r: any) => {
              const pct = r.totalMarks > 0 ? ((r.scoreAchieved / r.totalMarks) * 100).toFixed(1) : null;
              return (
                <div key={r.id} className="col-md-6 col-xl-4">
                  <div className="card shadow-sm h-100">
                    <div className="card-body">
                      <h5 className="fw-bold card-title mb-1">{r.exam?.title || 'Exam'}</h5>
                      <p className="text-muted small mb-3">
                        <i className="bi bi-calendar3 me-1"></i>{formatDateTime(r.submissionTime)}
                      </p>
                      {r.allReviewed ? (
                        <div className="d-flex align-items-center gap-3 mb-3">
                          <div
                            className="d-flex align-items-center justify-content-center text-white fw-bold rounded-circle"
                            style={{
                              width: 64, height: 64,
                              background: Number(pct) >= 75 ? '#198754' : Number(pct) >= 50 ? '#ffc107' : '#dc3545',
                              fontSize: 18,
                            }}
                          >
                            {pct}%
                          </div>
                          <div>
                            <div className="fw-semibold">{r.scoreAchieved} / {r.totalMarks} marks</div>
                            <span className={`badge ${Number(pct) >= 75 ? 'bg-success' : Number(pct) >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>
                              {Number(pct) >= 75 ? 'Excellent' : Number(pct) >= 50 ? 'Pass' : 'Needs Improvement'}
                            </span>
                          </div>
                        </div>
                      ) : (
                        <div className="mb-3">
                          <span className="badge bg-warning text-dark fs-6">
                            <i className="bi bi-hourglass-split me-1"></i>Pending Review
                          </span>
                        </div>
                      )}
                    </div>
                    <div className="card-footer bg-transparent d-flex gap-2">
                      <Link href={`/student/result-detail/${r.id}`} className="btn btn-sm btn-outline-primary flex-grow-1">
                        <i className="bi bi-info-circle me-1"></i>Details
                      </Link>
                      <Link href={`/student/review/${r.id}`} className="btn btn-sm btn-outline-secondary flex-grow-1">
                        <i className="bi bi-code-slash me-1"></i>Review
                      </Link>
                      {r.exam?.id && (
                        <Link href={`/student/leaderboard/${r.exam.id}`} className="btn btn-sm btn-outline-warning flex-grow-1">
                          <i className="bi bi-trophy me-1"></i>Board
                        </Link>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </>
  );
}
