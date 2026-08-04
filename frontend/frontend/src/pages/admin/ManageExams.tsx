import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link } from 'wouter';
import { getAllExams, deleteExam, publishExam, unpublishExam } from '@/services/adminService';
import Card from '@/components/ui/Card';
import Loader from '@/components/ui/Loader';
import EmptyState from '@/components/ui/EmptyState';
import Table, { TableHead, TableBody, TableRow, TableHeader, TableCell } from '@/components/ui/Table';
import Button from '@/components/ui/Button';
import { ExamDTO } from '@/types';

export default function ManageExams() {
  const [exams, setExams] = useState<ExamDTO[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchExams = async () => {
    try {
      setLoading(true);
      const data = await getAllExams();
      setExams(data || []);
    } catch {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExams();
  }, []);

  async function handleTogglePublish(exam: any) {
    try {
      const fn = exam.published ? unpublishExam : publishExam;
      const res = await fn(exam.id);
      if ((res as any).success) {
        setExams(prev => prev.map(e => e.id === exam.id ? { ...e, published: !e.published } : e));
      } else {
        alert((res as any).message || 'Operation failed');
      }
    } catch (error: any) {
      alert(error?.response?.data?.message || error?.message || 'Operation failed. Please try again.');
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('Are you sure you want to delete this exam? This action is irreversible.')) return;
    try {
      const res = await deleteExam(id);
      if ((res as any).success) {
        setExams(prev => prev.filter(e => e.id !== id));
      } else {
        alert((res as any).message || 'Delete failed');
      }
    } catch (error: any) {
      alert(error?.response?.data?.message || error?.message || 'Failed to delete exam. Please try again.');
    }
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h2 className="fw-bold mb-0">
            <i className="bi bi-journal-text text-primary me-2"></i>Manage Exams
          </h2>
          <Link href="/admin/exam/add" className="btn btn-success">
            <i className="bi bi-plus-circle me-2"></i>Add New Exam
          </Link>
        </div>

        {loading ? (
          <Loader message="Loading exams..." />
        ) : exams.length === 0 ? (
          <Card>
            <EmptyState
              icon="bi-journal-x"
              title="No exams found"
              description="Create your first exam to get started."
              action={
                <Link href="/admin/exam/add" className="btn btn-primary">
                  <i className="bi bi-plus-circle me-2"></i>Create New Exam
                </Link>
              }
            />
          </Card>
        ) : (
          <Card>
            <Table>
              <TableHead className="table-dark">
                <TableRow>
                  <TableHeader>#</TableHeader>
                  <TableHeader>Title</TableHeader>
                  <TableHeader>Duration</TableHeader>
                  <TableHeader>Questions</TableHeader>
                  <TableHeader>Status</TableHeader>
                  <TableHeader>Actions</TableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {exams.map((exam, idx) => (
                  <TableRow key={exam.id}>
                    <TableCell>{idx + 1}</TableCell>
                    <TableCell>
                      <div className="fw-semibold">{exam.title}</div>
                      {exam.description && (
                        <div className="text-muted small">{exam.description}</div>
                      )}
                    </TableCell>
                    <TableCell><i className="bi bi-clock me-1"></i>{exam.durationInMinutes} min</TableCell>
                    <TableCell>
                      <Link href={`/admin/exam/manage-questions/${exam.id}`} className="badge bg-info text-decoration-none">
                        {exam.questions?.length ?? 0} Questions
                      </Link>
                    </TableCell>
                    <TableCell>
                      {exam.published
                        ? <span className="badge bg-success"><i className="bi bi-check-circle me-1"></i>Published</span>
                        : <span className="badge bg-secondary"><i className="bi bi-pencil me-1"></i>Draft</span>}
                    </TableCell>
                    <TableCell>
                      <div className="d-flex flex-wrap gap-1">
                        {exam.published ? (
                          <Button size="sm" variant="warning" icon="bi-eye-slash" onClick={() => handleTogglePublish(exam)}>
                            Unpublish
                          </Button>
                        ) : (
                          <Button size="sm" variant="success" icon="bi-eye" onClick={() => handleTogglePublish(exam)}>
                            Publish
                          </Button>
                        )}
                        <Link href={`/admin/exam/edit/${exam.id}`} className="btn btn-sm btn-primary">
                          <i className="bi bi-pencil-square"></i> Edit
                        </Link>
                        <Link href={`/admin/exam/manage-questions/${exam.id}`} className="btn btn-sm btn-info">
                          <i className="bi bi-list-ol"></i> Questions
                        </Link>
                        <Link href={`/admin/results/${exam.id}`} className="btn btn-sm btn-secondary">
                          <i className="bi bi-bar-chart"></i> Results
                        </Link>
                        <Link href={`/admin/leaderboard/${exam.id}`} className="btn btn-sm btn-warning">
                          <i className="bi bi-trophy"></i> Leaderboard
                        </Link>
                        <Button size="sm" variant="danger" icon="bi-trash" onClick={() => handleDelete(exam.id)}>
                          Delete
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>
        )}
      </div>
    </>
  );
}