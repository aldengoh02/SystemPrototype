import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

export default function RegisterPage({ setIsLoggedIn }) {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName,
          lastName,
          email,
          password,
          phone,
          status: 'active',
          enrollForPromotions: true,
        })
      });
      const data = await res.json();
      if (res.ok && data.success) {
        
        setIsLoggedIn(true);
        navigate('/');
        
       /*
        alert('Registration successful! Please check your email to verify your account.');
        navigate('/login');
        */
      } else {
        setError(data.message || 'Registration failed.');
      }
    } catch (err) {
      setError('Register error: ' + err.message);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Register</h2>
      <form onSubmit={handleRegister}>
        <input
          type="text"
          value={firstName}
          onChange={e => setFirstName(e.target.value)}
          placeholder="First Name"
          required
        />
        <input
          type="text"
          value={lastName}
          onChange={e => setLastName(e.target.value)}
          placeholder="Last Name"
          required
        />
        <input
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          placeholder="Email"
          required
        />
        <input
          type="password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          placeholder="Password"
          required
        />
        <input
          type="tel"
          value={phone}
          onChange={e => setPhone(e.target.value)}
          placeholder="Phone"
          required
        />
        <button type="submit" style={{ marginTop: '10px' }}>Register</button>
        {error && <p style={{ color: 'red' }}>{error}</p>}
      </form>

      <p style={{ marginTop: '10px', textAlign: 'center' }}>
        Already registered?{' '}
        <Link to="/login" style={{ color: '#4a90e2', textDecoration: 'none' }}>
          Login here
        </Link>
      </p>
    </div>
  );
}

