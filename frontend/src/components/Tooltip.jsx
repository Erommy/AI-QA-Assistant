import { useState } from 'react';

export const RISK_TOOLTIP = {
  High:   'High risk — involves sensitive areas like payments, auth, or data integrity. Failures have serious consequences.',
  Medium: 'Medium risk — moderate complexity or impact, e.g. file uploads, integrations, or user data changes.',
  Low:    'Low risk — simple, low-impact feature such as display logic or basic filtering.',
};

export const CONFIDENCE_TOOLTIP =
  'Confidence score (0–100%) reflects how complete the AI response was. ' +
  '90%+ means all sections are well-populated. Below 50% means some sections were missing — consider resubmitting with more detail.';

export default function Tooltip({ text, children }) {
  const [visible, setVisible] = useState(false);
  return (
    <span
      className="tooltip-wrap"
      onMouseEnter={() => setVisible(true)}
      onMouseLeave={() => setVisible(false)}
    >
      {children}
      {visible && <span className="tooltip-box">{text}</span>}
    </span>
  );
}
