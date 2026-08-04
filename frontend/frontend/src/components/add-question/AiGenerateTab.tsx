import { useState } from 'react';
import { generateAiQuestion } from '@/services/aiService';
import { AiGenerateRequest, AiGenerateResponse } from '@/types/ai';

interface AiGenerateTabProps {
  onGenerated: (data: AiGenerateResponse) => void;
  onError: (msg: string) => void;
}

export default function AiGenerateTab({ onGenerated, onError }: AiGenerateTabProps) {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState<AiGenerateRequest>({
    topic: '',
    language: 'Java',
    difficulty: 'Medium',
    marks: 10,
  });

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function handleGenerate(e: React.FormEvent) {
    e.preventDefault();
    if (!form.topic.trim()) {
      onError('Please enter a topic or prompt.');
      return;
    }
    setLoading(true);
    onError('');
    try {
      const res = await generateAiQuestion({
        ...form,
        marks: Number(form.marks),
      });
      if (res.success && res.data) {
        onGenerated(res.data);
      } else {
        onError(res.message || 'AI generation failed.');
      }
    } catch (err: any) {
      onError(err?.response?.data?.message || err?.message || 'AI generation failed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="card shadow-sm">
      <div className="card-body p-4">
        <div className="text-center mb-4">
          <i className="bi bi-robot text-primary" style={{ fontSize: 48 }}></i>
          <h5 className="fw-bold mt-2">AI Question Generator</h5>
          <p className="text-muted small">Describe the topic and the AI will generate a complete coding question.</p>
        </div>

        <form onSubmit={handleGenerate}>
          <div className="mb-3">
            <label className="form-label fw-semibold">
              Topic / Prompt <span className="text-danger">*</span>
            </label>
            <textarea
              name="topic"
              className="form-control"
              rows={3}
              placeholder="e.g. Binary search in a sorted array, Two sum problem, String manipulation..."
              value={form.topic}
              onChange={handleChange}
              required
            />
          </div>

          <div className="row g-3 mb-3">
            <div className="col-md-4">
              <label className="form-label fw-semibold">Language</label>
              <select name="language" className="form-select" value={form.language} onChange={handleChange}>
                <option>Java</option>
                <option>Python</option>
                <option>C</option>
                <option>C++</option>
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label fw-semibold">Difficulty</label>
              <select name="difficulty" className="form-select" value={form.difficulty} onChange={handleChange}>
                <option>Easy</option>
                <option>Medium</option>
                <option>Hard</option>
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label fw-semibold">Marks</label>
              <input
                type="number"
                name="marks"
                className="form-control"
                min={1}
                value={form.marks}
                onChange={handleChange}
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary w-100" disabled={loading}>
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                Generating...
              </>
            ) : (
              <>
                <i className="bi bi-magic me-2"></i>Generate Question
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
