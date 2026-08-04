interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark' | 'outline-primary' | 'outline-secondary' | 'outline-success' | 'outline-danger';
  size?: 'sm' | 'lg';
  icon?: string;
  loading?: boolean;
  loadingText?: string;
}

export default function Button({
  children,
  variant = 'primary',
  size,
  icon,
  loading = false,
  loadingText = 'Loading...',
  className = '',
  disabled,
  type = 'button',
  ...props
}: ButtonProps) {
  const sizeClass = size ? `btn-${size}` : '';
  const variantClass = `btn-${variant}`;
  
  return (
    <button
      type={type}
      className={`btn ${variantClass} ${sizeClass} ${className}`}
      disabled={loading || disabled}
      {...props}
    >
      {loading ? (
        <>
          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          {loadingText}
        </>
      ) : (
        <>
          {icon && <i className={`bi ${icon} ${children ? 'me-2' : ''}`}></i>}
          {children}
        </>
      )}
    </button>
  );
}
