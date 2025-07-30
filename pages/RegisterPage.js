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
      firstName,
      lastName,
      email,
      password,
      phone,
      enrollForPromotions
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

  return (
    <div style={{ background: '#f2f2f2', minHeight: '100vh', paddingTop: '40px' }}>
      <div style={{
        maxWidth: '500px',
        margin: '0 auto',
        padding: '30px',
        borderRadius: '8px',
        backgroundColor: 'white',
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
      }}>
        <h2 style={{ textAlign: 'center', marginBottom: '25px' }}>Create an Account</h2>
        <form onSubmit={handleRegister}>
          <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)} placeholder="First Name" required />
          <input type="text" value={lastName} onChange={e => setLastName(e.target.value)} placeholder="Last Name" required />
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required />
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" required />
          <input type="tel" value={phone} onChange={e => setPhone(e.target.value)} placeholder="Phone" required />

          <div style={{ display: 'flex', alignItems: 'center', marginTop: '10px' }}>
            <label style={{ marginRight: '10px' }}>Enroll for Promotions:</label>
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

          <h4 style={{ marginTop: '20px' }}>Shipping Address (optional)</h4>
          <input type="text" value={shipping.street} onChange={e => setShipping({ ...shipping, street: e.target.value })} placeholder="Street" />
          <input type="text" value={shipping.city} onChange={e => setShipping({ ...shipping, city: e.target.value })} placeholder="City" />
          <input type="text" value={shipping.state} onChange={e => setShipping({ ...shipping, state: e.target.value })} placeholder="State" />
          <input type="text" value={shipping.zipCode} onChange={e => setShipping({ ...shipping, zipCode: e.target.value })} placeholder="Zip Code" />

          <h4 style={{ marginTop: '20px' }}>Payment Card (optional)</h4>
          <input type="text" value={card.cardNumber} onChange={e => setCard({ ...card, cardNumber: e.target.value })} placeholder="Card Number" />
          <input type="text" value={card.cardType} onChange={e => setCard({ ...card, cardType: e.target.value })} placeholder="Card Type (e.g. Visa)" />
          <input type="text" value={card.expirationDate} onChange={e => setCard({ ...card, expirationDate: e.target.value })} placeholder="Expiration Date (MM/YY)" />

          <h5 style={{ marginTop: '10px' }}>Billing Address (optional)</h5>
          <input type="text" value={card.billing.street} onChange={e => setCard({ ...card, billing: { ...card.billing, street: e.target.value } })} placeholder="Street" />
          <input type="text" value={card.billing.city} onChange={e => setCard({ ...card, billing: { ...card.billing, city: e.target.value } })} placeholder="City" />
          <input type="text" value={card.billing.state} onChange={e => setCard({ ...card, billing: { ...card.billing, state: e.target.value } })} placeholder="State" />
          <input type="text" value={card.billing.zipCode} onChange={e => setCard({ ...card, billing: { ...card.billing, zipCode: e.target.value } })} placeholder="Zip Code" />

          <button type="submit" style={{
            width: '100%',
            padding: '12px',
            backgroundColor: '#0046be',
            color: 'white',
            border: 'none',
            borderRadius: '6px',
            fontWeight: 'bold',
            fontSize: '16px',
            cursor: 'pointer',
            marginTop: '20px'
          }}>Register</button>
          {error && <p style={{ color: 'red' }}>{error}</p>}
        </form>

        <p style={{ marginTop: '15px', textAlign: 'center', fontSize: '14px' }}>
          Already registered?{' '}
          <Link to="/login" style={{ color: '#0046be', fontWeight: 'bold', textDecoration: 'none' }}>
            Login here
          </Link>
        </p>
      </div>
    </div>
  );
}


