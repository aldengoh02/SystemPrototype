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
  const [showPasswordFields, setShowPasswordFields] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [promotions, setPromotions] = useState(false);
  const [shippingAddressID, setShippingAddressID] = useState(null);

  const userID = localStorage.getItem('userID');
  const addressID = localStorage.getItem('addressID');

  useEffect(() => {
    // Fetch current profile info
    fetch(`http://localhost:8080/api/user/${userID}`, { credentials: 'include' })
      .then(res => res.json())
      .then(data => {
        // Support both flat and nested user object
        const userObj = data.user || data;
        setFirstName(userObj.firstName || '');
        setLastName(userObj.lastName || '');
        setEmail(userObj.email || '');
        // Autofill shipping address if present
        if (userObj.shippingAddress) {
          setStreet(userObj.shippingAddress.street || '');
          setCity(userObj.shippingAddress.city || '');
          setState(userObj.shippingAddress.state || '');
          setZipCode(userObj.shippingAddress.zipCode || '');
          setShippingAddressID(userObj.shippingAddress.addressID);
        } else {
          setStreet('');
          setCity('');
          setState('');
          setZipCode('');
          setShippingAddressID(null);
        }
        // Use paymentCards from backend if present
        if (data.paymentCards) {
          const mappedCards = data.paymentCards.map(card => ({
            cardNo: card.cardNo,
            type: card.cardType,
            expirationDate: card.expirationDate
          }));
          setCards(mappedCards);
          // Pre-fill the first card's details in the form fields
          if (mappedCards.length > 0) {
            setNewCard({
              cardNo: mappedCards[0].cardNo,
              type: mappedCards[0].type,
              expirationDate: mappedCards[0].expirationDate
            });
          }
        } else if (userObj.cards) {
          setCards(userObj.cards);
          // Pre-fill the first card's details in the form fields
          if (userObj.cards.length > 0) {
            setNewCard({
              cardNo: userObj.cards[0].cardNo,
              type: userObj.cards[0].type,
              expirationDate: userObj.cards[0].expirationDate
            });
          }
        } else {
          setCards([]);
        }
        setPromotions(userObj.enrollForPromotions || false);
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
        credentials: 'include',
        body: JSON.stringify({ userID, firstName, lastName }),
      });

      // Update shipping address
      await fetch('http://localhost:8080/api/user/update-shipping', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ userID, addressID: shippingAddressID, street, city, state, zipCode }),
      });

      // Update password if fields are shown and new password is entered
      if (showPasswordFields) {
        if (!newPassword || !newPassword.trim()) {
          alert('New password cannot be empty.');
          return;
        }
        const resp = await fetch('http://localhost:8080/api/user/update-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ userID, currentPassword, newPassword }),
        });
        const result = await resp.json();
        if (!resp.ok || (result && result.message && !result.message.toLowerCase().includes('success'))) {
          alert(result && result.message ? result.message : 'Current password is incorrect. Password not changed.');
          return;
        }
      }

      // Update promotions status
      await fetch('http://localhost:8080/api/user/update-promotions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, enrollForPromotions: promotions }),
      });

      alert('Profile updated successfully.');
      setShowPasswordFields(false);
      setCurrentPassword('');
      setNewPassword('');
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

        {/* Shipping Address label */}
        <div style={{ margin: '15px 0 5px 0', fontWeight: 'bold' }}>Shipping Address</div>

        {/* Billing Address */}
        <Input label="Street" value={street} onChange={setStreet} />
        <Input label="City" value={city} onChange={setCity} />
        <Input label="State" value={state} onChange={setState} />
        <Input label="Zip Code" value={zipCode} onChange={setZipCode} />

        {/* Password */}
        {!showPasswordFields ? (
          <button
            type="button"
            style={{ ...buttonStyle, width: 'auto', marginBottom: '15px' }}
            onClick={() => setShowPasswordFields(true)}
          >
            Change Password
          </button>
        ) : (
          <>
            <Input label="Current Password" type="password" value={currentPassword} onChange={setCurrentPassword} />
            <Input label="New Password" type="password" value={newPassword} onChange={setNewPassword} />
          </>
        )}

        {/* Promotions */}
        <div style={{ marginBottom: '15px' }}>
          <button
            type="button"
            style={{
              ...buttonStyle,
              background: promotions ? '#e94e77' : '#4a90e2',
              width: 'auto',
              marginBottom: 0
            }}
            onClick={() => setPromotions(!promotions)}
          >
            {promotions ? 'Unenroll from Promotions' : 'Enroll in Promotions'}
          </button>
        </div>

        {/* Payment Cards */}
        <h4>Payment Cards (Max 4)</h4>
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

