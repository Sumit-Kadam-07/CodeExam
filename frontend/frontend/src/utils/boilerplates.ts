export const LANGUAGE_BOILERPLATES: Record<string, string> = {
  Java: `import java.util.*;\n\npublic class Main {\n\n    public static void main(String[] args) {\n\n    }\n\n}`,
  Python: `def main():\n    pass\n\nif __name__ == "__main__":\n    main()`,
  C: `#include <stdio.h>\n\nint main() {\n\n    return 0;\n}`,
  'C++': `#include <iostream>\nusing namespace std;\n\nint main() {\n\n    return 0;\n}`,
};

export function getMonacoLanguage(lang: string): string {
  const l = lang.toLowerCase();
  if (l === 'java') return 'java';
  if (l === 'python') return 'python';
  if (l === 'c') return 'c';
  if (l === 'c++' || l === 'cpp') return 'cpp';
  return 'cpp';
}
