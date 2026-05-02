import { useState } from 'react';
import ExportButton from './ExportButton.jsx';
import Tooltip, { RISK_TOOLTIP, CONFIDENCE_TOOLTIP } from './Tooltip.jsx';

const TABS = [
  { key: 'testCases',          label: 'Test Cases' },
  { key: 'edgeCases',          label: 'Edge Cases' },
  { key: 'negativeTests',      label: 'Negative Tests' },
  { key: 'securityTests',      label: 'Security Tests' },
  { key: 'apiTests',           label: 'API Tests' },
  { key: 'automationSkeleton', label: 'Automation' },
];

function riskColor(level) {
  if (level === 'High')   return '#e53e3e';
  if (level === 'Medium') return '#dd6b20';
  return '#38a169';
}

export default function ResultPanel({ result, runId }) {
  const [activeTab, setActiveTab] = useState('testCases');

  if (!result) return null;

  const renderTab = () => {
    if (activeTab === 'automationSkeleton') {
      return (
        <div className="skeleton-panels">
          <div className="skeleton-panel">
            <h4>TestNG (Java)</h4>
            <pre className="code-block">{result.automationSkeleton || 'Not generated.'}</pre>
          </div>
          <div className="skeleton-panel">
            <h4>Playwright (TypeScript)</h4>
            <pre className="code-block">{result.playwrightSkeleton || 'Not generated.'}</pre>
          </div>
        </div>
      );
    }
    const items = result[activeTab];
    if (!items || items.length === 0) return <p className="empty">None generated.</p>;
    return (
      <ul>
        {items.map((item, i) => <li key={i}>{item}</li>)}
      </ul>
    );
  };

  const riskLevel = result.riskLevel || 'N/A';
  const riskTip = RISK_TOOLTIP[riskLevel] || 'Risk level set by the AI based on the nature of the feature.';

  return (
    <div className="result-panel">
      <div className="result-header">
        <div>
          {result.summary && <p className="summary">{result.summary}</p>}
          <Tooltip text={riskTip}>
            <span className="badge" style={{ background: riskColor(riskLevel) }}>
              Risk: {riskLevel} ⓘ
            </span>
          </Tooltip>
          <Tooltip text={CONFIDENCE_TOOLTIP}>
            <span className="badge score">
              Confidence: {(result.confidenceScore * 100).toFixed(0)}% ⓘ
            </span>
          </Tooltip>
        </div>
        {runId && <ExportButton runId={runId} />}
      </div>

      <div className="tabs">
        {TABS.map(tab => (
          <button
            key={tab.key}
            className={activeTab === tab.key ? 'tab active' : 'tab'}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="tab-content">{renderTab()}</div>
    </div>
  );
}
