import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';
import { getGlobalLeaderboard } from '@/services/adminService';

const medalColors: Record<number, string> = { 1: '#FFD700', 2: '#C0C0C0', 3: '#CD7F32' };
const medalIcons: Record<number, string> = { 1: 'bi-trophy-fill', 2: 'bi-award-fill', 3: 'bi-patch-check-fill' };

export default function LeaderboardGlobal() {
  const [entries, setEntries] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getGlobalLeaderboard()
      .then((res) => {
        if (!cancelled) {
          setEntries(res?.entries || []);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  const top3 = entries.slice(0, 3);

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-4">
          <Link href="/admin/dashboard" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-trophy-fill text-warning me-2"></i>Global Leaderboard
          </h4>
        </div>

        {loading ? (
          <div className="text-center p-4">Loading leaderboard…</div>
        ) : (
          <>
            {/* Podium */}
            {top3.length >= 1 && (
              <div className="card shadow-sm mb-4">
                <div className="card-body">
                  <h5 className="fw-bold text-center mb-4">
                    <i className="bi bi-trophy-fill text-warning me-2"></i>Top Students
                  </h5>
                  <div className="d-flex justify-content-center align-items-end gap-4 flex-wrap">
                    {[top3[1], top3[0], top3[2]].filter(Boolean).map((e, i) => {
                      const displayRank = i === 0 ? 2 : i === 1 ? 1 : 3;
                      const heights: Record<number, number> = { 1: 140, 2: 100, 3: 80 };
                      return (
                        <div key={e.studentUsername} className="text-center" style={{ minWidth: 110 }}>
                          <div className="fw-bold small mb-1">{e.studentName}</div>
                          <div className="text-muted small mb-2">{e.avgPercentageFormatted ?? e.avgPercentage ?? '0'}% avg</div>
                          <div
                            className="d-flex flex-column align-items-center justify-content-end mx-auto rounded-top"
                            style={{ width: 90, height: heights[displayRank], background: medalColors[displayRank], color: '#333' }}
                          >
                            <i className={`bi ${medalIcons[displayRank]} mb-2`} style={{ fontSize: '1.5rem' }}></i>
                            <span className="fw-bold fs-5">#{displayRank}</span>
                            <span className="small mb-1">{e.totalScore} pts</span>
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
              <div className="card-header fw-semibold">
                <i className="bi bi-list-ol me-2"></i>All Student Rankings
              </div>
              <div className="table-responsive">
                <table className="table table-hover mb-0">
                  <thead className="table-dark">
                    <tr>
                      <th>Rank</th>
                      <th>Student</th>
                      <th>Total Score</th>
                      <th>Avg. %</th>
                      <th>Graded Exams</th>
                      <th>Exams Taken</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((e) => (
                      <tr key={e.studentUsername}>
                        <td>
                          <span style={{ color: medalColors[e.rank] ?? undefined, fontWeight: 700 }}>
                            {e.rank <= 3 && <i className={`bi ${medalIcons[e.rank]} me-1`}></i>}#{e.rank}
                          </span>
                        </td>
                        <td>
                          <div className="d-flex align-items-center gap-2">
                            <div className="d-flex align-items-center justify-content-center text-white fw-bold"
                              style={{ width: 32, height: 32, borderRadius: '50%', background: '#6f42c1', fontSize: 12 }}>
                              {e.initials}
                            </div>
                            <div>
                              <div className="fw-semibold">{e.studentName}</div>
                              <div className="text-muted small">{e.studentUsername}</div>
                            </div>
                          </div>
                        </td>
                        <td><strong>{e.totalScore}</strong></td>
                        <td>
                          <span className={`badge ${e.avgPercentage >= 75 ? 'bg-success' : e.avgPercentage >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>
                            {e.avgPercentageFormatted ?? e.avgPercentage ?? '0'}%
                          </span>
                        </td>
                        <td>{e.gradedExams}</td>
                        <td>{e.examsTaken}</td>
                      </tr>
                    ))}
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
