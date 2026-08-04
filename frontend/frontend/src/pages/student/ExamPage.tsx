import { useState, useEffect, useRef } from 'react';
import { useParams, useLocation } from 'wouter';
import { getExamQuestions } from '@/services/studentService';
import { runCode, compileCode, submitExam } from '@/services/examService';
import { useAuth } from '@/context/AuthContext';
import Editor from '@monaco-editor/react';
import { LANGUAGE_BOILERPLATES } from '@/utils/boilerplates';

interface Question {
  id: number;
  title: string;
  problemStatement: string;
  inputFormat?: string;
  outputFormat?: string;
  constraints?: string;
  sampleInput?: string;
  sampleOutput?: string;
  marks: number;
  difficulty: string;
  language?: string;
  starterCode?: string;
  testCases: Array<{ id: number; inputData: string; expectedOutput: string; weight: number }>;
}

function getMonacoLanguage(lang: string): string {
  const l = lang.toLowerCase();
  if (l === 'java') return 'java';
  if (l === 'python') return 'python';
  if (l === 'c') return 'c';
  if (l === 'c++' || l === 'cpp') return 'cpp';
  return 'cpp';
}

function normalizeStarterCode(code: string | undefined | null): string {
  if (!code) return '';
  let result = code;
  // Convert escaped newline sequences into actual newlines.
  if (result.includes('\\n') || result.includes('\\r')) {
    result = result.replace(/\\r\\n/g, '\r\n').replace(/\\n/g, '\n').replace(/\\r/g, '\r');
  }
  // Convert raw windows newlines to unix style for editor consistency.
  result = result.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
  return result;
}

function getDefaultStarterCode(question?: Question, languageOverride?: string): string {
  if (!question) return LANGUAGE_BOILERPLATES['Java'];
  if (languageOverride) {
    const normalizedLang = LANG_OPTIONS.find(l => l.toLowerCase() === languageOverride.toLowerCase()) || languageOverride;
    return LANGUAGE_BOILERPLATES[normalizedLang] || LANGUAGE_BOILERPLATES['Java'];
  }
  if (question.starterCode && question.starterCode.trim() !== '') {
    return normalizeStarterCode(question.starterCode);
  }
  const lang = question.language || 'Java';
  const normalizedLang = LANG_OPTIONS.find(l => l.toLowerCase() === lang.toLowerCase()) || lang;
  return LANGUAGE_BOILERPLATES[normalizedLang] || LANGUAGE_BOILERPLATES['Java'];
}

const LANG_OPTIONS = ['Java', 'Python', 'C', 'C++'];
const DEFAULT_DURATION_MINUTES = 60;

type ThemeMode = 'dark' | 'light';

