import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';
import { getStudentDashboard } from '@/services/studentService';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, Filler,
} from 'chart.js';
import type { DashboardData } from '@/services/studentService';
ChartJS.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, Filler);

function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) + ' ' +
    d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
}

export default function StudentDashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const res = await getStudentDashboard();
        setData(res);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load dashboard');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  const student = data?.student;

  const chartData = {
    labels: data?.chartLabels ?? [],
    datasets: [{
      label: 'Score (%)',
      data: data?.chartData ?? [],
      borderColor: 'rgba(13,110,253,1)',
      backgroundColor: 'rgba(13,110,253,0.15)',
      pointBackgroundColor: 'rgba(13,110,253,1)',
      tension: 0.4,
      fill: true,
    }],
  };

  const chartOptions = {
    responsive: true,
    plugins: { legend: { position: 'top' as const }, title: { display: true, text: 'Score History (%)' } },
    scales: { y: { beginAtZero: true, max: 100 } },
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container d-flex align-items-center justify-content-center" style={{ minHeight: '60vh' }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <Navbar />
        <div className="container mt-4">
          <div className="alert alert-danger">{error}</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        {/* Profile header */}
        <div className="card shadow-sm mb-4">
          <div className="card-body d-flex align-items-center gap-4 p-4">
            <div className="d-flex align-items-center justify-content-center text-white fw-bold"
              style={{ width: 72, height: 72, borderRadius: '50%', background: 'linear-gradient(135deg, #0d6efd, #6f42c1)', fontSize: 24 }}>
              {student?.fullName?.split(' ').map(w => w[0]).join('').substring(0, 2) ?? 'ST'}
            </div>
            <div>
              <h4 className="fw-bold mb-1">{student?.fullName ?? 'Gopal Lad'}</h4>
              <p className="text-muted mb-0">{student?.username ?? 'gopal@example.com'}</p>
            </div>
          </div>
        </div>

        {/* KPI Cards */}
        <div className="row g-3 mb-4">
          <div className="col-sm-4">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #0d6efd, #0dcaf0)' }}>
              <div className="card-body text-center">
                <i className="bi bi-journal-check" style={{ fontSize: '2rem' }}></i>
                <div className="fs-3 fw-bold mt-1">{data?.totalTaken}</div>
                <div className="small">Exams Taken</div>
              </div>
            </div>
          </div>
          <div className="col-sm-4">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #198754, #20c997)' }}>
              <div className="card-body text-center">
                <i className="bi bi-graph-up-arrow" style={{ fontSize: '2rem' }}></i>
                <div className="fs-3 fw-bold mt-1">{data?.averageScore}%</div>
                <div className="small">Average Score</div>
              </div>
            </div>
          </div>
          <div className="col-sm-4">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #ffc107, #fd7e14)' }}>
              <div className="card-body text-center">
                <i className="bi bi-trophy-fill" style={{ fontSize: '2rem' }}></i>
                <div className="fs-3 fw-bold mt-1">{data?.highestScore}%</div>
                <div className="small">Best Score</div>
              </div>
            </div>
          </div>
        </div>

        {/* Chart */}
        <div className="row g-4 mb-4">
          <div className="col-lg-7">
            <div className="card shadow-sm">
              <div className="card-body">
                <Line data={chartData} options={chartOptions} />
              </div>
            </div>
          </div>
          {/* Available Exams */}
          <div className="col-lg-5">
            <div className="card shadow-sm h-100">
              <div className="card-header fw-semibold">
                <i className="bi bi-journal-arrow-down text-success me-2"></i>Available Exams
              </div>
              {!data?.availableExams || data.availableExams.length === 0 ? (
                <div className="card-body text-muted small">No exams available right now.</div>
              ) : (
                <ul className="list-group list-group-flush">
                  {data.availableExams.map(exam => (
                    <li key={exam.id} className="list-group-item">
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <div className="fw-semibold">{exam.title}</div>
                          <div className="text-muted small">
                            <i className="bi bi-clock me-1"></i>{exam.durationInMinutes} min
                            &nbsp;|&nbsp; {exam.questions.length} questions
                          </div>
                        </div>
                        <Link href={`/exam/${exam.id}`} className="btn btn-sm btn-primary">
                          Start
                        </Link>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>

        {/* Past Results */}
        <div className="card shadow-sm">
          <div className="card-header fw-semibold">
            <i className="bi bi-clock-history me-2"></i>Past Results
          </div>
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead className="table-light">
                <tr>
                  <th>Exam</th>
                  <th>Submitted</th>
                  <th>Score</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {data?.pastResults.map(r => {
                  const pct = r.totalMarks > 0 ? ((r.scoreAchieved / r.totalMarks) * 100).toFixed(1) : '0';
                  return (
                    <tr key={r.id}>
                      <td className="fw-semibold">{r.exam?.title || 'Exam'}</td>
                      <td className="text-muted small">{formatDateTime(r.submissionTime)}</td>
                      <td>
                        {r.allReviewed
                          ? <span className={`badge ${Number(pct) >= 75 ? 'bg-success' : Number(pct) >= 50 ? 'bg-warning text-dark' : 'bg-danger'}`}>{pct}%</span>
                          : <span className="badge bg-secondary">Pending</span>}
                      </td>
                      <td>
                        <Link href={`/student/result-detail/${r.id}`} className="btn btn-sm btn-outline-primary me-1">
                          <i className="bi bi-eye"></i> View
                        </Link>
                        <Link href={`/student/review/${r.id}`} className="btn btn-sm btn-outline-secondary">
                          <i className="bi bi-code"></i> Code
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
}
