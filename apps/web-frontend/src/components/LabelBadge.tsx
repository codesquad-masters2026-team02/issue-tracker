import type { LabelResponse } from '../lib/api';
import './LabelBadge.css';

interface LabelBadgeProps {
  label: Pick<LabelResponse, 'name' | 'backgroundColor' | 'textColor'>;
}

export function LabelBadge({ label }: LabelBadgeProps) {
  return (
    <span
      className="label-badge"
      style={{
        backgroundColor: label.backgroundColor,
        color: label.textColor === 'LIGHT' ? '#FFFFFF' : '#14142B',
      }}
    >
      {label.name}
    </span>
  );
}
