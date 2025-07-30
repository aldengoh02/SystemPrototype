import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function LoginPage() {
  const navigate = useNavigate();
  const [loginMethod, setLoginMethod] = useState('email');
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { setAuth } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const body = loginMethod === 'email'
        ? { email: identifier, password }
        : { identifier };
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(body)
      });
      const data = await res.json();
      if (res.ok && data.success) {
        // Store userID in localStorage for profile updates
        localStorage.setItem('userID', data.user_id);
        setAuth({
          isLoggedIn: true,
          userRole: data.user_role,
          userName: data.user_name,
          userEmail: data.user_email,
        });
        navigate('/home');
      } else {
        setError(data.message || data.error || 'Login failed.');        
      }
    } catch (err) {
      setError('Login error: ' + err.message);
    }
  };

  return (
    <div style={{
      maxWidth: '400px',
      margin: '50px auto',
      padding: '30px',
      border: '1px solid #ddd',
      borderRadius: '8px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
      fontFamily: 'Arial, sans-serif'
    }}>
      <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Sign In</h2>

      <div style={{ textAlign: 'center', marginBottom: '20px' }}>
        <button
          type="button"
          onClick={() => { setLoginMethod('email'); setPassword(''); setError(''); }}
          style={{
            background: loginMethod === 'email' ? '#0046be' : '#f4f4f4',
            color: loginMethod === 'email' ? 'white' : '#333',
            border: '1px solid #ccc',
            padding: '10px 16px',
            marginRight: '8px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          Email
        </button>
        <button
          type="button"
          onClick={() => { setLoginMethod('accountId'); setPassword(''); setError(''); }}
          style={{
            background: loginMethod === 'accountId' ? '#0046be' : '#f4f4f4',
            color: loginMethod === 'accountId' ? 'white' : '#333',
            border: '1px solid #ccc',
            padding: '10px 16px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          Account ID
        </button>
      </div>

      <form onSubmit={handleLogin}>
        <input
          type={loginMethod === 'email' ? 'email' : 'text'}
          value={identifier}
          onChange={e => setIdentifier(e.target.value)}
          placeholder={loginMethod === 'email' ? 'Email Address' : 'Account ID (7-digit number)'}
          required
          style={{
            width: '94%',
            padding: '12px',
            marginBottom: '12px',
            borderRadius: '4px',
            border: '1px solid #ccc',
            fontSize: '16px'
          }}
        />

        {loginMethod === 'email' && (
          <input
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            placeholder="Password"
            required
            style={{
              width: '94%',
              padding: '12px',
              marginBottom: '12px',
              borderRadius: '4px',
              border: '1px solid #ccc',
              fontSize: '16px'
            }}
          />
        )}

        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '16px' }}>
          <input
            type="checkbox"
            id="remember"
            style={{ marginRight: '8px', width: '16px', height: '16px' }}
          />
          <label htmlFor="remember" style={{ fontSize: '14px', cursor: 'pointer' }}>
            Keep me signed in.
          </label>
        </div>

        <button
          type="submit"
          style={{
            width: '100%',
            backgroundColor: '#0046be',
            color: 'white',
            padding: '12px',
            border: 'none',
            borderRadius: '4px',
            fontWeight: 'bold',
            fontSize: '16px',
            cursor: 'pointer',
            marginBottom: '12px'
          }}
        >
          Continue
        </button>

        {error && <p style={{ color: 'red', fontSize: '14px' }}>{error}</p>}
      </form>

      <p style={{ textAlign: 'center', fontSize: '14px', margin: '12px 0' }}>
        <Link to="/forgot-password" style={{ color: '#0046be', textDecoration: 'none' }}>
          Forgot Password?
        </Link>
      </p>

      <hr style={{ margin: '20px 0' }} />

      <p style={{ textAlign: 'center', fontSize: '14px' }}>
        Don't have an account?{' '}
        <Link to="/register" style={{ color: '#0046be', fontWeight: 'bold', textDecoration: 'none' }}>
          Create an account
        </Link>
      </p>
    </div>
  );
}
