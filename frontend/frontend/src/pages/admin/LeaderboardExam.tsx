import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getExamLeaderboard } from '@/services/adminService';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend,
} from 'chart.js';
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const medalColors: Record<number, string> = { 1: '#FFD700', 2: '#C0C0C0', 3: '#CD7F32' };
const medalIcons: Record<number, string> = { 1: 'bi-trophy-fill', 2: 'bi-award-fill', 3: 'bi-patch-check-fill' };

export default function LeaderboardExam() {
  const params = useParams<{ id: string }>();
  const examId = Number(params.id);
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

  const chartData = {
    labels: ranked.map((e: any) => e.studentName),
    datasets: [{
      label: 'Score (%)',
      data: ranked.map((e: any) => e.percentage),
      backgroundColor: ranked.map((_: any, i: number) => i === 0 ? 'rgba(255,193,7,0.8)' : i === 1 ? 'rgba(108,117,125,0.8)' : 'rgba(205,127,50,0.8)'),
      borderRadius: 6,
    }],
  };

  const chartOptions = {
    responsive: true,
    plugins: { legend: { display: false }, title: { display: true, text: 'Score Distribution' } },
    scales: { y: { beginAtZero: true, max: 100 } },
  };

  const avgScore = ranked.length ? (ranked.reduce((s: number, e: any) => s + (e.percentage ?? 0), 0) / ranked.length).toFixed(1) : '0';

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Link href="/admin/manage-exams" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-trophy-fill text-warning me-2"></i>Exam Leaderboard
          </h4>
        </div>
        <p className="text-muted mb-4">Exam: <strong>{data?.exam?.title || `#${examId}`}</strong></p>

        {loading ? (
          <div className="text-center p-4">Loading leaderboard…</div>
        ) : (
          <>
            {/* KPI cards */}
            <div className="row g-3 mb-4">
              <div className="col-sm-4">
                <div className="card text-white text-center" style={{ background: 'linear-gradient(135deg, #ffc107, #fd7e14)' }}>
                  <div className="card-body py-3">
                    <div className="fs-4 fw-bold">{ranked.length}</div>
                    <div className="small">Ranked Students</div>
                  </div>
                </div>
              </div>
              <div className="col-sm-4">
                <div className="card text-white text-center" style={{ background: 'linear-gradient(135deg, #0d6efd, #6f42c1)' }}>
                  <div className="card-body py-3">
                    <div className="fs-4 fw-bold">{avgScore}%</div>
                    <div className="small">Average Score</div>
                  </div>
                </div>
              </div>
              <div className="col-sm-4">
                <div className="card text-white text-center" style={{ background: 'linear-gradient(135deg, #198754, #20c997)' }}>
                  <div className="card-body py-3">
                    <div className="fs-4 fw-bold">{top3[0]?.percentage ?? '0'}%</div>
                    <div className="small">Top Score</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Podium */}
            {top3.length >= 1 && (
              <div className="card shadow-sm mb-4">
                <div className="card-body">
                  <h5 className="fw-bold text-center mb-4">
                    <i className="bi bi-trophy-fill text-warning me-2"></i>Top Performers
                  </h5>
                  <div className="d-flex justify-content-center align-items-end gap-4 flex-wrap">
                    {[top3[1], top3[0], top3[2]].filter(Boolean).map((e: any, i: number) => {
                      const displayRank = i === 0 ? 2 : i === 1 ? 1 : 3;
                      const heights: Record<number, number> = { 1: 140, 2: 100, 3: 80 };
                      return (
                        <div key={e.resultId} className="text-center" style={{ minWidth: 110 }}>
                          <div className="fw-bold small mb-1">{e.studentName}</div>
                          <div className="text-muted small mb-2">{e.percentage}%</div>
                          <div
                            className="d-flex flex-column align-items-center justify-content-end mx-auto rounded-top"
                            style={{ width: 90, height: heights[displayRank], background: medalColors[displayRank], color: '#333' }}
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

            {/* Chart */}
            {ranked.length > 0 && (
              <div className="card shadow-sm mb-4">
                <div className="card-body">
                  <Bar data={chartData} options={chartOptions} />
                </div>
              </div>
            )}

            {/* Full table */}
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
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((e: any) => (
                      <tr key={e.resultId}>
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
                              style={{ width: 32, height: 32, borderRadius: '50%', background: '#0d6efd', fontSize: 12 }}>
                              {e.initials}
                            </div>
                            <div>
                              <div className="fw-semibold">{e.studentName}</div>
                              <div className="text-muted small">{e.studentUsername}</div>
                            </div>
                          </div>
                        </td>
                        <td>{e.fullyGraded ? `${e.scoreAchieved} / ${e.totalMarks}` : <span className="text-muted">Pending</span>}</td>
                        <td>
                          {e.fullyGraded
                            ? <span className={`badge ${e.percentage >= 75 ? 'bg-success' : e.percentage >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>{e.percentage}%</span>
                            : <span className="text-muted">—</span>}
                        </td>
                        <td>
                          {e.fullyGraded
                            ? <span className="badge bg-success">Graded</span>
                            : <span className="badge bg-warning text-dark">Pending</span>}
                        </td>
                        <td>
                          <Link href={`/admin/grade/${e.resultId}`} className="btn btn-sm btn-outline-primary">
                            <i className="bi bi-eye"></i>
                          </Link>
                        </td>
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