function formatTime(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

const THEME_STORAGE_KEY = 'exam_theme';

export default function ExamPage() {
  const params = useParams<{ id: string }>();
  const [, navigate] = useLocation();
  const { user } = useAuth();
  const examId = Number(params.id);

  const [questions, setQuestions] = useState<Question[]>([]);
  const [durationMinutes, setDurationMinutes] = useState(DEFAULT_DURATION_MINUTES);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [activeQ, setActiveQ] = useState(0);
  const STORAGE_KEY = `exam_${examId}_${user?.username ?? 'guest'}_codes`;
  const LANG_KEY = `exam_${examId}_${user?.username ?? 'guest'}_langs`;

  function loadCodes(): Record<number, string> {
    try { const raw = localStorage.getItem(STORAGE_KEY); return raw ? JSON.parse(raw) : {}; } catch { return {}; }
  }

  function loadLanguages(): Record<number, string> {
    try { const raw = localStorage.getItem(LANG_KEY); return raw ? JSON.parse(raw) : {}; } catch { return {}; }
  }

const [codes, setCodes] = useState<Record<number, string>>(loadCodes);
  const [languages, setLanguages] = useState<Record<number, string>>(loadLanguages);
  const [timeLeft, setTimeLeft] = useState(DEFAULT_DURATION_MINUTES * 60);
  const [timerStarted, setTimerStarted] = useState(false);
  const [output, setOutput] = useState('');
  const [running, setRunning] = useState(false);
  const [compiling, setCompiling] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [fullscreen, setFullscreen] = useState(false);
  const [tabSwitches, setTabSwitches] = useState(0);
  const [theme, setTheme] = useState<ThemeMode>(() => {
    try { return (localStorage.getItem(THEME_STORAGE_KEY) as ThemeMode) || 'dark'; } catch { return 'dark'; }
  });
  const autoSaveTimer = useRef<ReturnType<typeof setInterval> | null>(null);
  const didSubmit = useRef(false);
  const handleSubmitRef = useRef<(() => Promise<void>) | null>(null);
  const fullscreenCheckRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const codesRef = useRef(codes);
  const languagesRef = useRef(languages);
  codesRef.current = codes;
  languagesRef.current = languages;

  if (isNaN(examId)) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ height: '100vh', background: '#1e1e2e', color: '#cdd6f4' }}>
        <div className="text-center">
          <h4>Invalid Exam</h4>
          <p className="text-muted">No valid exam ID provided.</p>
        </div>
      </div>
    );
  }

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    // Fetch both questions and exam info (which includes duration)
    Promise.all([
      getExamQuestions(examId),
      import('@/services/studentService').then(mod => mod.getAvailableExams ? mod.getAvailableExams() : Promise.resolve([])),
    ])
      .then(([qs, exams]) => {
        if (!cancelled) {
          setQuestions(qs);
          // Set timer from actual exam duration
          const exam = (exams || []).find((e: any) => e.id === examId);
          const dur = exam?.durationInMinutes || DEFAULT_DURATION_MINUTES;
          setDurationMinutes(dur);
          setTimeLeft(dur * 60);
          setTimerStarted(true);
          setLoading(false);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setError('Failed to load exam questions.');
          setLoading(false);
        }
      });
    return () => { cancelled = true; };
  }, [examId]);

