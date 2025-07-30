import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [tokenFromUrl, setTokenFromUrl] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const urlToken = params.get('token');
    if (urlToken) {
      setToken(urlToken);
      setTokenFromUrl(urlToken);
    }
  }, [location.search]);

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    try {
      const res = await fetch('/password-reset', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ token, newPassword }),
      });

      const data = await res.json();

      if (res.ok && data.message) {
        setSuccessMessage('Password reset successfully. You can now log in.');
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setError(data.error || data.message || 'Password reset failed.');
      }
    } catch (err) {
      setError('An error occurred: ' + err.message);
    }
  };

return (
    <div
        style={{
        maxWidth: '400px',
        margin: '40px auto',
        padding: '30px',
        borderRadius: '12px',
        boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
        background: '#fff',
        }}
    >
        <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>Reset Password</h2>
        <form onSubmit={handleResetPassword}>
        {!tokenFromUrl && (
            <input
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Reset Token"
            required
            style={{
                width: '100%',
                padding: '16px',
                marginBottom: '16px',
                borderRadius: '8px',
                border: '1px solid #ccc',
                fontSize: '16px',
                fontWeight: '600',
                boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.1)',
                outline: 'none',
            }}
            onFocus={(e) => (e.target.style.borderColor = '#0046be')}
            onBlur={(e) => (e.target.style.borderColor = '#ccc')}
            />
        )}
        <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="New Password"
            required
            style={{
            width: '90%',
            padding: '16px',
            marginBottom: '16px',
            borderRadius: '8px',
            border: '1px solid #ccc',
            fontSize: '16px',
            fontWeight: '600',
            boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.1)',
            outline: 'none',
            }}
            onFocus={(e) => (e.target.style.borderColor = '#0046be')}
            onBlur={(e) => (e.target.style.borderColor = '#ccc')}
        />
        <button
            type="submit"
            style={{
            width: '100%',
            backgroundColor: '#0046be',
            color: 'white',
            padding: '12px',
            fontWeight: 'bold',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            }}
        >
            Reset Password
        </button>
        {error && <p style={{ color: 'red', textAlign: 'center', marginTop: '10px' }}>{error}</p>}
        {successMessage && (
            <p style={{ color: 'green', textAlign: 'center', marginTop: '10px' }}>{successMessage}</p>
        )}
        </form>
    </div>
    );
    }