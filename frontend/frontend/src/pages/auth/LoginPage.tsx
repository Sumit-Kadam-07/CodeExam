import { useState } from 'react';
import { Link, useLocation } from 'wouter';
import { login } from '@/services/authService';
import { useAuth } from '@/context/AuthContext';
import Navbar from '@/components/layout/Navbar';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card, { CardBody } from '@/components/ui/Card';

export default function LoginPage() {
  const [, navigate] = useLocation();
  const { login: authLogin } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await login(username, password);
      const normalizedRole = res.user.role?.toString().replace(/^ROLE_/, '') ?? res.user.role;
      localStorage.setItem('token', res.token);
      localStorage.setItem('user', JSON.stringify({
        username: res.user.username,
        fullName: res.user.fullName,
        role: normalizedRole,
        profilePicUrl: res.user.profilePicUrl ?? null,
      }));
      authLogin({
        username: res.user.username,
        fullName: res.user.fullName,
        role: normalizedRole as 'ADMIN' | 'STUDENT',
        profilePicUrl: res.user.profilePicUrl ?? null,
      });
      if (normalizedRole === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/student/dashboard');
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Login failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <Navbar />
      <div className="container d-flex align-items-center justify-content-center" style={{ minHeight: '85vh' }}>
        <Card className="shadow" style={{ width: '100%', maxWidth: 420 }}>
          <CardBody className="p-4">
            <h4 className="card-title text-center mb-4 fw-bold">
              <i className="bi bi-code-slash text-primary me-2"></i>CodeExam Login
            </h4>

            {error && (
              <div className="alert alert-danger" role="alert">{error}</div>
            )}

            <form onSubmit={handleSubmit}>
              <Input
                label="Username"
                type="text"
                placeholder="Enter your username or email"
                value={username}
                onChange={e => setUsername(e.target.value)}
                required
              />
              <Input
                label="Password"
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
              <div className="d-grid mt-4">
                <Button 
                  type="submit" 
                  variant="primary" 
                  size="lg" 
                  loading={loading} 
                  loadingText="Logging in…" 
                  icon="bi-box-arrow-in-right"
                >
                  Login
                </Button>
              </div>
            </form>

            <hr />
            <p className="text-center text-muted small mb-0">
              Don't have an account? <Link href="/register">Register here</Link>
            </p>
          </CardBody>
        </Card>
      </div>
    </>
  );
}