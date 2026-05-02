import { useState } from 'react';
import GeneratorForm from './components/GeneratorForm.jsx';
import ResultPanel from './components/ResultPanel.jsx';
import HistoryList from './components/HistoryList.jsx';
import './App.css';

export default function App() {
  const [result, setResult] = useState(null);
  const [runId, setRunId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [tab, setTab] = useState('generator');

  function handleResult(data) {
    setResult(data);
    setRunId(data.id ?? null);
    setTab('generator');
  }

  return (
    <div className="app">
      <header>
        <h1>AI QA Assistant</h1>
        <p>Convert requirements into structured test plans with AI</p>
      </header>

      <nav className="main-nav">
        <button className={tab === 'generator' ? 'active' : ''} onClick={() => setTab('generator')}>
          Generator
        </button>
        <button className={tab === 'history' ? 'active' : ''} onClick={() => setTab('history')}>
          History
        </button>
      </nav>

      <main>
        {tab === 'generator' && (
          <>
            <GeneratorForm onResult={handleResult} onLoading={setLoading} />
            {loading && <p className="loading">Generating test plan...</p>}
            <ResultPanel result={result} runId={runId} />
          </>
        )}
        {tab === 'history' && <HistoryList />}
      </main>
    </div>
  );
}
