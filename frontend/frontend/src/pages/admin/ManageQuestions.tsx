import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams } from 'wouter';
import { getExamQuestions, deleteQuestion, getExam } from '@/services/adminService';

export default function ManageQuestions() {
  const params = useParams<{ id: string }>();
  const examId = Number(params.id);
  const [questions, setQuestions] = useState<any[]>([]);
  const [exam, setExam] = useState<any>(null);
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
    Promise.all([
      getExam(examId),
      getExamQuestions(examId)
    ])
      .then(([examData, qs]) => {
        if (!cancelled) {
          setExam(examData);
          setQuestions(qs || []);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [examId]);

  async function handleDelete(id: number) {
    if (!window.confirm('Delete this question? This cannot be undone.')) return;
    try {
      const res = await deleteQuestion(id, examId);
      if ((res as any).success) {
        setQuestions(prev => prev.filter(q => q.id !== id));
      } else {
        alert((res as any).message || 'Delete failed');
      }
    } catch (error: any) {
      alert(error?.response?.data?.message || error?.message || 'Failed to delete question. Please try again.');
    }
  }

  const totalMarks = questions.reduce((s: number, q: any) => s + (q.marks ?? 0), 0);

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4"><div className="text-center p-4">Loading questions…</div></div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex align-items-center gap-2 mb-2">
          <Link href="/admin/manage-exams" className="btn btn-outline-secondary btn-sm">
            <i className="bi bi-arrow-left"></i>
          </Link>
          <h4 className="fw-bold mb-0">
            <i className="bi bi-list-ol text-primary me-2"></i>Manage Questions
          </h4>
        </div>
        <p className="text-muted mb-3">
          Exam: <strong>{exam?.title || `#${examId}`}</strong> &nbsp;|&nbsp; Duration: <strong>{exam?.durationInMinutes ?? '—'} min</strong>
          &nbsp;|&nbsp; Total Marks: <strong>{totalMarks}</strong>
        </p>

        <div className="d-flex justify-content-between align-items-center mb-3">
          <span className="badge bg-secondary fs-6">{questions.length} Question(s)</span>
          <Link href={`/admin/exam/${examId}/question/add`} className="btn btn-success">
            <i className="bi bi-plus-circle me-2"></i>Add Question
          </Link>
        </div>

        {questions.length === 0 ? (
          <div className="alert alert-info">
            No questions yet. <Link href={`/admin/exam/${examId}/question/add`}>Add the first question.</Link>
          </div>
        ) : (
          <div className="card shadow-sm">
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead className="table-dark">
                  <tr>
                    <th>#</th>
                    <th>Title</th>
                    <th>Difficulty</th>
                    <th>Marks</th>
                    <th>Test Cases</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {questions.map((q, idx) => (
                    <tr key={q.id}>
                      <td>{idx + 1}</td>
                      <td>
                        <div className="fw-semibold">{q.title}</div>
                        <div className="text-muted small" style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {q.problemStatement}
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${q.difficulty === 'Easy' ? 'bg-success' : q.difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'}`}>
                          {q.difficulty}
                        </span>
                      </td>
                      <td><span className="badge bg-primary">{q.marks} pts</span></td>
                      <td>{q.testCases?.length ?? 0}</td>
                      <td>
                        <div className="d-flex gap-1">
                          <Link href={`/admin/question/edit/${q.id}`} className="btn btn-sm btn-primary">
                            <i className="bi bi-pencil-square"></i>
                          </Link>
                          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(q.id)}>
                            <i className="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="table-light">
                  <tr>
                    <td colSpan={3} className="fw-bold text-end">Total</td>
                    <td><span className="badge bg-dark">{totalMarks} pts</span></td>
                    <td colSpan={2}></td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
