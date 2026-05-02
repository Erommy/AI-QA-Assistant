import { useEffect, useState } from 'react';
import { fetchHistory, exportUrl, pdfExportUrl } from '../api.js';
import Tooltip, { RISK_TOOLTIP, CONFIDENCE_TOOLTIP } from './Tooltip.jsx';

function riskColor(level) {
  if (level === 'High')   return '#e53e3e';
  if (level === 'Medium') return '#dd6b20';
  return '#38a169';
}

export default function HistoryList() {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory()
      .then(setHistory)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading history...</p>;
  if (history.length === 0) return <p className="empty">No history yet.</p>;

  return (
    <div className="history-list">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Feature</th>
            <th>Risk</th>
            <th>Score</th>
            <th>Date</th>
            <th>Export</th>
          </tr>
        </thead>
        <tbody>
          {history.map(run => {
            const riskLevel = run.riskLevel || 'N/A';
            const riskTip = RISK_TOOLTIP[riskLevel] || 'Risk level set by the AI based on the nature of the feature.';
            return (
              <tr key={run.id}>
                <td>{run.id}</td>
                <td className="feature-cell">{run.feature}</td>
                <td>
                  <Tooltip text={riskTip}>
                    <span className="badge" style={{ background: riskColor(riskLevel) }}>
                      {riskLevel} ⓘ
                    </span>
                  </Tooltip>
                </td>
                <td>{run.confidenceScore != null
                  ? <Tooltip text={CONFIDENCE_TOOLTIP}>
                      <span style={{ cursor: 'default' }}>
                        {(run.confidenceScore * 100).toFixed(0)}% ⓘ
                      </span>
                    </Tooltip>
                  : 'N/A'}
                </td>
                <td>{new Date(run.createdAt).toLocaleString()}</td>
                <td>
                  <a href={exportUrl(run.id)} className="export-link">↓ .md</a>
                  {' '}
                  <a href={pdfExportUrl(run.id)} className="export-link export-link-pdf">↓ .pdf</a>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
