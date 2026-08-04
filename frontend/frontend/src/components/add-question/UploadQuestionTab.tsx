import { useState, useRef, DragEvent } from 'react';
import { useLocation } from 'wouter';
import { uploadAndGenerate } from '@/services/uploadService';

interface UploadQuestionTabProps {
  examId: number;
  onError: (msg: string) => void;
}

const ACCEPTED_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'text/plain',
  'text/markdown',
];

const ACCEPTED_EXTENSIONS = '.pdf,.doc,.docx,.txt,.md';

export default function UploadQuestionTab({ examId, onError }: UploadQuestionTabProps) {
  const [, navigate] = useLocation();
  const [loading, setLoading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [language, setLanguage] = useState('Java');
  const [difficulty, setDifficulty] = useState('Medium');
  const [marks, setMarks] = useState(10);
  const [progress, setProgress] = useState(0);
  const [result, setResult] = useState<{ savedCount: number; message: string; errors?: string[] } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function handleFileSelect(file: File) {
    const ext = file.name.split('.').pop()?.toLowerCase();
    const validExts = ['pdf', 'doc', 'docx', 'txt', 'md'];
    if (!validExts.includes(ext || '')) {
      onError('Unsupported file type. Supported: PDF, DOC, DOCX, TXT, MD');
      return;
    }
    setSelectedFile(file);
    onError('');
    setResult(null);
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFileSelect(file);
  }

  function handleDragOver(e: DragEvent) {
    e.preventDefault();
    setDragOver(true);
  }

  function handleDragLeave() {
    setDragOver(false);
  }

  function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (file) handleFileSelect(file);
  }

  async function handleUpload(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedFile) {
      onError('Please select a file to upload.');
      return;
    }
    setLoading(true);
    setProgress(0);
    onError('');
    setResult(null);

    const progressInterval = setInterval(() => {
      setProgress((p) => Math.min(p + 5, 90));
    }, 500);

    try {
      const res = await uploadAndGenerate(selectedFile, examId, language, difficulty, Number(marks));
      if (res.success) {
        setResult({
          savedCount: res.savedCount || 0,
          message: res.message || 'File processed successfully.',
          errors: res.errors,
        });
        // Navigate to manage-questions page after a short delay
        setTimeout(() => navigate(`/admin/exam/manage-questions/${examId}`), 2000);
      } else {
        onError(res.message || 'Upload processing failed.');
      }
    } catch (err: any) {
      onError(err?.response?.data?.message || err?.message || 'Upload processing failed.');
    } finally {
      clearInterval(progressInterval);
      setProgress(100);
      setTimeout(() => setLoading(false), 500);
    }
  }

  return (
    <div className="card shadow-sm">
      <div className="card-body p-4">
        <div className="text-center mb-4">
          <i className="bi bi-cloud-upload text-success" style={{ fontSize: 48 }}></i>
          <h5 className="fw-bold mt-2">Upload Question File</h5>
          <p className="text-muted small">Upload a document containing one or more questions. AI will extract each question and save them directly.</p>
        </div>

        {result ? (
          <div className="text-center py-4">
            <div className="mb-3">
              <i className="bi bi-check-circle text-success" style={{ fontSize: 48 }}></i>
            </div>
            <h5 className="fw-bold text-success">Upload Complete!</h5>
            <p className="mb-2">{result.message}</p>
            {result.savedCount > 0 && (
              <p className="text-muted">
                <strong>{result.savedCount}</strong> question(s) created and saved.
              </p>
            )}
            {result.errors && result.errors.length > 0 && (
              <div className="text-start mt-3">
                <p className="text-warning fw-semibold mb-1">Warnings:</p>
                <ul className="small text-muted mb-0">
                  {result.errors.map((err, i) => (
                    <li key={i}>{err}</li>
                  ))}
                </ul>
              </div>
            )}
            <p className="text-muted small mt-2">Redirecting to question list...</p>
          </div>
        ) : (
          <form onSubmit={handleUpload}>
            <div
              className={`border rounded p-5 text-center mb-3 ${dragOver ? 'border-primary bg-primary bg-opacity-10' : 'border-dashed'}`}
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onClick={() => fileInputRef.current?.click()}
              style={{ cursor: 'pointer' }}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept={ACCEPTED_EXTENSIONS}
                onChange={handleInputChange}
                className="d-none"
              />
              {selectedFile ? (
                <div>
                  <i className="bi bi-file-earmark-check text-success" style={{ fontSize: 36 }}></i>
                  <p className="fw-semibold mt-2 mb-1">{selectedFile.name}</p>
                  <p className="text-muted small mb-0">{(selectedFile.size / 1024).toFixed(1)} KB</p>
                </div>
              ) : (
                <div>
                  <i className="bi bi-cloud-arrow-up text-muted" style={{ fontSize: 36 }}></i>
                  <p className="fw-semibold mt-2 mb-1">Drag & Drop or Click to Browse</p>
                  <p className="text-muted small mb-0">
                    Supported: PDF, DOC, DOCX, TXT, MD
                  </p>
                </div>
              )}
            </div>

            <div className="row g-3 mb-3">
              <div className="col-md-4">
                <label className="form-label fw-semibold">Language</label>
                <select className="form-select" value={language} onChange={(e) => setLanguage(e.target.value)}>
                  <option>Java</option>
                  <option>Python</option>
                  <option>C</option>
                  <option>C++</option>
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label fw-semibold">Difficulty</label>
                <select className="form-select" value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
                  <option>Easy</option>
                  <option>Medium</option>
                  <option>Hard</option>
                </select>
              </div>
              <div className="col-md-4">
                <label className="form-label fw-semibold">Marks</label>
                <input
                  type="number"
                  className="form-control"
                  min={1}
                  value={marks}
                  onChange={(e) => setMarks(Number(e.target.value))}
                />
              </div>
            </div>

            {loading && (
              <div className="mb-3">
                <div className="progress" style={{ height: 8 }}>
                  <div className="progress-bar bg-success" role="progressbar" style={{ width: `${progress}%` }}></div>
                </div>
                <small className="text-muted mt-1 d-block">Extracting text, generating questions, and saving...</small>
              </div>
            )}

            <button type="submit" className="btn btn-success w-100" disabled={loading || !selectedFile}>
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                  Processing...
                </>
              ) : (
                <>
                  <i className="bi bi-upload me-2"></i>Upload & Generate Questions
                </>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
