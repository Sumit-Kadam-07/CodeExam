import Editor from '@monaco-editor/react';
import { getMonacoLanguage } from '@/utils/boilerplates';

interface MonacoCodeEditorProps {
  language: string;
  value: string;
  onChange: (value: string) => void;
  height?: string;
}

export default function MonacoCodeEditor({ language, value, onChange, height = '240px' }: MonacoCodeEditorProps) {
  const monacoLanguage = getMonacoLanguage(language);

  return (
    <div className="border rounded" style={{ height, overflow: 'hidden' }}>
      <Editor
        key={language + monacoLanguage}
        height={height}
        language={monacoLanguage}
        value={value}
        onChange={(v) => onChange(v ?? '')}
        theme="vs-dark"
        options={{
          fontSize: 13,
          minimap: { enabled: false },
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          lineNumbers: 'on',
        }}
      />
    </div>
  );
}
