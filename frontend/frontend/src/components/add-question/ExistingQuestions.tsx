import { Link } from 'wouter';

interface ExistingQuestionsProps {
  examId: number;
  questions: any[];
}

export default function ExistingQuestions({ examId, questions }: ExistingQuestionsProps) {
  return (
    <div className="col-lg-4">
      <div className="card shadow-sm">
        <div className="card-header fw-semibold">
          <i className="bi bi-list-ol me-2"></i>Existing Questions ({questions.length})
        </div>
        {questions.length === 0 ? (
          <div className="card-body text-muted small">No questions yet.</div>
        ) : (
          <ul className="list-group list-group-flush">
            {questions.map((q: any, i: number) => (
              <li key={q.id} className="list-group-item">
                <div className="d-flex justify-content-between align-items-start">
                  <div>
                    <span className="badge bg-secondary me-2">{i + 1}</span>
                    <span className="fw-semibold small">{q.title}</span>
                  </div>
                  <span className="badge bg-primary">{q.marks} pts</span>
                </div>
                <div className="mt-1">
                  <span
                    className={`badge ${q.difficulty === 'Easy' ? 'bg-success' : q.difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'} small`}
                  >
                    {q.difficulty}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
