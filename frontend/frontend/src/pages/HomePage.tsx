import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';

export default function HomePage() {
  return (
    <>
      <Navbar />
      <div
        className="d-flex flex-column align-items-center justify-content-center text-white"
        style={{
          background: 'linear-gradient(135deg, #0d6efd 0%, #6f42c1 100%)',
          minHeight: '70vh',
          padding: '60px 20px',
          textAlign: 'center',
        }}
      >
        <i className="bi bi-code-slash" style={{ fontSize: '4rem', marginBottom: '1rem' }}></i>
        <h1 className="display-4 fw-bold mb-3">Smart Coding Exam System</h1>
        <p className="lead mb-4" style={{ maxWidth: 600, opacity: 0.9 }}>
          A modern platform for online coding examinations with real-time submission, auto-grading, and performance analytics.
        </p>
        <div className="d-flex gap-3 flex-wrap justify-content-center">
          <Link href="/login" className="btn btn-light btn-lg px-4">
            <i className="bi bi-box-arrow-in-right me-2"></i>Login
          </Link>
          <Link href="/register" className="btn btn-outline-light btn-lg px-4">
            <i className="bi bi-person-plus me-2"></i>Register
          </Link>
        </div>
      </div>

      <div className="container py-5">
        <h2 className="text-center fw-bold mb-4">Why CodeExam?</h2>
        <div className="row g-4">
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body text-center p-4">
                <i className="bi bi-stopwatch text-primary" style={{ fontSize: '2.5rem' }}></i>
                <h5 className="card-title mt-3 fw-semibold">Timed Exams</h5>
                <p className="card-text text-muted">
                  Real-time countdown timers keep students on pace and simulate real exam conditions.
                </p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body text-center p-4">
                <i className="bi bi-code-square text-success" style={{ fontSize: '2.5rem' }}></i>
                <h5 className="card-title mt-3 fw-semibold">Monaco Editor</h5>
                <p className="card-text text-muted">
                  Industry-standard code editor with syntax highlighting, autocompletion, and multi-language support.
                </p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body text-center p-4">
                <i className="bi bi-bar-chart-line text-warning" style={{ fontSize: '2.5rem' }}></i>
                <h5 className="card-title mt-3 fw-semibold">Analytics</h5>
                <p className="card-text text-muted">
                  Detailed performance analytics, leaderboards, and score breakdowns for admins and students.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