useEffect(() => {
    if (!timerStarted) return;
    const interval = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(interval);
          if (handleSubmitRef.current) handleSubmitRef.current();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [timerStarted]);

  useEffect(() => {
    autoSaveTimer.current = setInterval(() => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(codesRef.current));
      localStorage.setItem(LANG_KEY, JSON.stringify(languagesRef.current));
    }, 30000);
    return () => { if (autoSaveTimer.current) clearInterval(autoSaveTimer.current); };
  }, []);

  // Full-screen on exam start & monitor exit
  useEffect(() => {
    if (!timerStarted || submitted) return;
    const el = document.documentElement;
    if (el.requestFullscreen && !document.fullscreenElement) {
      el.requestFullscreen().then(() => setFullscreen(true)).catch(() => {});
    }
    function onFsChange() {
      setFullscreen(!!document.fullscreenElement);
    }
    document.addEventListener('fullscreenchange', onFsChange);
    // Periodically check and warn if user exits fullscreen
    fullscreenCheckRef.current = setInterval(() => {
      if (!document.fullscreenElement && timerStarted && !submitted) {
        // Try to re-enter fullscreen
        document.documentElement.requestFullscreen().catch(() => {});
      }
    }, 5000);
    return () => {
      document.removeEventListener('fullscreenchange', onFsChange);
      if (fullscreenCheckRef.current) clearInterval(fullscreenCheckRef.current);
      if (submitted && document.fullscreenElement) {
        document.exitFullscreen().catch(() => {});
      }
    };
  }, [timerStarted, submitted]);

  // Tab-switch cheating prevention
  useEffect(() => {
    if (!timerStarted || submitted) return;
    function onVisibilityChange() {
      if (document.hidden && timerStarted && !submitted && !didSubmit.current) {
        setTabSwitches(prev => prev + 1);
        // Record the violation for audit
        try {
          const VIOLATIONS_KEY = `exam_${examId}_${user?.username ?? 'guest'}_violations`;
          const violations = JSON.parse(localStorage.getItem(VIOLATIONS_KEY) || '[]');
          violations.push({ type: 'tab_switch', timestamp: new Date().toISOString() });
          localStorage.setItem(VIOLATIONS_KEY, JSON.stringify(violations));
        } catch {}
      }
    }
    document.addEventListener('visibilitychange', onVisibilityChange);
    return () => document.removeEventListener('visibilitychange', onVisibilityChange);
  }, [timerStarted, submitted, examId]);

  // Persist theme to localStorage when it changes
  useEffect(() => {
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  useEffect(() => {
    if (questions.length === 0) return;
    setCodes(prev => {
      const updated = { ...prev };
      let changed = false;
      questions.forEach(q => {
        const existing = prev[q.id];
        if (existing == null || existing === '') {
          const lang = languages[q.id] || q.language;
          const starter = getDefaultStarterCode(q, lang);
          updated[q.id] = starter;
          changed = true;
        }
      });
      return changed ? updated : prev;
    });
  }, [questions]);

  function getCode(qIdx: number): string {
    const q = questions[qIdx];
    if (!q) return '';
    const stored = codes[q.id];
    return stored != null && stored !== '' ? normalizeStarterCode(stored) : getDefaultStarterCode(q);
  }

  function getStarterCode(qIdx: number): string {
    const q = questions[qIdx];
    if (!q) return '';
    const lang = getLanguage(qIdx);
    return getDefaultStarterCode(q, lang);
  }

  function setCode(qIdx: number, value: string) {
    const q = questions[qIdx];
    if (!q) return;
    setCodes(prev => ({ ...prev, [q.id]: value }));
  }

  function getLanguage(qIdx: number): string {
    const q = questions[qIdx];
    if (!q) return 'Java';
    return languages[q.id] || q.language || 'Java';
  }

  function setLanguage(qIdx: number, lang: string) {
    const q = questions[qIdx];
    if (!q) return;
    setLanguages(prev => ({ ...prev, [q.id]: lang }));
    // Update boilerplate code to match the newly selected language
    const currentCode = codes[q.id];
    const oldLang = languages[q.id] || q.language || 'Java';
    const oldNormalized = LANG_OPTIONS.find(l => l.toLowerCase() === oldLang.toLowerCase()) || oldLang;
    const oldBoilerplate = LANGUAGE_BOILERPLATES[oldNormalized] || '';
    if (!currentCode || currentCode.trim() === '' || currentCode === oldBoilerplate) {
      const newNormalized = LANG_OPTIONS.find(l => l.toLowerCase() === lang.toLowerCase()) || lang;
      const newBoilerplate = LANGUAGE_BOILERPLATES[newNormalized] || LANGUAGE_BOILERPLATES['Java'];
      setCodes(prev => ({ ...prev, [q.id]: newBoilerplate }));
    }
  }

  async function handleRun() {
    setRunning(true);
    setOutput('');
    const q = questions[activeQ];
    const code = getCode(activeQ);
    const lang = getLanguage(activeQ);
    try {
      const res = await runCode({ language: lang, sourceCode: code, input: q?.sampleInput || '' });
      if (res.success) {
        setOutput(res.stdout || '(No output)');
      } else {
        setOutput(res.compilationError || res.runtimeError || 'Execution failed.');
      }
    } catch {
      setOutput('Error running code. Please try again.');
    } finally {
      setRunning(false);
    }
  }

  async function handleCompile() {
    setCompiling(true);
    setOutput('');
    const code = getCode(activeQ);
    const lang = getLanguage(activeQ);
    try {
      const res = await compileCode({ language: lang, sourceCode: code });
      if (res.success) {
        setOutput(res.compilationOutput || `✓ Compilation successful for Q${activeQ + 1}`);
      } else {
        setOutput(`✗ Compilation failed:\n${res.compilationError || 'Unknown error'}`);
      }
    } catch {
      setOutput('Error compiling code. Please try again.');
    } finally {
      setCompiling(false);
    }
  }

  async function handleSubmit() {
    if (submitting || submitted || didSubmit.current) return;
    didSubmit.current = true;
    setShowConfirm(false);
    setSubmitting(true);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(codes));
    localStorage.setItem(LANG_KEY, JSON.stringify(languages));
    try {
      const answers = questions.map((q, i) => ({
        questionId: q.id,
        sourceCode: getCode(i),
        language: getLanguage(i),
      }));
      const res = await submitExam({ examId, answers });
      if (res.success) {
        setSubmitted(true);
        localStorage.removeItem(STORAGE_KEY);
        localStorage.removeItem(LANG_KEY);
        if (document.fullscreenElement) {
          document.exitFullscreen().catch(() => {});
        }
        setTimeout(() => navigate('/student/result'), 800);
      } else {
        setOutput('Submit failed: ' + res.message);
        didSubmit.current = false;
      }
    } catch {
      setOutput('Error submitting exam. Please try again.');
      didSubmit.current = false;
    } finally {
      setSubmitting(false);
    }
  }

  useEffect(() => {
    handleSubmitRef.current = handleSubmit;
  }, [handleSubmit]);

  const timerDanger = timeLeft < 300;
  const isDark = theme === 'dark';

  // Theme-aware color palette
  const colors = {
    bg: isDark ? '#1e1e2e' : '#f8f9fa',
    bg2: isDark ? '#181825' : '#ffffff',
    bg3: isDark ? '#11111b' : '#e9ecef',
    fg: isDark ? '#cdd6f4' : '#212529',
    fg2: isDark ? '#a6adc8' : '#495057',
    fgMuted: isDark ? '#6c7086' : '#6c757d',
    border: isDark ? '#313244' : '#dee2e6',
    accent: '#89b4fa',
    accentLight: isDark ? '#89b4fa' : '#0d6efd',
    success: '#a6e3a1',
    successLight: isDark ? '#a6e3a1' : '#198754',
    danger: '#f38ba8',
    warning: '#f9e2af',
    navBg: isDark ? '#1e1e2e' : '#f8f9fa',
    navHover: isDark ? '#313244' : '#e9ecef',
    navActive: isDark ? '#45475a' : '#d0d7de',
    outputBg: isDark ? '#11111b' : '#f8f9fa',
    outputFg: isDark ? '#a6e3a1' : '#198754',
    codeBg: isDark ? '#1e1e2e' : '#ffffff',
    descBg: isDark ? '#1e1e2e' : '#ffffff',
    selectBg: isDark ? '#313244' : '#ffffff',
    selectFg: isDark ? '#cdd6f4' : '#212529',
    selectBorder: isDark ? '#45475a' : '#ced4da',
  };

  function toggleTheme() {
    setTheme(prev => prev === 'dark' ? 'light' : 'dark');
  }

  if (loading) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ height: '100vh', background: colors.bg, color: colors.fg }}>
        <div className="spinner-border text-primary me-3" role="status"></div>
        <span>Loading exam...</span>
      </div>
    );
  }

  if (error || questions.length === 0) {
    return (
      <div className="d-flex align-items-center justify-content-center" style={{ height: '100vh', background: colors.bg, color: colors.fg }}>
        <div className="text-center">
          <h4>Exam Unavailable</h4>
          <p className="text-muted">{error || 'No questions found for this exam.'}</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', background: colors.bg, color: colors.fg, overflow: 'hidden' }}>
      <style>{`
        .exam-nav-btn { background: none; border: none; color: ${colors.fg2}; padding: 8px 12px; text-align: left; width: 100%; cursor: pointer; border-radius: 6px; margin-bottom: 4px; }
        .exam-nav-btn:hover { background: ${colors.navHover}; color: ${colors.fg}; }
        .exam-nav-btn.active { background: ${colors.navActive}; color: ${colors.accentLight}; font-weight: 600; }
        .exam-topbar { background: ${colors.bg2}; border-bottom: 1px solid ${colors.border}; display: flex; align-items: center; padding: 8px 16px; gap: 12px; min-height: 52px; }
        .exam-sidebar { background: ${colors.bg2}; border-right: 1px solid ${colors.border}; width: 210px; flex-shrink: 0; overflow-y: auto; padding: 12px; }
        .exam-desc { background: ${colors.descBg}; border-right: 1px solid ${colors.border}; width: 340px; flex-shrink: 0; overflow-y: auto; padding: 16px; }
        .exam-editor { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: ${colors.codeBg}; }
        .exam-editor-toolbar { background: ${colors.bg2}; border-bottom: 1px solid ${colors.border}; padding: 6px 12px; display: flex; align-items: center; gap: 8px; }
        .exam-output { background: ${colors.outputBg}; border-top: 1px solid ${colors.border}; height: 140px; overflow-y: auto; font-family: monospace; font-size: 12px; padding: 8px 12px; color: ${colors.outputFg}; flex-shrink: 0; }
        .exam-output-text { color: ${colors.fg2}; }
      `}</style>

      {/* Top bar */}
      <div className="exam-topbar">
        <span style={{ color: colors.accentLight, fontWeight: 700, fontSize: 16 }}>
          <i className="bi bi-code-slash me-2"></i>Exam
        </span>
        {tabSwitches > 0 && (
          <span style={{ color: colors.danger, fontSize: 11 }} title="Tab switch detected">
            <i className="bi bi-eye-slash me-1"></i>{tabSwitches}
          </span>
        )}
        {!fullscreen && timerStarted && !submitted && (
          <span style={{ color: colors.warning, fontSize: 11 }}>
            <i className="bi bi-arrows-fullscreen me-1"></i>Not fullscreen
          </span>
        )}
        <span style={{ marginLeft: 'auto', color: timerDanger ? colors.danger : colors.successLight, fontWeight: 700, fontSize: 18 }}>
          <i className="bi bi-stopwatch me-2"></i>{formatTime(timeLeft)}
        </span>
        <button
          className="btn btn-sm"
          onClick={toggleTheme}
          style={{ background: 'transparent', border: `1px solid ${colors.border}`, color: colors.fg2 }}
          title={`Switch to ${isDark ? 'light' : 'dark'} mode`}
        >
          <i className={`bi bi-${isDark ? 'sun' : 'moon'}`}></i>
        </button>
        <button
          className="btn btn-sm btn-danger ms-2"
          onClick={() => setShowConfirm(true)}
          disabled={submitting || submitted}
        >
          <i className="bi bi-send me-1"></i>{submitted ? 'Submitted' : 'Submit'}
        </button>
      </div>

      {/* Main body */}
      <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
        {/* Sidebar: question navigator */}
        <div className="exam-sidebar">
<div style={{ color: colors.fgMuted, fontSize: 11, textTransform: 'uppercase', fontWeight: 600, marginBottom: 8 }}>Questions</div>
          {questions.map((q, i) => (
            <button
              key={q.id}
              className={`exam-nav-btn${activeQ === i ? ' active' : ''}`}
              onClick={() => setActiveQ(i)}
            >
<span style={{ fontSize: 11, color: colors.fgMuted }}>Q{i + 1}</span>
              <div style={{ fontSize: 13, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{q.title}</div>
              <div style={{ fontSize: 11 }}>
                <span className={`badge ${q.difficulty === 'Easy' ? 'bg-success' : q.difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'}`} style={{ fontSize: 10 }}>
                  {q.difficulty}
                </span>
<span style={{ color: colors.accentLight, marginLeft: 6 }}>{q.marks} pts</span>
              </div>
            </button>
          ))}
        </div>

{/* Question description */}
        {questions[activeQ] && (
          <div className="exam-desc">
            <h5 style={{ color: isDark ? '#cba6f7' : '#6f42c1', fontWeight: 700 }}>{questions[activeQ].title}</h5>
            <div style={{ fontSize: 11, marginBottom: 12 }}>
              <span className={`badge ${questions[activeQ].difficulty === 'Easy' ? 'bg-success' : questions[activeQ].difficulty === 'Medium' ? 'bg-warning text-dark' : 'bg-danger'}`}>
                {questions[activeQ].difficulty}
              </span>
              <span style={{ color: colors.accentLight, marginLeft: 8 }}>{questions[activeQ].marks} pts</span>
            </div>
            <p style={{ color: colors.fg, fontSize: 14 }}>{questions[activeQ].problemStatement}</p>
            {questions[activeQ].inputFormat && (
              <><div style={{ color: colors.accentLight, fontWeight: 600, fontSize: 13 }}>Input Format</div>
              <p style={{ fontSize: 13, color: colors.fg }}>{questions[activeQ].inputFormat}</p></>
            )}
            {questions[activeQ].outputFormat && (
              <><div style={{ color: colors.accentLight, fontWeight: 600, fontSize: 13 }}>Output Format</div>
              <p style={{ fontSize: 13, color: colors.fg }}>{questions[activeQ].outputFormat}</p></>
            )}
            {questions[activeQ].constraints && (
              <><div style={{ color: colors.accentLight, fontWeight: 600, fontSize: 13 }}>Constraints</div>
              <p style={{ fontSize: 13, color: colors.fg }}>{questions[activeQ].constraints}</p></>
            )}
            {questions[activeQ].sampleInput !== undefined && (
              <><div style={{ color: colors.accentLight, fontWeight: 600, fontSize: 13 }}>Sample Input</div>
              <pre style={{ background: colors.bg3, padding: '8px', borderRadius: 4, fontSize: 12, color: colors.successLight }}>{questions[activeQ].sampleInput || '(none)'}</pre></>
            )}
            {questions[activeQ].sampleOutput !== undefined && (
              <><div style={{ color: colors.accentLight, fontWeight: 600, fontSize: 13 }}>Sample Output</div>
              <pre style={{ background: colors.bg3, padding: '8px', borderRadius: 4, fontSize: 12, color: colors.successLight }}>{questions[activeQ].sampleOutput}</pre></>
            )}
          </div>
        )}

        {/* Editor + Output */}
        <div className="exam-editor">
          <div className="exam-editor-toolbar">
<span style={{ color: colors.fg2, fontSize: 12 }}>
              <i className="bi bi-file-code me-1"></i>
              Q{activeQ + 1}: {questions[activeQ]?.title ?? 'No question'}
            </span>
{/* Per-question language selector */}
            <select
              className="form-select form-select-sm ms-auto"
              style={{ width: 110, background: colors.selectBg, color: colors.selectFg, border: `1px solid ${colors.selectBorder}` }}
              value={getLanguage(activeQ)}
              onChange={e => setLanguage(activeQ, e.target.value)}
            >
              {LANG_OPTIONS.map(l => <option key={l}>{l}</option>)}
            </select>
<button
              className="btn btn-sm"
              style={{ borderColor: colors.success, color: colors.success }}
              onClick={handleRun}
              disabled={running}
            >
              {running ? <><span className="spinner-border spinner-border-sm me-1"></span>Running…</> : <><i className="bi bi-play-fill me-1"></i>Run</>}
            </button>
            <button
              className="btn btn-sm"
              style={{ borderColor: '#ffc107', color: '#ffc107', border: '1px solid' }}
              onClick={handleCompile}
              disabled={compiling}
            >
              {compiling ? <><span className="spinner-border spinner-border-sm me-1"></span>Compiling…</> : <><i className="bi bi-hammer me-1"></i>Compile</>}
            </button>
            <button
              className="btn btn-sm"
              style={{ borderColor: colors.accentLight, color: colors.accentLight, border: '1px solid' }}
              onClick={() => setCode(activeQ, getStarterCode(activeQ))}
            >
              <i className="bi bi-arrow-counterclockwise me-1"></i>Reset
            </button>
          </div>
          <div style={{ flex: 1, overflow: 'hidden' }}>
<Editor
              height="100%"
              language={getMonacoLanguage(getLanguage(activeQ))}
              value={getCode(activeQ)}
              onChange={v => setCode(activeQ, v ?? '')}
              theme={isDark ? 'vs-dark' : 'light'}
              options={{ fontSize: 14, minimap: { enabled: false }, scrollBeyondLastLine: false, wordWrap: 'on' }}
            />
          </div>
<div className="exam-output">
            <div style={{ color: colors.fgMuted, marginBottom: 4, fontSize: 11, textTransform: 'uppercase', fontWeight: 600 }}>Output</div>
            {output || <span className="exam-output-text">Run your code to see output here.</span>}
          </div>
        </div>
      </div>

{/* Submit Confirmation Modal */}
      {showConfirm && (
        <>
          <div className="modal show d-block" tabIndex={-1}>
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content" style={{ background: colors.bg, border: `1px solid ${colors.border}`, color: colors.fg }}>
                <div className="modal-header" style={{ borderColor: colors.border }}>
                  <h5 className="modal-title"><i className="bi bi-send me-2"></i>Submit Exam</h5>
                  <button type="button" className="btn-close" style={{ filter: isDark ? 'invert(1)' : 'none' }} onClick={() => setShowConfirm(false)}></button>
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to submit? You won't be able to change your answers after submission.</p>
                  <p style={{ color: colors.danger }}>
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    Time remaining: <strong>{formatTime(timeLeft)}</strong>
                  </p>
                </div>
                <div className="modal-footer" style={{ borderColor: colors.border }}>
                  <button className="btn btn-secondary" onClick={() => setShowConfirm(false)} disabled={submitting}>Cancel</button>
                  <button className="btn btn-danger" onClick={handleSubmit} disabled={submitting}>
                    <i className="bi bi-send me-2"></i>{submitting ? 'Submitting…' : 'Submit Now'}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop show"></div>
        </>
      )}
    </div>
  );
}
