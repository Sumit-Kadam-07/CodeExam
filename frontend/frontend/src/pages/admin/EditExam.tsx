import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useLocation, useParams } from 'wouter';
import { getExam, updateExam } from '@/services/adminService';

export default function EditExam() {
  const params = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const examId = Number(params.id);
  const [form, setForm] = useState({ title: '', description: '', durationInMinutes: 60 });
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  if (isNaN(examId)) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="alert alert-danger">Invalid exam ID.</div></div>
      </>
    );
  }

  const durationPresets = [30, 45, 60, 90, 120];

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getExam(examId)
      .then((exam) => {
        if (!cancelled) {
          setForm({
            title: exam.title,
            description: exam.description ?? '',
            durationInMinutes: exam.durationInMinutes,
          });
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('Failed to load exam.');
          setLoading(false);
        }
      });
    return () => { cancelled = true; };
  }, [examId]);

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

async function handleSubmit(e: React.FormEvent) {
     e.preventDefault();
     setError('');
     setSuccess(false);
     setLoading(true);
     try {
       await updateExam(examId, { title: form.title, description: form.description, durationInMinutes: Number(form.durationInMinutes) });
       setSuccess(true);
       setTimeout(() => navigate('/admin/manage-exams'), 1200);
     } catch (err: any) {
       setError(err?.message || 'Failed to update exam.');
     } finally {
       setLoading(false);
     }
   }

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading exam…</div></div>
      </>
    );
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
            <i className="bi bi-pencil-square text-primary me-2"></i>Edit Exam
          </h2>
        </div>

        {success && (
          <div className="alert alert-success">
            <i className="bi bi-check-circle me-2"></i>Exam updated successfully!
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
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  <i className="bi bi-save me-2"></i>{loading ? 'Saving...' : 'Save Changes'}
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
