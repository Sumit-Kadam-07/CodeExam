import { useState, useEffect } from 'react';
import Navbar from '@/components/layout/Navbar';
import { Link, useParams, useLocation } from 'wouter';
import { getExam } from '@/services/adminService';
import { addQuestion } from '@/services/questionService';
import { LANGUAGE_BOILERPLATES } from '@/utils/boilerplates';
import { validateTestCases } from '@/utils/validators';
import { QuestionFormState, TestCaseItem, DEFAULT_QUESTION_FORM, DEFAULT_TEST_CASES } from '@/types/question';
import { AiGenerateResponse } from '@/types/ai';
import ManualQuestionTab from '@/components/add-question/ManualQuestionTab';
import AiGenerateTab from '@/components/add-question/AiGenerateTab';
import UploadQuestionTab from '@/components/add-question/UploadQuestionTab';
import ExistingQuestions from '@/components/add-question/ExistingQuestions';

type TabType = 'manual' | 'ai' | 'upload';

export default function AddQuestion() {
  const params = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const examId = Number(params.id);
  const [exam, setExam] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState<TabType>('manual');
  const [form, setForm] = useState<QuestionFormState>(DEFAULT_QUESTION_FORM);
  const [starterCode, setStarterCode] = useState(LANGUAGE_BOILERPLATES['Java']);
  const [testCases, setTestCases] = useState<TestCaseItem[]>(DEFAULT_TEST_CASES);
  const [showAdvanced, setShowAdvanced] = useState(false);

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
    getExam(examId)
      .then((data) => {
        if (!cancelled) {
          setExam(data);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [examId]);

  function handleFormChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) {
    const newForm = { ...form, [e.target.name]: e.target.value };
    if (e.target.name === 'language') {
      const boilerplate = LANGUAGE_BOILERPLATES[e.target.value] || '';
      setStarterCode(boilerplate);
      newForm.starterCode = boilerplate;
    }
    setForm(newForm);
  }

  function handleStarterCodeChange(value: string) {
    setStarterCode(value);
    setForm((prev) => ({ ...prev, starterCode: value }));
  }

  function addTestCase() {
    setTestCases([...testCases, { id: Date.now(), inputData: '', expectedOutput: '', weight: 1, sample: false }]);
  }

  function removeTestCase(id: number) {
    setTestCases(testCases.filter((tc) => tc.id !== id));
  }

  function updateTestCase(id: number, field: string, value: string | number | boolean) {
    setTestCases(testCases.map((tc) => (tc.id === id ? { ...tc, [field]: value } : tc)));
  }

function handleAiGenerated(data: AiGenerateResponse) {
    const newLanguage = data.language || form.language;
    const newBoilerplate = LANGUAGE_BOILERPLATES[newLanguage] || '';
    const newStarterCode = data.starterCode || newBoilerplate;

    const newForm: QuestionFormState = {
      title: data.title || '',
      difficulty: data.difficulty || form.difficulty,
      marks: data.marks > 0 ? data.marks : form.marks,
      problemStatement: data.problemStatement || '',
      inputFormat: data.inputFormat || '',
      outputFormat: data.outputFormat || '',
      constraints: data.constraints || '',
      sampleInput: data.sampleInput || '',
      sampleOutput: data.sampleOutput || '',
      explanation: data.explanation || '',
      expectedSolution: data.expectedSolution || '',
      language: newLanguage,
      starterCode: newStarterCode,
    };
    setForm(newForm);
    setStarterCode(newStarterCode);

    if (data.testCases && data.testCases.length > 0) {
      setTestCases(data.testCases.map((tc, i) => ({
        id: Date.now() + i,
        inputData: tc.input || '',
        expectedOutput: tc.expectedOutput || '',
        weight: tc.weight || 1,
        sample: tc.sample || false,
      })));
    }

    setShowAdvanced(true);
    setActiveTab('manual');
    setError('');
  }

  function handleGenerationError(msg: string) {
    setError(msg);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');

    const hasValidTestCase = testCases.some((tc) => tc.inputData.trim() !== '' && tc.expectedOutput.trim() !== '');
    if (!hasValidTestCase) {
      setError('At least one test case with non-empty input and expected output is required.');
      return;
    }

    setSaving(true);
    try {
      const res = await addQuestion(examId, {
        title: form.title,
        problemStatement: form.problemStatement,
        language: form.language,
        difficulty: form.difficulty,
        marks: Number(form.marks),
        starterCode: starterCode,
        executionTimeout: 5000,
        memoryLimit: 256,
        inputFormat: form.inputFormat,
        outputFormat: form.outputFormat,
        constraints: form.constraints,
        sampleInput: form.sampleInput,
        sampleOutput: form.sampleOutput,
        explanation: form.explanation,
        expectedSolution: form.expectedSolution,
        testCases: testCases.map((tc) => ({
          input: tc.inputData,
          expectedOutput: tc.expectedOutput,
          weight: tc.weight,
          sample: tc.sample,
        })),
      });
      if (res.success) {
        setTimeout(() => navigate(`/admin/exam/manage-questions/${examId}`), 800);
      } else {
        setError(res.message || 'Failed to add question.');
      }
    } catch (err: any) {
      setError(err?.response?.data?.error || err?.response?.data?.message || err?.message || 'Failed to add question.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="container mt-4">
          <div className="text-center p-4">Loading exam...</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="container-fluid mt-4">
        <div className="row g-4">
          <div className="col-lg-8">
            <div className="d-flex align-items-center gap-2 mb-3">
              <Link href={`/admin/exam/manage-questions/${examId}`} className="btn btn-outline-secondary btn-sm">
                <i className="bi bi-arrow-left"></i>
              </Link>
              <h4 className="fw-bold mb-0">
                <i className="bi bi-plus-circle text-success me-2"></i>Add Question
              </h4>
            </div>
            <p className="text-muted">
              For exam: <strong>{exam?.title || `#${examId}`}</strong>
            </p>

            {error && <div className="alert alert-danger">{error}</div>}

            {/* Tabs */}
            <ul className="nav nav-tabs mb-3">
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === 'manual' ? 'active' : ''}`}
                  onClick={() => setActiveTab('manual')}
                  type="button"
                >
                  <i className="bi bi-keyboard me-1"></i>Manual Question
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === 'ai' ? 'active' : ''}`}
                  onClick={() => setActiveTab('ai')}
                  type="button"
                >
                  <i className="bi bi-robot me-1"></i>AI Generate
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === 'upload' ? 'active' : ''}`}
                  onClick={() => setActiveTab('upload')}
                  type="button"
                >
                  <i className="bi bi-cloud-upload me-1"></i>Upload File
                </button>
              </li>
            </ul>

            {/* Tab Content */}
            {activeTab === 'manual' && (
              <div className="card shadow-sm">
                <div className="card-body p-4">
                  <ManualQuestionTab
                    form={form}
                    testCases={testCases}
                    showAdvanced={showAdvanced}
                    saving={saving}
                    onFormChange={handleFormChange}
                    onStarterCodeChange={handleStarterCodeChange}
                    onToggleAdvanced={() => setShowAdvanced(!showAdvanced)}
                    onAddTestCase={addTestCase}
                    onRemoveTestCase={removeTestCase}
                    onUpdateTestCase={updateTestCase}
                    onSubmit={handleSubmit}
                  />
                  <div className="mt-3">
                    <Link href={`/admin/exam/manage-questions/${examId}`} className="btn btn-secondary">
                      Cancel
                    </Link>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'ai' && (
              <AiGenerateTab onGenerated={handleAiGenerated} onError={handleGenerationError} />
            )}

{activeTab === 'upload' && (
              <UploadQuestionTab examId={examId} onError={handleGenerationError} />
            )}
          </div>

          <ExistingQuestions examId={examId} questions={exam?.questions || []} />
        </div>
      </div>
    </>
  );
}
