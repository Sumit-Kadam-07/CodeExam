interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export default function EmptyState({ icon = 'bi-inbox', title, description, action }: EmptyStateProps) {
  return (
    <div className="text-center py-5">
      <i className={`bi ${icon} display-4 text-muted mb-3 d-block`}></i>
      <h5 className="fw-semibold text-secondary">{title}</h5>
      {description && <p className="text-muted small mb-4">{description}</p>}
      {action && <div>{action}</div>}
    </div>
  );
}