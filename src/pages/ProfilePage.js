import React, { useState, useEffect } from 'react';

export default function ProfilePage() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [street, setStreet] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [zipCode, setZipCode] = useState('');
  const [cards, setCards] = useState([]);
  const [newCard, setNewCard] = useState({ cardNo: '', type: '', expirationDate: '' });
  const [password, setPassword] = useState('');
  const [promotions, setPromotions] = useState(false);

  const userID = localStorage.getItem('userID');
  const addressID = localStorage.getItem('addressID');

  useEffect(() => {
    // Fetch current profile info
    fetch(`http://localhost:8080/api/user/${userID}`)
      .then(res => res.json())
      .then(data => {
        setFirstName(data.firstName);
        setLastName(data.lastName);
        setEmail(data.email);
        setStreet(data.address?.street || '');
        setCity(data.address?.city || '');
        setState(data.address?.state || '');
        setZipCode(data.address?.zipCode || '');
        setCards(data.cards || []);
        setPromotions(data.enrollForPromotions || false);
      })
      .catch(err => console.error('Failed to load user profile:', err));
  }, [userID]);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      // Update name
      await fetch('http://localhost:8080/api/user/update-name', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, firstName, lastName }),
      });

      // Update billing address
      await fetch('http://localhost:8080/api/user/update-billing', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, addressID, street, city, state, zipCode }),
      });

      // Update password if entered
      if (password.trim()) {
        await fetch('http://localhost:8080/api/user/update-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ userID, password }),
        });
      }

      // Update promotions status
      await fetch('http://localhost:8080/api/user/update-promotions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, enrollForPromotions: promotions }),
      });

      alert('Profile updated successfully.');
    } catch (err) {
      alert('Error updating profile: ' + err.message);
    }
  };

  const handleAddCard = async () => {
    if (cards.length >= 4) return alert('You can only store up to 4 cards.');

    const response = await fetch('http://localhost:8080/api/user/add-payment', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userID,
        cardNo: newCard.cardNo,
        type: newCard.type,
        expirationDate: newCard.expirationDate,
        billingAddressID: addressID,
      }),
    });

    const added = await response.json();
    if (response.ok) {
      setCards(prev => [...prev, newCard]);
      setNewCard({ cardNo: '', type: '', expirationDate: '' });
      alert('Card added successfully.');
    } else {
      alert('Failed to add card: ' + added.message);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h2>Profile</h2>
      <form onSubmit={handleSave} style={formStyle}>
        {/* Name */}
        <Input label="First Name" value={firstName} onChange={setFirstName} />
        <Input label="Last Name" value={lastName} onChange={setLastName} />

        {/* Email (readonly) */}
        <Input label="Email" value={email} disabled />

        {/* Billing Address */}
        <Input label="Street" value={street} onChange={setStreet} />
        <Input label="City" value={city} onChange={setCity} />
        <Input label="State" value={state} onChange={setState} />
        <Input label="Zip Code" value={zipCode} onChange={setZipCode} />

        {/* Password */}
        <Input label="New Password (optional)" type="password" value={password} onChange={setPassword} />

        {/* Promotions */}
        <div style={{ marginBottom: '15px' }}>
          <label>
            <input type="checkbox" checked={promotions} onChange={() => setPromotions(!promotions)} />
            {' '}Enroll in Promotions
          </label>
        </div>

        {/* Payment Cards */}
        <h4>Payment Cards (Max 4)</h4>
        {cards.map((card, i) => (
          <p key={i}>{card.cardType} ending in {card.cardNo.slice(-4)} (Exp: {card.expirationDate})</p>
        ))}
        {cards.length < 4 && (
          <div style={{ marginBottom: '15px' }}>
            <Input label="Card Number" value={newCard.cardNo} onChange={val => setNewCard({ ...newCard, cardNo: val })} />
            <Input label="Card Type" value={newCard.type} onChange={val => setNewCard({ ...newCard, type: val })} />
            <Input label="Expiration Date" value={newCard.expirationDate} onChange={val => setNewCard({ ...newCard, expirationDate: val })} />
            <button type="button" onClick={handleAddCard}>Add Card</button>
          </div>
        )}

        <button type="submit" style={buttonStyle}>Save Profile</button>
      </form>
    </div>
  );
}

const Input = ({ label, value, onChange, type = 'text', disabled = false }) => (
  <div style={{ marginBottom: '15px' }}>
    <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>{label}</label>
    <input
      type={type}
      value={value}
      onChange={onChange ? e => onChange(e.target.value) : undefined}
      disabled={disabled}
      style={{
        width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc',
        background: disabled ? '#eee' : '#fff'
      }}
    />
  </div>
);

const formStyle = {
  background: '#fff',
  padding: '20px',
  borderRadius: '8px',
  boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
};

const buttonStyle = {
  background: '#4a90e2',
  color: 'white',
  border: 'none',
  padding: '10px 15px',
  borderRadius: '5px',
  cursor: 'pointer',
  width: '100%',
  marginTop: '10px'
};

