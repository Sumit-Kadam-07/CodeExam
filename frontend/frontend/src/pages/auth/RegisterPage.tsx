import { useState } from 'react';
import { Link, useLocation } from 'wouter';
import Navbar from '@/components/layout/Navbar';
import { register } from '@/services/authService';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card, { CardBody } from '@/components/ui/Card';

const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
const PHONE_REGEX = /^[6-9]\d{9}$/;
const PASSWORD_MIN_LENGTH = 8;

function getPasswordErrors(password: string): string[] {
  const errors: string[] = [];
  if (password.length < PASSWORD_MIN_LENGTH) {
    errors.push(`At least ${PASSWORD_MIN_LENGTH} characters`);
  }
  if (!/[A-Z]/.test(password)) {
    errors.push('At least one uppercase letter');
  }
  if (!/[a-z]/.test(password)) {
    errors.push('At least one lowercase letter');
  }
  if (!/\d/.test(password)) {
    errors.push('At least one digit');
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
    errors.push('At least one special character (!@#$%^&*...)');
  }
  return errors;
}

export default function RegisterPage() {
  const [, navigate] = useLocation();
  const [form, setForm] = useState({ firstName: '', middleName: '', lastName: '', email: '', mobile: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    setFieldErrors(prev => ({ ...prev, [name]: '' }));
  }

  function validate(): boolean {
    const errors: Record<string, string> = {};

    if (!form.firstName.trim()) {
      errors.firstName = 'First name is required';
    }

    if (!form.lastName.trim()) {
      errors.lastName = 'Last name is required';
    }

    if (!form.email.trim()) {
      errors.email = 'Email is required';
    } else if (!EMAIL_REGEX.test(form.email)) {
      errors.email = 'Please enter a valid email address';
    }

    if (!form.mobile.trim()) {
      errors.mobile = 'Mobile number is required';
    } else if (!PHONE_REGEX.test(form.mobile)) {
      errors.mobile = 'Mobile number must be exactly 10 digits';
    }

    const passwordErrors = getPasswordErrors(form.password);
    if (!form.password) {
      errors.password = 'Password is required';
    } else if (passwordErrors.length > 0) {
      errors.password = passwordErrors.join(', ');
    }

    if (!form.confirmPassword) {
      errors.confirmPassword = 'Please confirm your password';
    } else if (form.password !== form.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    if (!validate()) return;

    setLoading(true);
    try {
      await register({
        firstName: form.firstName,
        middleName: form.middleName,
        lastName: form.lastName,
        email: form.email,
        mobileNumber: form.mobile,
        password: form.password,
        confirmPassword: form.confirmPassword,
        profilePhoto: null,
      });
      navigate('/login');
    } catch (err: any) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError(err instanceof Error ? err.message : 'Registration failed');
      }
    } finally {
      setLoading(false);
    }
  }

  const passwordHints = getPasswordErrors(form.password);

  return (
    <>
      <Navbar />
      <div className="container d-flex align-items-center justify-content-center py-5">
        <Card className="shadow" style={{ width: '100%', maxWidth: 480 }}>
          <CardBody className="p-4">
            <h4 className="card-title text-center mb-4 fw-bold">
              <i className="bi bi-person-plus text-primary me-2"></i>Create Account
            </h4>

            {error && (
              <div className="alert alert-danger">{error}</div>
            )}

            <form onSubmit={handleSubmit} noValidate>
              <Input
                label="First Name"
                name="firstName"
                placeholder="Enter your first name"
                value={form.firstName}
                onChange={handleChange}
                error={fieldErrors.firstName}
                required
              />
              <Input
                label="Middle Name"
                name="middleName"
                placeholder="Enter your middle name (optional)"
                value={form.middleName}
                onChange={handleChange}
              />
              <Input
                label="Last Name"
                name="lastName"
                placeholder="Enter your last name"
                value={form.lastName}
                onChange={handleChange}
                error={fieldErrors.lastName}
                required
              />
              <Input
                label="Email"
                type="email"
                name="email"
                placeholder="Enter your email"
                value={form.email}
                onChange={handleChange}
                error={fieldErrors.email}
                required
              />
              <Input
                label="Mobile Number"
                type="tel"
                name="mobile"
                placeholder="10-digit mobile number"
                maxLength={10}
                pattern="\d{10}"
                value={form.mobile}
                onChange={handleChange}
                error={fieldErrors.mobile}
                required
              />
              <Input
                label="Password"
                type="password"
                name="password"
                placeholder="Create a password"
                value={form.password}
                onChange={handleChange}
                error={fieldErrors.password}
                required
              />

              <Input
                label="Confirm Password"
                type="password"
                name="confirmPassword"
                placeholder="Repeat your password"
                value={form.confirmPassword}
                onChange={handleChange}
                error={fieldErrors.confirmPassword}
                required
              />
              <div className="d-grid mt-4">
                <Button 
                  type="submit" 
                  variant="primary" 
                  size="lg" 
                  loading={loading} 
                  loadingText="Registering..." 
                  icon="bi-person-check"
                >
                  Register
                </Button>
              </div>
            </form>

            <hr />
            <p className="text-center text-muted small mb-0">
              Already have an account? <Link href="/login">Login here</Link>
            </p>
          </CardBody>
        </Card>
      </div>
    </>
  );
}
