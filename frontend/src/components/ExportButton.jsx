import { exportUrl, pdfExportUrl } from '../api.js';

export default function ExportButton({ runId }) {
  return (
    <div className="export-group">
      <a href={exportUrl(runId)} className="export-btn" download>
        ↓ Markdown
      </a>
      <a href={pdfExportUrl(runId)} className="export-btn export-btn-pdf" download>
        ↓ PDF
      </a>
    </div>
  );
}
