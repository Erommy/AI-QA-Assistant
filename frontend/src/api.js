const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api/tests';

export async function generateTestPlan(feature) {
  const res = await fetch(`${API_BASE}/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ feature }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.feature || err.error || `Error ${res.status}`);
  }
  return res.json();
}

export async function fetchHistory() {
  const res = await fetch(`${API_BASE}/history`);
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
}

export function exportUrl(id) {
  return `${API_BASE}/export/${id}`;
}

export function pdfExportUrl(id) {
  return `${API_BASE}/export/${id}/pdf`;
}
