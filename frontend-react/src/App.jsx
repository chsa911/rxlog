import React, { useState } from 'react'
export default function App(){
  const [title,setTitle]=useState(''); const [author,setAuthor]=useState(''); const [publisher,setPublisher]=useState(''); const [barcode,setBarcode]=useState(''); const [log,setLog]=useState([])
  async function registerBook(e){ e.preventDefault(); const res=await fetch('/api/register/book',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({title,author,publisher,barcode,sizeRuleId:1})}); const data=await res.json().catch(()=>({})); setLog(l=>[`Registered: ${JSON.stringify(data)}`,...l])}
  return (<div style={{fontFamily:'system-ui',padding:'2rem',maxWidth:780,margin:'0 auto'}}>
    <h1>RxLog Demo</h1>
    <form onSubmit={registerBook} style={{display:'grid',gap:8}}>
      <input placeholder="Title" value={title} onChange={e=>setTitle(e.target.value)} required />
      <input placeholder="Author" value={author} onChange={e=>setAuthor(e.target.value)} required />
      <input placeholder="Publisher" value={publisher} onChange={e=>setPublisher(e.target.value)} required />
      <input placeholder="Barcode (optional)" value={barcode} onChange={e=>setBarcode(e.target.value)} />
      <button type="submit">Register</button>
    </form>
    <pre style={{background:'#f6f8fa',padding:12,marginTop:16,borderRadius:8}}>{log.join('\n')}</pre>
  </div>)
}
