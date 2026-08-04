import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getExamResults } from '@/services/adminService';

export default function ViewResults() {
  const params = useParams<{ id: string }>();
  const examId = Number(params.id);
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [examTitle, setExamTitle] = useState('');

  if (isNaN(examId)) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Invalid exam ID.</div></div>
      </>
    );
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getExamResults(examId)
      .then((data) => {
        if (!cancelled) {
          setResults(data || []);
          if (data && data.length > 0 && data[0].examName) {
            setExamTitle(data[0].examName);
          }
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [examId]);

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Link href="/admin/manage-exams" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-bar-chart-fill text-primary me-2"></i>View Results
          </h4>
        </div>
        <p className="text-muted mb-4">
          Exam: <strong>{examTitle || `#${examId}`}</strong>
        </p>

        {loading ? (
          <div className="text-center p-4">Loading results…</div>
        ) : results.length === 0 ? (
          <div className="alert alert-info">No submissions yet for this exam.</div>
        ) : (
          <div className="card shadow-sm">
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr>
                    <th>#</th>
                    <th>Student</th>
                    <th>Submitted At</th>
                    <th>Score</th>
                    <th>Percentage</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, idx) => {
                    const pct = r.totalMarks > 0 ? ((r.scoreAchieved / r.totalMarks) * 100).toFixed(1) : '0';
                    return (
                      <tr key={r.id}>
                        <td>{idx + 1}</td>
<td>
                           <div className="fw-semibold">{r.studentName}</div>
                         </td>
                        <td>{r.submissionTime || '—'}</td>
                        <td>
                          {r.allReviewed
                            ? <span className="fw-bold">{r.scoreAchieved} / {r.totalMarks}</span>
                            : <span className="text-muted">Pending</span>}
                        </td>
                        <td>
                          {r.allReviewed
                            ? (
                              <span className={`badge ${Number(pct) >= 75 ? 'bg-success' : Number(pct) >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>
                                {pct}%
                              </span>
                            )
                            : <span className="text-muted">—</span>}
                        </td>
                        <td>
                          {r.allReviewed
                            ? <span className="badge bg-success"><i className="bi bi-check-circle me-1"></i>Graded</span>
                            : <span className="badge bg-warning text-dark"><i className="bi bi-hourglass-split me-1"></i>Pending</span>}
                        </td>
                        <td>
                          <Link href={`/admin/grade/${r.id}`} className="btn btn-sm btn-primary">
                            <i className="bi bi-pencil-square me-1"></i>
                            {r.allReviewed ? 'View' : 'Grade'}
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
