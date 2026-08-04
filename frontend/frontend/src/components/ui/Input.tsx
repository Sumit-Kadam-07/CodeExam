import { useId } from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helpText?: string;
}

export default function Input({
  label,
  error,
  helpText,
  id,
  className = '',
  ...props
}: InputProps) {
  const generatedId = useId();
  const inputId = id || generatedId;
  
  return (
    <div className="mb-3">
      {label && (
        <label htmlFor={inputId} className="form-label fw-semibold">
          {label} {props.required && <span className="text-danger">*</span>}
        </label>
      )}
      <input
        id={inputId}
        className={`form-control ${error ? 'is-invalid' : ''} ${className}`}
        {...props}
      />
      {error && <div className="invalid-feedback">{error}</div>}
      {helpText && !error && <div className="form-text">{helpText}</div>}
    </div>
  );
}