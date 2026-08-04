import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getExamLeaderboard } from '@/services/studentService';
import { useAuth } from '@/context/AuthContext';

const medalColors: Record<number, string> = { 1: '#FFD700', 2: '#C0C0C0', 3: '#CD7F32' };
const medalIcons: Record<number, string> = { 1: 'bi-trophy-fill', 2: 'bi-award-fill', 3: 'bi-patch-check-fill' };

export default function Leaderboard() {
  const params = useParams<{ id: string }>();
  const examId = Number(params.id);
  const { user } = useAuth();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

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
    getExamLeaderboard(examId)
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
  }, [examId]);

  const entries = data?.entries || [];
  const ranked = entries.filter((e: any) => e.rank > 0);
  const top3 = ranked.slice(0, 3);
  const myEntry = entries.find((e: any) => e.studentUsername === (user?.username ?? ''));

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Link href="/student/my-results" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-trophy-fill text-warning me-2"></i>Leaderboard
          </h4>
        </div>
        <p className="text-muted mb-4">Exam: <strong>{data?.exam?.title || `#${examId}`}</strong></p>

        {loading ? (
          <div className="text-center p-4">Loading leaderboard…</div>
        ) : (
          <>
            {/* Your rank banner */}
            {myEntry && myEntry.rank > 0 && (
              <div
                className="alert d-flex align-items-center justify-content-between mb-4"
                style={{ background: 'linear-gradient(135deg, #0d6efd22, #6f42c122)', border: '1px solid #0d6efd55' }}
              >
                <div>
                  <i className="bi bi-person-check-fill text-primary me-2"></i>
                  <strong>Your Rank: #{myEntry.rank}</strong> &nbsp;—&nbsp; {myEntry.scoreAchieved} / {myEntry.totalMarks} marks
                </div>
                <span className={`badge ${myEntry.percentage >= 75 ? 'bg-success' : myEntry.percentage >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>
                  {myEntry.percentageFormatted ?? myEntry.percentage ?? '0'}%
                </span>
              </div>
            )}

            {/* Podium */}
            {top3.length >= 1 && (
              <div className="card shadow-sm mb-4">
                <div className="card-body">
                  <h5 className="fw-bold text-center mb-4">
                    <i className="bi bi-trophy-fill text-warning me-2"></i>Top Performers
                  </h5>
                  <div className="d-flex justify-content-center align-items-end gap-4 flex-wrap">
                    {[top3[1], top3[0], top3[2]].filter(Boolean).map((e, i) => {
                      const displayRank = i === 0 ? 2 : i === 1 ? 1 : 3;
                      const heights: Record<number, number> = { 1: 140, 2: 100, 3: 80 };
                      const isMe = e.studentUsername === (user?.username ?? '');
                      return (
                        <div key={e.resultId} className="text-center" style={{ minWidth: 110 }}>
                          <div className="fw-bold small mb-1" style={{ color: isMe ? '#0d6efd' : undefined }}>
                            {e.studentName}{isMe && <span className="ms-1 badge bg-primary" style={{ fontSize: 9 }}>You</span>}
                          </div>
                          <div className="text-muted small mb-2">{e.percentageFormatted ?? e.percentage ?? '0'}%</div>
                          <div
                            className="d-flex flex-column align-items-center justify-content-end mx-auto rounded-top"
                            style={{ width: 90, height: heights[displayRank], background: medalColors[displayRank], color: '#333', outline: isMe ? '3px solid #0d6efd' : undefined }}
                          >
                            <i className={`bi ${medalIcons[displayRank]} mb-2`} style={{ fontSize: '1.5rem' }}></i>
                            <span className="fw-bold fs-5">#{displayRank}</span>
                            <span className="small mb-1">{e.scoreAchieved}/{e.totalMarks}</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* Full Rankings Table */}
            <div className="card shadow-sm">
              <div className="card-header fw-semibold">Full Rankings</div>
              <div className="table-responsive">
                <table className="table table-hover mb-0">
                  <thead className="table-dark">
                    <tr>
                      <th>Rank</th>
                      <th>Student</th>
                      <th>Score</th>
                      <th>Percentage</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((e: any) => {
                      const isMe = e.studentUsername === (user?.username ?? '');
                      return (
                        <tr key={e.resultId} className={isMe ? 'table-primary' : ''}>
                          <td>
                            {e.rank > 0
                              ? <span style={{ color: medalColors[e.rank] ?? undefined, fontWeight: 700 }}>
                                  {e.rank <= 3 && <i className={`bi ${medalIcons[e.rank]} me-1`}></i>}#{e.rank}
                                </span>
                              : <span className="text-muted">—</span>}
                          </td>
                          <td>
                            <div className="d-flex align-items-center gap-2">
                              <div className="d-flex align-items-center justify-content-center text-white fw-bold"
                                style={{ width: 32, height: 32, borderRadius: '50%', background: isMe ? '#0d6efd' : '#6f42c1', fontSize: 12 }}>
                                {e.initials}
                              </div>
                              <div>
                                <div className="fw-semibold">
                                  {e.studentName}
                                  {isMe && <span className="ms-2 badge bg-primary" style={{ fontSize: 10 }}>You</span>}
                                </div>
                              </div>
                            </div>
                          </td>
                          <td>{e.fullyGraded ? `${e.scoreAchieved} / ${e.totalMarks}` : <span className="text-muted">Pending</span>}</td>
                          <td>
                            {e.fullyGraded
                              ? <span className={`badge ${e.percentage >= 75 ? 'bg-success' : e.percentage >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>{e.percentageFormatted ?? e.percentage ?? '0'}%</span>
                              : <span className="text-muted">—</span>}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </div>
    </>
  );
}
