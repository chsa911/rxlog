// frontend-react/src/App.jsx
import React, { useState, useEffect } from 'react';
import { parseDimensionToMM } from './lib/dimensions';

export default function App() {
  // core form state
  const [author, setAuthor] = useState('');
  const [publisher, setPublisher] = useState('');
  const [pages, setPages] = useState('');

  // match backend naming
  const [titleKeyword, setTitleKeyword] = useState('');
  const [titleKeywordPosition, setTitleKeywordPosition] = useState('');
  const [titleKeyword2, setTitleKeyword2] = useState('');
  const [titleKeyword2Position, setTitleKeyword2Position] = useState('');
  const [titleKeyword3, setTitleKeyword3] = useState('');
  const [titleKeyword3Position, setTitleKeyword3Position] = useState('');

  // dimensions (raw + normalized)
  const [widthRaw, setWidthRaw]   = useState('');
  const [heightRaw, setHeightRaw] = useState('');
  const [widthMM, setWidthMM]     = useState(null);
  const [heightMM, setHeightMM]   = useState(null);

  // derived cm
  const widthCm  = widthMM  != null ? (widthMM  / 10) : null;
  const heightCm = heightMM != null ? (heightMM / 10) : null;

  // barcode & status
  const [barcode, setBarcode]     = useState('');
  const [color, setColor]         = useState('');
  const [position, setPosition]   = useState('');
  const [readingStatus, setReadingStatus] = useState('in_progress');
  const [topBook, setTopBook]     = useState(false);

  const [log, setLog] = useState([]);

  function handleWidthBlur()  { setWidthMM(parseDimensionToMM(widthRaw)); }
  function handleHeightBlur() { setHeightMM(parseDimensionToMM(heightRaw)); }

  // release helper
  async function releaseCurrentBarcode(reason = '') {
    if (!barcode) return;
    try {
      await fetch('/api/barcodes/release', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ code: barcode })
      });
      setLog(l => [`Barcode freigegeben${reason ? `: ${reason}` : ''}: ${barcode}`, ...l]);
    } catch (_) { /* ignore */ }
    setBarcode(''); setColor(''); setPosition('');
  }

  // auto-assign on valid dimensions
  useEffect(() => {
    let cancelled = false;

    async function maybeAssign() {
      const ready = Number.isFinite(widthMM) && Number.isFinite(heightMM) && widthMM > 0 && heightMM > 0;
      if (!ready) {
        if (barcode) await releaseCurrentBarcode('Dimensionen gelöscht/ungültig');
        return;
      }
      if (barcode) await releaseCurrentBarcode('Dimensionen geändert');

      const payload = {
        // send BOTH so old/new backends work
        width:    widthMM,
        height:   heightMM,
        widthCm:  widthCm,
        heightCm: heightCm
      };

      try {
        const res = await fetch('/api/barcodes/assignForDimensions', {
          method: 'POST',
          headers: { 'content-type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (cancelled) return;

        if (!res.ok) {
          let msg = `Kein verfügbarer Barcode (${res.status}).`;
          try {
            const err = await res.json();
            if (err?.type === 'NO_RULE_APPLIES') msg = 'Kein Größen-Regel passt zu den eingegebenen Maßen.';
            if (err?.type === 'NO_STOCK')        msg = 'Kein Barcode verfügbar für die ermittelte Kombination.';
            if (err?.type === 'DB_UNAVAILABLE')  msg = 'Barcode-Service/DB derzeit nicht erreichbar.';
          } catch {}
          setBarcode(''); setColor(''); setPosition('');
          setLog(l => [msg, ...l]);
          return;
        }

        const data = await res.json().catch(() => ({}));
        if (cancelled) return;

        const code = data.barcode ?? data.code ?? '';
        const clr  = data.rule    ?? data.color ?? '';
        const pos  = data.position ?? '';

        if (!code) {
          setLog(l => ['Antwort ohne barcode/code Feld erhalten.', ...l]);
          return;
        }

        setBarcode(code); setColor(clr); setPosition(pos);
        setLog(l => [`Barcode zugewiesen: ${code} (${clr || '-'} · ${pos || '-'})`, ...l]);
      } catch (e) {
        if (cancelled) return;
        setLog(l => [`Netzwerkfehler bei Zuweisung: ${e.message}`, ...l]);
      }
    }

    void maybeAssign();
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [widthMM, heightMM]);

  async function onSubmit(e) {
    e.preventDefault();
    if (!barcode) { alert('Bitte zuerst Barcode ermitteln.'); return; }
    if (!Number.isFinite(widthMM) || !Number.isFinite(heightMM)) {
      alert('Bitte Buchbreite/-höhe eingeben.'); return;
    }

    const payload = {
      author,
      publisher,
      pages: pages ? Number(pages) : null,

      // match backend DTO exactly
      titleKeyword:              titleKeyword      || null,
      titleKeywordPosition:      titleKeywordPosition ? Number(titleKeywordPosition) : null,
      titleKeyword2:             titleKeyword2     || null,
      titleKeyword2Position:     titleKeyword2Position ? Number(titleKeyword2Position) : null,
      titleKeyword3:             titleKeyword3     || null,
      titleKeyword3Position:     titleKeyword3Position ? Number(titleKeyword3Position) : null,

      barcode,
      readingStatus,
      topBook,

      // mm for register service
      width:  widthMM,
      height: heightMM
    };

    try {
      const res = await fetch('/api/register/book', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        await releaseCurrentBarcode('Registrierung fehlgeschlagen');
        throw new Error('Registrierung fehlgeschlagen: ' + res.status);
      }

      const data = await res.json();
      setLog(l => [`Gespeichert: ${data.bookId} mit ${barcode} [${readingStatus}]`, ...l]);
      resetForm(); // success
    } catch (err) {
      setLog(l => [`Fehler: ${err.message}`, ...l]);
      alert(err.message);
    }
  }

  function resetForm() {
    setAuthor(''); setPublisher(''); setPages('');
    setTitleKeyword(''); setTitleKeywordPosition('');
    setTitleKeyword2(''); setTitleKeyword2Position('');
    setTitleKeyword3(''); setTitleKeyword3Position('');
    setWidthRaw(''); setHeightRaw(''); setWidthMM(null); setHeightMM(null);
    setBarcode(''); setColor(''); setPosition('');
    setReadingStatus('in_progress'); setTopBook(false);
  }

  return (
    <div style={{fontFamily:'system-ui, sans-serif', padding:'2rem', maxWidth:900, margin:'0 auto'}}>
      <h1>RxLog – Buch registrieren</h1>

      <form onSubmit={onSubmit} className="grid" style={{gap: '0.75rem', maxWidth: 720}}>
        <label>Autor
          <input required value={author} onChange={e=>setAuthor(e.target.value)} placeholder="z. B. T. Fontane" />
        </label>

        <label>Verlag
          <input required value={publisher} onChange={e=>setPublisher(e.target.value)} placeholder="z. B. Suhrkamp" />
        </label>

        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.5rem'}}>
          <label>Schlagwort 1
            <input required value={titleKeyword} onChange={e=>setTitleKeyword(e.target.value)} />
          </label>
          <label>Position 1
            <input required type="number" min={1} value={titleKeywordPosition ?? ''} onChange={e=>setTitleKeywordPosition(e.target.value)} />
          </label>

          <label>Schlagwort 2
            <input value={titleKeyword2} onChange={e=>setTitleKeyword2(e.target.value)} />
          </label>
          <label>Position 2
            <input type="number" min={1} value={titleKeyword2Position ?? ''} onChange={e=>setTitleKeyword2Position(e.target.value)} />
          </label>

          <label>Schlagwort 3
            <input value={titleKeyword3} onChange={e=>setTitleKeyword3(e.target.value)} />
          </label>
          <label>Position 3
            <input type="number" min={1} value={titleKeyword3Position ?? ''} onChange={e=>setTitleKeyword3Position(e.target.value)} />
          </label>
        </div>

        <label>Seitenzahl
          <input required type="number" min={1} value={pages} onChange={e=>setPages(e.target.value)} />
        </label>

        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.5rem'}}>
          <label>Buchbreite (mm oder cm)
            <input
              value={widthRaw}
              onChange={e=>setWidthRaw(e.target.value)}
              onBlur={handleWidthBlur}
              placeholder="z. B. 105 mm / 10,5 cm / 10"
              inputMode="decimal"
            />
          </label>
          <label>Buchhöhe (mm oder cm)
            <input
              value={heightRaw}
              onChange={e=>setHeightRaw(e.target.value)}
              onBlur={handleHeightBlur}
              placeholder="z. B. 190 mm / 19 cm / 19"
              inputMode="decimal"
            />
          </label>
        </div>

        {(widthMM != null || heightMM != null) && (
          <div style={{color:'#555', fontSize:13}}>
            Normalisiert:&nbsp;
            {widthMM  != null && <>Breite <b>{widthMM} mm</b> ({(widthMM/10).toFixed(1)} cm)</>}
            {heightMM != null && <>, Höhe <b>{heightMM} mm</b> ({(heightMM/10).toFixed(1)} cm)</>}
          </div>
        )}

        <fieldset style={{marginTop:'0.5rem'}}>
          <legend>Lesestatus</legend>
          <label><input type="radio" name="rs" value="in_progress"
                        checked={readingStatus==='in_progress'} onChange={e=>setReadingStatus(e.target.value)} /> In Bearbeitung</label>{' '}
          <label><input type="radio" name="rs" value="finished"
                        checked={readingStatus==='finished'} onChange={e=>setReadingStatus(e.target.value)} /> Fertig gelesen</label>{' '}
          <label><input type="radio" name="rs" value="abandoned"
                        checked={readingStatus==='abandoned'} onChange={e=>setReadingStatus(e.target.value)} /> Vorzeitig beendet</label>
        </fieldset>

        <label><input type="checkbox" checked={topBook} onChange={e=>setTopBook(e.target.checked)} /> Top-Buch</label>

        <div style={{display:'flex', gap:8, marginTop:8}}>
          <button type="submit" disabled={!barcode}>Buch registrieren</button>
        </div>

        {barcode && (
          <div style={{background:'#f6f8fa', padding:'0.5rem', borderRadius:8, marginTop:'0.5rem'}}>
            <b>Barcode:</b> {barcode} {color || position ? <> (<span>{color || '-'}</span> · <span>{position || '-'}</span>)</> : null}
          </div>
        )}
      </form>

      <h3 style={{marginTop:'1rem'}}>Logs</h3>
      <pre style={{background:'#f6f8fa', padding:'0.75rem', borderRadius:8, minHeight:80}}>
        {log.join('\n')}
      </pre>
    </div>
  );
}