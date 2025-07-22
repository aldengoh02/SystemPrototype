import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    try {
      const res = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword }),
      });

      const data = await res.json();

      if (res.ok && data.success) {
        setSuccessMessage('Password reset successfully. You can now log in.');
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setError(data.message || 'Password reset failed.');
      }
    } catch (err) {
      setError('An error occurred: ' + err.message);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Reset Password</h2>
      <form onSubmit={handleResetPassword}>
        <input
          type="text"
          value={token}
          onChange={e => setToken(e.target.value)}
          placeholder="Reset Token"
          required
        />
        <input
          type="password"
          value={newPassword}
          onChange={e => setNewPassword(e.target.value)}
          placeholder="New Password"
          required
        />
        <button type="submit" style={{ marginTop: '10px' }}>
          Reset Password
        </button>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        {successMessage && <p style={{ color: 'green' }}>{successMessage}</p>}
      </form>
    </div>
  );
}
