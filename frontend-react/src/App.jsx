import React, { useState, useEffect } from 'react';
import { parseDimensionToMM } from './lib/dimensions';

export default function App() {
  // core form state
  const [author, setAuthor] = useState('');
  const [publisher, setPublisher] = useState('');
  const [pages, setPages] = useState('');
  const [kw1, setKw1] = useState('');
  const [kw1Pos, setKw1Pos] = useState('');
  const [kw2, setKw2] = useState(''); const [kw2Pos, setKw2Pos] = useState('');
  const [kw3, setKw3] = useState(''); const [kw3Pos, setKw3Pos] = useState('');

  // dimensions (raw + normalized)
  const [widthRaw, setWidthRaw] = useState('');
  const [heightRaw, setHeightRaw] = useState('');
  const [widthMM, setWidthMM] = useState(null);
  const [heightMM, setHeightMM] = useState(null);

  // barcode & status
  const [barcode, setBarcode] = useState('');
  const [color, setColor] = useState('');
  const [position, setPosition] = useState('');
  const [readingStatus, setReadingStatus] = useState('in_progress'); // or 'finished' | 'abandoned'
  const [topBook, setTopBook] = useState(false);

  const [log, setLog] = useState([]);

  function handleWidthBlur()  { setWidthMM(parseDimensionToMM(widthRaw)); }
  function handleHeightBlur() { setHeightMM(parseDimensionToMM(heightRaw)); }

  // Helper: release current (unsaved) barcode if any
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
    setBarcode('');
    setColor('');
    setPosition('');
  }

  // Auto-assign a barcode ONLY when width & height are present.
  useEffect(() => {
    let cancelled = false;

    async function maybeAssign() {
      const ready = !!widthMM && !!heightMM;
      if (!ready) {
        // Dimensions cleared/invalid -> release any unsaved code
        if (barcode) await releaseCurrentBarcode('Dimensionen gelöscht');
        return;
      }

      // Re-assign when dimensions change: release any previous unsaved code first
      if (barcode) await releaseCurrentBarcode('Dimensionen geändert');

      const res = await fetch('/api/barcodes/assignForDimensions', {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ width: widthMM, height: heightMM })
      });

      if (cancelled) return;

      if (!res.ok) {
        setBarcode('');
        setColor('');
        setPosition('');
        setLog(l => [`Kein verfügbarer Barcode (${res.status}).`, ...l]);
        return;
      }
      const data = await res.json();
      if (cancelled) return;

      setBarcode(data.code);
      setColor(data.color || '');
      setPosition(data.position || '');
      setLog(l => [`Barcode zugewiesen: ${data.code} (${data.color || '-'} · ${data.position || '-'})`, ...l]);
    }

    maybeAssign();
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [widthMM, heightMM]); // intentionally not depending on `barcode` to avoid loops

  async function onSubmit(e) {
    e.preventDefault();
    if (!barcode) { alert('Bitte zuerst Barcode ermitteln.'); return; }
    if (!widthMM || !heightMM) { alert('Bitte Buchbreite/-höhe eingeben.'); return; }

    const payload = {
      author, publisher,
      pages: Number(pages),

      titleKeyword: kw1,
      titleKeywordPosition: Number(kw1Pos),
      titleKeyword2: kw2 || null,
      titleKeyword2Position: kw2Pos ? Number(kw2Pos) : null,
      titleKeyword3: kw3 || null,
      titleKeyword3Position: kw3Pos ? Number(kw3Pos) : null,

      barcode,
      readingStatus,
      topBook,

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
        // On failure, release the assigned (unsaved) barcode
        await releaseCurrentBarcode('Registrierung fehlgeschlagen');
        throw new Error('Registrierung fehlgeschlagen: ' + res.status);
      }

      const data = await res.json();
      setLog(l => [`Gespeichert: ${data.bookId} mit ${barcode} [${readingStatus}]`, ...l]);

      // SUCCESS → clear everything for the next book; do NOT release (barcode is now used)
      resetForm();
    } catch (err) {
      setLog(l => [`Fehler: ${err.message}`, ...l]);
      alert(err.message);
    }
  }

  function resetForm() {
    setAuthor(''); setPublisher(''); setPages('');
    setKw1(''); setKw1Pos(''); setKw2(''); setKw2Pos(''); setKw3(''); setKw3Pos('');
    setWidthRaw(''); setHeightRaw(''); setWidthMM(null); setHeightMM(null);
    setBarcode(''); setColor(''); setPosition('');
    setReadingStatus('in_progress'); setTopBook(false);
  }

  return (
    <div style={{fontFamily:'system-ui, sans-serif', padding:'2rem', maxWidth:900, margin:'0 auto'}}>
      <h1>RxLog – Buch registrieren</h1>

      <form onSubmit={onSubmit} className="grid" style={{gap: '0.75rem', maxWidth: 600}}>
        <label>Autor
          <input required value={author} onChange={e=>setAuthor(e.target.value)} placeholder="z. B. T. Fontane" />
        </label>
        <label>Verlag
          <input required value={publisher} onChange={e=>setPublisher(e.target.value)} placeholder="z. B. Suhrkamp" />
        </label>

        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.5rem'}}>
          <label>Schlagwort 1
            <input required value={kw1} onChange={e=>setKw1(e.target.value)} />
          </label>
          <label>Position 1
            <input required type="number" min={1} value={kw1Pos} onChange={e=>setKw1Pos(e.target.value)} />
          </label>
          <label>Schlagwort 2
            <input value={kw2} onChange={e=>setKw2(e.target.value)} />
          </label>
          <label>Position 2
            <input type="number" min={1} value={kw2Pos} onChange={e=>setKw2Pos(e.target.value)} />
          </label>
          <label>Schlagwort 3
            <input value={kw3} onChange={e=>setKw3(e.target.value)} />
          </label>
          <label>Position 3
            <input type="number" min={1} value={kw3Pos} onChange={e=>setKw3Pos(e.target.value)} />
          </label>
        </div>

        <label>Seitenzahl
          <input required type="number" min={1} value={pages} onChange={e=>setPages(e.target.value)} />
        </label>

        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'0.5rem'}}>
          <label>Buchbreite (mm oder cm)
            <input value={widthRaw} onChange={e=>setWidthRaw(e.target.value)} onBlur={handleWidthBlur}
                   placeholder="z. B. 105 oder 10,5 cm" inputMode="decimal" />
          </label>
          <label>Buchhöhe (mm oder cm)
            <input value={heightRaw} onChange={e=>setHeightRaw(e.target.value)} onBlur={handleHeightBlur}
                   placeholder="z. B. 190 mm oder 19 cm" inputMode="decimal" />
          </label>
        </div>
        {(widthMM || heightMM) && (
          <div style={{color:'#555', fontSize:13}}>
            Normalisiert:&nbsp;
            {widthMM && <>Breite <b>{widthMM} mm</b></>}
            {heightMM && <>, Höhe <b>{heightMM} mm</b></>}
          </div>
        )}

        <fieldset style={{marginTop:'0.5rem'}}>
          <legend>Lesestatus</legend>
          <label><input type="radio" name="rs" value="in_progress"
                        checked={readingStatus==='in_progress'} onChange={e=>setReadingStatus(e.target.value)} /> In Bearbeitung</label>&nbsp;
          <label><input type="radio" name="rs" value="finished"
                        checked={readingStatus==='finished'} onChange={e=>setReadingStatus(e.target.value)} /> Fertig gelesen</label>&nbsp;
          <label><input type="radio" name="rs" value="abandoned"
                        checked={readingStatus==='abandoned'} onChange={e=>setReadingStatus(e.target.value)} /> Vorzeitig beendet</label>
        </fieldset>

        <label><input type="checkbox" checked={topBook} onChange={e=>setTopBook(e.target.checked)} /> Top-Buch</label>

        <div style={{display:'flex', gap:'0.5rem', marginTop:'0.25rem'}}>
          {/* No manual "pick" button; barcode is auto-assigned when width & height are valid */}
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