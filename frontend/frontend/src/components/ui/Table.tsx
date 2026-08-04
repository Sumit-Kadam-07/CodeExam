interface TableProps {
  children: React.ReactNode;
  className?: string;
  responsive?: boolean;
}

export default function Table({ children, className = '', responsive = true }: TableProps) {
  const tableContent = (
    <table className={`table table-hover mb-0 ${className}`}>
      {children}
    </table>
  );

  if (responsive) {
    return <div className="table-responsive">{tableContent}</div>;
  }

  return tableContent;
}

export function TableHead({ children, className = '' }: { children: React.ReactNode, className?: string }) {
  return (
    <thead className={`table-light ${className}`}>
      {children}
    </thead>
  );
}

export function TableBody({ children, className = '' }: { children: React.ReactNode, className?: string }) {
  return <tbody className={className}>{children}</tbody>;
}

export function TableRow({ children, className = '' }: { children: React.ReactNode, className?: string }) {
  return <tr className={className}>{children}</tr>;
}

export function TableHeader({ children, className = '' }: { children: React.ReactNode, className?: string }) {
  return <th className={className}>{children}</th>;
}

export function TableCell({ children, className = '', colSpan }: { children: React.ReactNode, className?: string, colSpan?: number }) {
  return <td className={className} colSpan={colSpan}>{children}</td>;
}