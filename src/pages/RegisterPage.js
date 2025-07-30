import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

export default function RegisterPage({ setIsLoggedIn }) {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [enrollForPromotions, setEnrollForPromotions] = useState(true);

  const [shipping, setShipping] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: '',
  });

  const [card, setCard] = useState({
    cardNumber: '',
    cardType: '',
    expirationDate: '',
    billing: {
      street: '',
      city: '',
      state: '',
      zipCode: '',
    }
  });

  const [error, setError] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();
    const payload = {
      firstName, lastName, email, password, phone, enrollForPromotions
    };
    if (shipping.street || shipping.city || shipping.state || shipping.zipCode) {
      payload.shippingAddress = shipping;
    }
    if (card.cardNumber || card.cardType || card.expirationDate) {
      if (card.billing.street || card.billing.city || card.billing.state || card.billing.zipCode) {
        payload.paymentCard = {
          cardNumber: card.cardNumber,
          cardType: card.cardType,
          expirationDate: card.expirationDate,
          billingAddress: card.billing
        };
      }
    }

    try {
      const res = await fetch('/api/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (res.ok && data.success) {
        alert('Registration successful! Please check your email to verify your account.');
        navigate('/login');
      } else {
        setError(data.message || 'Registration failed.');
      }
    } catch (err) {
      setError('Register error: ' + err.message);
    }
  };

  const inputStyle = {
    width: '95%',
    padding: '12px',
    marginBottom: '12px',
    borderRadius: '4px',
    border: '1px solid #ccc',
    fontSize: '16px'
  };

  return (
    <div style={{
      maxWidth: '500px',
      margin: '50px auto',
      padding: '30px',
      border: '1px solid #ddd',
      borderRadius: '8px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
      fontFamily: 'Arial, sans-serif',
      backgroundColor: 'white'
    }}>
      <h2 style={{ textAlign: 'center', marginBottom: '25px' }}>Create an Account</h2>

      <form onSubmit={handleRegister}>
        <input style={inputStyle} type="text" value={firstName} onChange={e => setFirstName(e.target.value)} placeholder="First Name" required />
        <input style={inputStyle} type="text" value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Last Name" required />
        <input style={inputStyle} type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required />
        <input style={inputStyle} type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" required />
        <input style={inputStyle} type="tel" value={phone} onChange={e => setPhone(e.target.value)} placeholder="Phone Number" required />

        <div style={{ marginBottom: '16px' }}>
          <label style={{ fontSize: '14px', display: 'block', marginBottom: '6px' }}>Enroll for Promotions</label>
          <label style={{ position: 'relative', display: 'inline-block', width: '50px', height: '24px' }}>
            <input
              type="checkbox"
              checked={enrollForPromotions}
              onChange={(e) => setEnrollForPromotions(e.target.checked)}
              style={{ opacity: 0, width: 0, height: 0 }}
            />
            <span style={{
              position: 'absolute',
              cursor: 'pointer',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              backgroundColor: enrollForPromotions ? '#4a90e2' : '#ccc',
              transition: '0.4s',
              borderRadius: '24px'
            }}></span>
            <span style={{
              position: 'absolute',
              height: '18px',
              width: '18px',
              left: enrollForPromotions ? '26px' : '4px',
              bottom: '3px',
              backgroundColor: 'white',
              transition: '0.4s',
              borderRadius: '50%',
              boxShadow: '0 0 2px rgba(0,0,0,0.2)'
            }}></span>
          </label>
        </div>

        <h4 style={{ marginTop: '25px', marginBottom: '10px' }}>Shipping Address (optional)</h4>
        <input style={inputStyle} type="text" value={shipping.street} onChange={e => setShipping({ ...shipping, street: e.target.value })} placeholder="Street" />
        <input style={inputStyle} type="text" value={shipping.city} onChange={e => setShipping({ ...shipping, city: e.target.value })} placeholder="City" />
        <input style={inputStyle} type="text" value={shipping.state} onChange={e => setShipping({ ...shipping, state: e.target.value })} placeholder="State" />
        <input style={inputStyle} type="text" value={shipping.zipCode} onChange={e => setShipping({ ...shipping, zipCode: e.target.value })} placeholder="Zip Code" />

        <h4 style={{ marginTop: '25px', marginBottom: '10px' }}>Payment Card (optional)</h4>
        <input style={inputStyle} type="text" value={card.cardNumber} onChange={e => setCard({ ...card, cardNumber: e.target.value })} placeholder="Card Number" />
        <input style={inputStyle} type="text" value={card.cardType} onChange={e => setCard({ ...card, cardType: e.target.value })} placeholder="Card Type (e.g. Visa)" />
        <input style={inputStyle} type="text" value={card.expirationDate} onChange={e => setCard({ ...card, expirationDate: e.target.value })} placeholder="Expiration Date (MM/YY)" />

        <h5 style={{ marginTop: '10px', marginBottom: '6px' }}>Billing Address (optional)</h5>
        <input style={inputStyle} type="text" value={card.billing.street} onChange={e => setCard({ ...card, billing: { ...card.billing, street: e.target.value } })} placeholder="Street" />
        <input style={inputStyle} type="text" value={card.billing.city} onChange={e => setCard({ ...card, billing: { ...card.billing, city: e.target.value } })} placeholder="City" />
        <input style={inputStyle} type="text" value={card.billing.state} onChange={e => setCard({ ...card, billing: { ...card.billing, state: e.target.value } })} placeholder="State" />
        <input style={inputStyle} type="text" value={card.billing.zipCode} onChange={e => setCard({ ...card, billing: { ...card.billing, zipCode: e.target.value } })} placeholder="Zip Code" />

        <button type="submit" style={{
          width: '100%',
          padding: '12px',
          backgroundColor: '#0046be',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          fontWeight: 'bold',
          fontSize: '16px',
          cursor: 'pointer',
          marginTop: '20px'
        }}>
          Register
        </button>
        {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}
      </form>

      <p style={{ textAlign: 'center', fontSize: '14px', marginTop: '20px' }}>
        Already have an account?{' '}
        <Link to="/login" style={{ color: '#0046be', fontWeight: 'bold', textDecoration: 'none' }}>
          Login here
        </Link>
      </p>
    </div>
  );
}


