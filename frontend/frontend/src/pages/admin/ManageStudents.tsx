import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { getStudents, resetStudentPassword, deleteStudent } from '@/services/adminService';
import Card from '@/components/ui/Card';
import Loader from '@/components/ui/Loader';
import EmptyState from '@/components/ui/EmptyState';
import Table, { TableHead, TableBody, TableRow, TableHeader, TableCell } from '@/components/ui/Table';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Modal from '@/components/ui/Modal';
import { UserDTO } from '@/types';

export default function ManageStudents() {
  const [students, setStudents] = useState<UserDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedStudent, setSelectedStudent] = useState<any | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [resetSuccess, setResetSuccess] = useState(false);
  const [resetError, setResetError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    getStudents()
      .then((data) => {
        if (!cancelled) {
          setStudents(data || []);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, []);

  async function handleResetPassword(e: React.FormEvent) {
    e.preventDefault();
    setResetError('');
    if (!selectedStudent) return;
    try {
      await resetStudentPassword(selectedStudent.id, newPassword);
      setResetSuccess(true);
      setTimeout(() => {
        setResetSuccess(false);
        setSelectedStudent(null);
        setNewPassword('');
      }, 1500);
    } catch (err) {
      setResetError('Failed to reset password.');
    }
  }

  async function handleDelete(studentId: number, studentName: string) {
    if (!window.confirm(`Are you sure you want to delete student "${studentName}"? This will permanently remove the student and all associated results.`)) return;
    try {
      const res = await deleteStudent(studentId);
      if ((res as any).success) {
        setStudents(prev => prev.filter(s => s.id !== studentId));
      } else {
        alert('Delete failed');
      }
    } catch {
      alert('Failed to delete student.');
    }
  }

  return (
    <>
      <Navbar />
      <div className="container mt-4">
        <h2 className="fw-bold mb-4">
          <i className="bi bi-people-fill text-primary me-2"></i>Manage Students
        </h2>

        {loading ? (
          <Loader message="Loading students..." />
        ) : students.length === 0 ? (
          <Card>
            <EmptyState
              icon="bi-people"
              title="No students found"
              description="There are no students registered yet."
            />
          </Card>
        ) : (
          <Card>
            <Table>
              <TableHead className="table-dark">
                <TableRow>
                  <TableHeader>#</TableHeader>
                  <TableHeader>Name</TableHeader>
                  <TableHeader>Email / Username</TableHeader>
                  <TableHeader>Mobile</TableHeader>
                  <TableHeader>Actions</TableHeader>
                </TableRow>
              </TableHead>
              <TableBody>
                {students.map((s, idx) => (
                  <TableRow key={s.id}>
                    <TableCell>{idx + 1}</TableCell>
                    <TableCell>
                      <div className="d-flex align-items-center gap-2">
                        {s.profilePicUrl ? (
                          <img src={s.profilePicUrl} alt="pic" style={{ width: 32, height: 32, borderRadius: '50%', objectFit: 'cover' }} />
                        ) : (
                          <div
                            className="d-flex align-items-center justify-content-center text-white fw-bold"
                            style={{ width: 32, height: 32, borderRadius: '50%', background: '#0d6efd', fontSize: 13 }}
                          >
                            {(s.fullName || 'S').split(' ').map((w: string) => w[0]).join('').substring(0, 2)}
                          </div>
                        )}
                        <span className="fw-semibold">{s.fullName}</span>
                      </div>
                    </TableCell>
                    <TableCell>{s.username}</TableCell>
                    <TableCell>{s.mobileNumber || '—'}</TableCell>
                    <TableCell>
                      <div className="d-flex gap-1">
                        <Button
                          size="sm"
                          variant="warning"
                          icon="bi-key"
                          onClick={() => setSelectedStudent(s)}
                        >
                          Reset Password
                        </Button>
                        <Button
                          size="sm"
                          variant="danger"
                          icon="bi-trash"
                          onClick={() => handleDelete(s.id, s.fullName)}
                        >
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

      {/* Reset Password Modal */}
      <Modal
        isOpen={!!selectedStudent}
        onClose={() => setSelectedStudent(null)}
        title="Reset Password"
        icon="bi-key"
      >
        <form onSubmit={handleResetPassword}>
          {resetSuccess && (
            <div className="alert alert-success">Password reset successfully!</div>
          )}
          {resetError && (
            <div className="alert alert-danger">{resetError}</div>
          )}
          <p className="text-muted">
            Resetting password for: <strong>{selectedStudent?.fullName}</strong>
          </p>
          <Input
            label="New Password"
            type="password"
            placeholder="Enter new password"
            value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
            required
          />
          <div className="d-flex justify-content-end gap-2 mt-4">
            <Button variant="secondary" onClick={() => setSelectedStudent(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="warning" icon="bi-key">
              Reset Password
            </Button>
          </div>
        </form>
      </Modal>
    </>
  );
}