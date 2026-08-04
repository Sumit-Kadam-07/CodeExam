import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend,
} from 'chart.js';
import { getAdminDashboard, DashboardData } from '@/services/adminService';
import { useFetch } from '@/hooks/useFetch';
import Card, { CardBody, CardHeader } from '@/components/ui/Card';
import Loader from '@/components/ui/Loader';
import Table, { TableHead, TableBody, TableRow, TableHeader, TableCell } from '@/components/ui/Table';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

export default function AdminDashboard() {
  const { data, loading, error } = useFetch<DashboardData>(getAdminDashboard);

  const chartData = {
    labels: data?.chartLabels || [],
    datasets: [{
      label: 'Submissions',
      data: data?.chartData || [],
      backgroundColor: ['rgba(13,110,253,0.7)', 'rgba(111,66,193,0.7)', 'rgba(25,135,84,0.7)', 'rgba(220,53,69,0.7)'],
      borderRadius: 6,
    }],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top' as const },
      title: { display: true, text: 'Submissions per Exam' },
    },
    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
  };

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <h2 className="fw-bold mb-4">
          <i className="bi bi-grid-fill text-primary me-2"></i>Admin Dashboard
        </h2>

        {/* KPI Cards */}
        <div className="row g-4 mb-4">
          <div className="col-sm-6 col-xl-3">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #0d6efd, #0dcaf0)' }}>
              <div className="card-body d-flex align-items-center gap-3">
                <i className="bi bi-people-fill" style={{ fontSize: '2.5rem' }}></i>
                <div>
                  <div className="fs-4 fw-bold">{loading ? '…' : data?.totalStudents ?? 0}</div>
                  <div className="small">Total Students</div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-sm-6 col-xl-3">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #6f42c1, #d63384)' }}>
              <div className="card-body d-flex align-items-center gap-3">
                <i className="bi bi-journal-text" style={{ fontSize: '2.5rem' }}></i>
                <div>
                  <div className="fs-4 fw-bold">{loading ? '…' : data?.totalExams ?? 0}</div>
                  <div className="small">Total Exams</div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-sm-6 col-xl-3">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #198754, #20c997)' }}>
              <div className="card-body d-flex align-items-center gap-3">
                <i className="bi bi-question-circle-fill" style={{ fontSize: '2.5rem' }}></i>
                <div>
                  <div className="fs-4 fw-bold">{loading ? '…' : data?.totalQuestions ?? 0}</div>
                  <div className="small">Total Questions</div>
                </div>
              </div>
            </div>
          </div>
          <div className="col-sm-6 col-xl-3">
            <div className="card text-white h-100" style={{ background: 'linear-gradient(135deg, #dc3545, #fd7e14)' }}>
              <div className="card-body d-flex align-items-center gap-3">
                <i className="bi bi-send-check-fill" style={{ fontSize: '2.5rem' }}></i>
                <div>
                  <div className="fs-4 fw-bold">{loading ? '…' : data?.totalSubmissions ?? 0}</div>
                  <div className="small">Total Submissions</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Chart + Quick Actions */}
        <div className="row g-4">
          <div className="col-lg-8">
            <Card>
              <CardBody>
                {loading ? <Loader /> : <Bar data={chartData} options={chartOptions} />}
              </CardBody>
            </Card>
          </div>
          <div className="col-lg-4">
            <Card className="h-100">
              <CardHeader className="fw-semibold">
                <i className="bi bi-lightning-fill text-warning me-2"></i>Quick Actions
              </CardHeader>
              <div className="list-group list-group-flush">
                <Link href="/admin/exam/add" className="list-group-item list-group-item-action">
                  <i className="bi bi-plus-circle text-success me-2"></i>Create New Exam
                </Link>
                <Link href="/admin/manage-exams" className="list-group-item list-group-item-action">
                  <i className="bi bi-journal-text text-primary me-2"></i>Manage Exams
                </Link>
                <Link href="/admin/students" className="list-group-item list-group-item-action">
                  <i className="bi bi-people text-info me-2"></i>Manage Students
                </Link>
                <Link href="/admin/leaderboard" className="list-group-item list-group-item-action">
                  <i className="bi bi-trophy text-warning me-2"></i>Global Leaderboard
                </Link>
              </div>
            </Card>
          </div>
        </div>

        {/* Recent Submissions */}
        <Card className="mt-4">
          <CardHeader className="fw-semibold">
            <i className="bi bi-clock-history me-2"></i>Recent Submissions
          </CardHeader>
          <Table>
            <TableHead>
              <TableRow>
                <TableHeader>Student</TableHeader>
                <TableHeader>Exam</TableHeader>
                <TableHeader>Score</TableHeader>
                <TableHeader>Submitted</TableHeader>
                <TableHeader>Actions</TableHeader>
              </TableRow>
            </TableHead>
            <TableBody>
              {(data?.recentResults || []).slice(0, 5).map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="fw-semibold">{r.studentName}</TableCell>
                  <TableCell>{r.examName}</TableCell>
                  <TableCell>{r.scoreAchieved} / {r.totalMarks}</TableCell>
                  <TableCell className="text-muted small">{r.submissionTime ? new Date(r.submissionTime).toLocaleDateString() : '—'}</TableCell>
                  <TableCell>
                    {r.examId != null ? (
                      <Link href={`/admin/results/${r.examId}`} className="btn btn-sm btn-outline-primary">
                        <i className="bi bi-eye"></i>
                      </Link>
                    ) : (
                      <span className="text-muted">—</span>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      </div>
    </>
  );
}