import React from 'react';
import { Link } from 'react-router-dom';

export default function WelcomePage() {
  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center', padding: '50px' }}>
      <h1>📘 Welcome to the University Bookstore</h1>
      <p style={{ fontSize: '1.2em', marginBottom: '30px' }}>
        Your online checkout system for textbooks and resources.
      </p>
      <div style={{ display: 'flex', justifyContent: 'center', gap: '20px' }}>
        <Link to="/login">
          <button style={buttonStyle}>Login</button>
        </Link>
        <Link to="/register">
          <button style={buttonStyle}>Register</button>
        </Link>
      </div>
    </div>
  );
}

const buttonStyle = {
  background: '#4a90e2',
  color: 'white',
  padding: '10px 20px',
  border: 'none',
  borderRadius: '5px',
  fontSize: '1em',
  cursor: 'pointer'
};
