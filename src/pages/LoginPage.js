import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';

export default function LoginPage({ setIsLoggedIn }) {
  const navigate = useNavigate();
  const [loginMethod, setLoginMethod] = useState('email'); // email or userID
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const body = loginMethod === 'email'
        ? { email: identifier, password }
        : { identifier };
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      const data = await res.json();
      if (res.ok && data.success) {
        setIsLoggedIn(true);
        navigate('/home');
      } else {
        setError(data.message || data.error || 'Login failed.');
      }
    } catch (err) {
      setError('Login error: ' + err.message);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Login</h2>
      <div style={{ marginBottom: '10px', textAlign: 'center' }}>
        <button
          type="button"
          onClick={() => { setLoginMethod('email'); setPassword(''); setError(''); }}
          style={{
            background: loginMethod === 'email' ? '#4a90e2' : '#eee',
            color: loginMethod === 'email' ? '#fff' : '#333',
            border: 'none',
            padding: '8px 16px',
            marginRight: '5px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Login with Email
        </button>
        <button
          type="button"
          onClick={() => { setLoginMethod('userID'); setPassword(''); setError(''); }}
          style={{
            background: loginMethod === 'userID' ? '#4a90e2' : '#eee',
            color: loginMethod === 'userID' ? '#fff' : '#333',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Login with User ID
        </button>
      </div>
      <form onSubmit={handleLogin}>
        <input
          type={loginMethod === 'email' ? 'email' : 'text'}
          value={identifier}
          onChange={e => setIdentifier(e.target.value)}
          placeholder={loginMethod === 'email' ? 'Email' : 'User ID'}
          required
        />
        {loginMethod === 'email' && (
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" required />
        )}
        <button type="submit">Login</button>
        {error && <p style={{ color: 'red' }}>{error}</p>}
      </form>
      <p style={{ marginTop: '10px', textAlign: 'center' }}>
        <Link to="/forgot-password" style={{ color: '#4a90e2', textDecoration: 'none' }}>
          Forgot Password?
        </Link>
      </p>
      <p style={{ marginTop: '10px', textAlign: 'center' }}>
        Don’t have an account?{' '}
        <Link to="/register" style={{ color: '#4a90e2', textDecoration: 'none', fontWeight: 'bold' }}>
          Create an account
        </Link>
      </p>
    </div>
  );
}