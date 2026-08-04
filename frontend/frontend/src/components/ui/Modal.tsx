import Button from './Button';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  icon?: string;
  size?: 'sm' | 'lg' | 'xl';
}

export default function Modal({ isOpen, onClose, title, children, footer, icon, size }: ModalProps) {
  if (!isOpen) return null;

  const sizeClass = size ? `modal-${size}` : '';

  return (
    <>
      <div className="modal show d-block" tabIndex={-1} role="dialog" aria-modal="true">
        <div className={`modal-dialog modal-dialog-centered ${sizeClass}`}>
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">
                {icon && <i className={`bi ${icon} me-2`}></i>}
                {title}
              </h5>
              <button type="button" className="btn-close" onClick={onClose} aria-label="Close"></button>
            </div>
            <div className="modal-body">
              {children}
            </div>
            {footer && (
              <div className="modal-footer">
                {footer}
              </div>
            )}
          </div>
        </div>
      </div>
      <div className="modal-backdrop show"></div>
    </>
  );
}