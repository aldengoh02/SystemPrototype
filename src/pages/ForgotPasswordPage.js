import React, { useState } from 'react';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/password-reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ email })
      });
      const data = await res.json();
      if (res.ok && data.message) {
        setMessage('Password reset link has been sent to your email.');
        setError('');
      } else {
        setError(data.error || data.message || 'Failed to send reset link.');
        setMessage('');
      }
    } catch (err) {
      setError('Error: ' + err.message);
      setMessage('');
    }
  };
  
 return (
    <div style={{ maxWidth: '400px', margin: '40px auto', padding: '30px', borderRadius: '12px', boxShadow: '0 2px 12px rgba(0,0,0,0.15)', background: '#fff' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Forgot Password</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Enter your email address"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className="form-input"
          style={{
            width: '90%',
            padding: '16px',
            marginBottom: '16px',
            borderRadius: '8px',
            border: '1px solid #c5c5c5',
            fontSize: '16px',
            fontWeight: '600',
            outline: 'none',
            boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.1)',
            transition: 'border-color 0.2s ease-in-out',
        }}
        onFocus={(e) => e.target.style.borderColor = '#0046be'}
        onBlur={(e) => e.target.style.borderColor = '#c5c5c5'}
        />
        <button type="submit" style={{ width: '100%', backgroundColor: '#0046be', color: 'white', padding: '12px', fontWeight: 'bold', border: 'none', borderRadius: '4px', marginTop: '15px', cursor: 'pointer' }}>
          Send Reset Link
        </button>
      </form>
      {message && <p style={{ color: 'green', textAlign: 'center', marginTop: '15px' }}>{message}</p>}
      {error && <p style={{ color: 'red', textAlign: 'center', marginTop: '15px' }}>{error}</p>}
    </div>
  );
}

