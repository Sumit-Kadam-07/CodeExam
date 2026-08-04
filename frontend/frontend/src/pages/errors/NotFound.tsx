import { Link } from 'wouter';
import Navbar from '@/components/layout/Navbar';

export default function NotFound() {
  return (
    <>
      <Navbar />
      <div className="container d-flex align-items-center justify-content-center" style={{ minHeight: '80vh' }}>
        <div className="text-center">
          <h1 className="display-1 fw-bold text-muted">404</h1>
          <h4 className="fw-semibold mb-3">Page Not Found</h4>
          <p className="text-muted mb-4">The page you're looking for doesn't exist or has been moved.</p>
          <Link href="/" className="btn btn-primary">
            <i className="bi bi-house me-2"></i>Back to Home
          </Link>
        </div>
      </div>
    </>
  );
}
