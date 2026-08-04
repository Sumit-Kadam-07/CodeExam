export function validateRequired(value: string, fieldName: string): string | null {
  if (!value || value.trim() === '') {
    return `${fieldName} is required.`;
  }
  return null;
}

export function validateMinLength(value: string, min: number, fieldName: string): string | null {
  if (value && value.trim().length < min) {
    return `${fieldName} must be at least ${min} characters.`;
  }
  return null;
}

export function validateMaxLength(value: string, max: number, fieldName: string): string | null {
  if (value && value.trim().length > max) {
    return `${fieldName} must be at most ${max} characters.`;
  }
  return null;
}

export function validateNumber(value: number, min: number, max: number, fieldName: string): string | null {
  if (isNaN(value) || value < min || value > max) {
    return `${fieldName} must be between ${min} and ${max}.`;
  }
  return null;
}

export function validateTestCases(testCases: Array<{ inputData: string; expectedOutput: string }>): string | null {
  const hasValid = testCases.some(
    (tc) => tc.inputData.trim() !== '' && tc.expectedOutput.trim() !== ''
  );
  if (!hasValid) {
    return 'At least one test case with non-empty input and expected output is required.';
  }
  return null;
}

export function validateFileExtension(filename: string, allowedExtensions: string[]): string | null {
  const ext = filename.split('.').pop()?.toLowerCase();
  if (!ext || !allowedExtensions.includes(ext)) {
    return `Unsupported file type. Allowed: ${allowedExtensions.join(', ')}`;
  }
  return null;
}
