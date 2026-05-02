import { useState } from 'react';
import { generateTestPlan } from '../api.js';

export default function GeneratorForm({ onResult, onLoading }) {
  const [feature, setFeature] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!feature.trim()) return;
    onLoading(true);
    try {
      const result = await generateTestPlan(feature.trim());
      onResult(result);
    } catch (err) {
      alert('Error: ' + err.message);
    } finally {
      onLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="generator-form">
      <label htmlFor="feature">Feature / User Story</label>
      <textarea
        id="feature"
        rows={4}
        placeholder="As a user, I want to reset my password using my email address"
        value={feature}
        onChange={e => setFeature(e.target.value)}
      />
      <button type="submit">Generate Test Plan</button>
    </form>
  );
}
