import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';

export default function LoginPage({ setIsLoggedIn }) {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const data = await res.json();
      if (res.ok && data.success) {
        setIsLoggedIn(true);
        navigate('/home');
      } else {
        setError(data.message || 'Login failed.');
      }
    } catch (err) {
      setError('Login error: ' + err.message);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required />
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" required />
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