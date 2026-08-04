import { Link } from 'wouter';
import Navbar from '@/components/layout/Navbar';

export default function ResultConfirmation() {
  return (
    <>
      <Navbar />
      <div className="container d-flex align-items-center justify-content-center" style={{ minHeight: '80vh' }}>
        <div className="card shadow text-center" style={{ maxWidth: 520, width: '100%' }}>
          {/* Gradient confetti bar */}
          <div style={{ height: 8, background: 'linear-gradient(90deg, #0d6efd, #6f42c1, #198754, #ffc107, #dc3545)' }} className="rounded-top"></div>
          <div className="card-body p-5">
            <div
              className="d-flex align-items-center justify-content-center mx-auto mb-4 text-white"
              style={{ width: 80, height: 80, borderRadius: '50%', background: 'linear-gradient(135deg, #198754, #20c997)', fontSize: 36 }}
            >
              <i className="bi bi-check-lg"></i>
            </div>
            <h3 className="fw-bold mb-2">Exam Submitted!</h3>
            <p className="text-muted mb-3">
              Your responses have been recorded and auto-evaluated successfully.
            </p>
            <div className="alert alert-info py-2 px-3 mb-3" style={{ fontSize: 14 }}>
              <i className="bi bi-info-circle me-2"></i>
              <strong>Please check with the admin for evaluation.</strong> Your results will be finalized after the instructor reviews your submissions.
            </div>
            <div className="d-flex flex-column gap-2">
              <Link href="/student/my-results" className="btn btn-primary btn-lg">
                <i className="bi bi-bar-chart me-2"></i>View My Results
              </Link>
              <Link href="/student/dashboard" className="btn btn-outline-secondary">
                <i className="bi bi-house me-2"></i>Back to Dashboard
              </Link>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
