import { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useLocation } from 'wouter';
import { createExam } from '@/services/adminService';

export default function AddExam() {
  const [, navigate] = useLocation();
  const [form, setForm] = useState({ title: '', description: '', durationInMinutes: 60 });
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const durationPresets = [30, 45, 60, 90, 120];

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await createExam({ title: form.title, description: form.description, durationInMinutes: Number(form.durationInMinutes) });
      setSuccess(true);
      setTimeout(() => navigate('/admin/manage-exams'), 1200);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to create exam. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4" style={{ maxWidth: 680 }}>
        <div className="d-flex align-items-center gap-2 mb-4">
          <Link href="/admin/manage-exams" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h2 className="fw-bold mb-0">
            <i className="bi bi-plus-circle text-success me-2"></i>Add New Exam
          </h2>
        </div>

        {success && (
          <div className="alert alert-success">
            <i className="bi bi-check-circle me-2"></i>Exam created successfully! Redirecting…
          </div>
        )}
        {error && (
          <div className="alert alert-danger">{error}</div>
        )}

        <div className="card shadow-sm">
          <div className="card-body p-4">
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="title" className="form-label fw-semibold">Exam Title <span className="text-danger">*</span></label>
                <input
                  type="text"
                  id="title"
                  name="title"
                  className="form-control"
                  placeholder="e.g. Java Practical Exam – June 2026"
                  value={form.title}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="description" className="form-label fw-semibold">Description</label>
                <textarea
                  id="description"
                  name="description"
                  className="form-control"
                  rows={3}
                  placeholder="Brief description of the exam (optional)"
                  value={form.description}
                  onChange={handleChange}
                />
              </div>
              <div className="mb-4">
                <label htmlFor="durationInMinutes" className="form-label fw-semibold">
                  Duration (minutes) <span className="text-danger">*</span>
                </label>
                <input
                  type="number"
                  id="durationInMinutes"
                  name="durationInMinutes"
                  className="form-control"
                  min={1}
                  value={form.durationInMinutes}
                  onChange={handleChange}
                  required
                />
                <div className="mt-2 d-flex flex-wrap gap-2">
                  {durationPresets.map(d => (
                    <button
                      key={d}
                      type="button"
                      className={`btn btn-sm ${form.durationInMinutes === d ? 'btn-primary' : 'btn-outline-secondary'}`}
                      onClick={() => setForm({ ...form, durationInMinutes: d })}
                    >
                      {d} min
                    </button>
                  ))}
                </div>
              </div>
              <div className="d-flex gap-2">
                <button type="submit" className="btn btn-success" disabled={loading}>
                  <i className="bi bi-save me-2"></i>{loading ? 'Creating…' : 'Create Exam'}
                </button>
                <Link href="/admin/manage-exams" className="btn btn-secondary">
                  Cancel
                </Link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}
