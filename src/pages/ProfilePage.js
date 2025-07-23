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
  const [showAddCardMenu, setShowAddCardMenu] = useState(false);
  // Add new state for billing address fields
  const [billingStreet, setBillingStreet] = useState('');
  const [billingCity, setBillingCity] = useState('');
  const [billingState, setBillingState] = useState('');
  const [billingZip, setBillingZip] = useState('');
  // Add state for billing addresses
  const [billingAddresses, setBillingAddresses] = useState([]);
  // Add state for which card is being edited and the edited billing address
  const [editingCardID, setEditingCardID] = useState(null);
  const [editedCard, setEditedCard] = useState({ cardNo: '', type: '', expirationDate: '' });
  const [editedBilling, setEditedBilling] = useState({ street: '', city: '', state: '', zipCode: '' });
  const [editCardStatus, setEditCardStatus] = useState({ loading: false, error: '', success: '' });

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
            expirationDate: card.expirationDate,
            cardID: card.cardID,
            billingAddressID: card.billingAddressID
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
        // Store billing addresses
        if (data.billingAddresses) {
          setBillingAddresses(data.billingAddresses);
        } else if (data.user && data.user.billingAddresses) {
          setBillingAddresses(data.user.billingAddresses);
        } else {
          setBillingAddresses([]);
        }
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

      // Update or add shipping address
      if (shippingAddressID) {
        // Update existing shipping address
        await fetch('http://localhost:8080/api/user/update-shipping', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ userID, addressID: shippingAddressID, street, city, state, zipCode }),
        });
      } else {
        // Add new shipping address
        const resp = await fetch('http://localhost:8080/api/user/add-shipping', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ userID, street, city, state, zipCode }),
        });
        const result = await resp.json();
        if (resp.ok && result.addressID) {
          setShippingAddressID(result.addressID); // update state with new addressID
        }
      }

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
    // Require billing address fields
    if (!billingStreet || !billingCity || !billingState || !billingZip) {
      alert('Please enter all billing address fields.');
      return;
    }
    const response = await fetch('http://localhost:8080/api/user/add-payment', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userID,
        cardNo: newCard.cardNo,
        type: newCard.type,
        expirationDate: newCard.expirationDate,
        billingAddress: {
          street: billingStreet,
          city: billingCity,
          state: billingState,
          zipCode: billingZip,
        },
      }),
    });

    const added = await response.json();
    if (response.ok && added.message && added.message.toLowerCase().includes('success')) {
      // Refresh cards by refetching profile data
      fetch(`http://localhost:8080/api/user/${userID}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
          if (data.paymentCards) {
            const mappedCards = data.paymentCards.map(card => ({
              cardNo: card.cardNo,
              type: card.cardType,
              expirationDate: card.expirationDate,
              cardID: card.cardID,
              billingAddressID: card.billingAddressID
            }));
            setCards(mappedCards);
          }
          if (data.billingAddresses) {
            setBillingAddresses(data.billingAddresses);
          }
        });
      setNewCard({ cardNo: '', type: '', expirationDate: '' });
      setBillingStreet('');
      setBillingCity('');
      setBillingState('');
      setBillingZip('');
      setShowAddCardMenu(false);
      alert('Card added successfully.');
    } else {
      alert('Failed to add card: ' + (added.message || 'Unknown error'));
    }
  };

  function maskCardNumber(cardNo) {
    if (!cardNo) return '';
    const last4 = cardNo.slice(-4);
    return '************' + last4;
  }

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
        {cards.map((card, idx) => {
          // Find the billing address for this card
          const billing = billingAddresses.find(addr => addr.addressID === card.billingAddressID);
          const isEditing = editingCardID === card.cardID;
          return (
            <div key={idx} style={{ marginBottom: '25px', border: '1px solid #e0e0e0', borderRadius: '10px', background: isEditing ? '#f8faff' : '#fafbfc', boxShadow: isEditing ? '0 2px 8px rgba(74,144,226,0.08)' : 'none', padding: '20px' }}>
              {!isEditing ? (
                <>
                  <div style={{ fontWeight: 'bold', fontSize: '1.1em', marginBottom: 10 }}>Card Details</div>
                  <div style={{ marginBottom: 8 }}><span style={{ fontWeight: 600 }}>Card Number:</span> {maskCardNumber(card.cardNo)}</div>
                  <div style={{ marginBottom: 8 }}><span style={{ fontWeight: 600 }}>Card Type:</span> {card.type}</div>
                  <div style={{ marginBottom: 8 }}><span style={{ fontWeight: 600 }}>Expiration Date:</span> {card.expirationDate}</div>
                  {billing && (
                    <div style={{ marginTop: 18, background: '#f7f7f7', borderRadius: 6, padding: 12 }}>
                      <div style={{ fontWeight: 'bold', marginBottom: 4 }}>Billing Address</div>
                      <div>{billing.street}</div>
                      <div>{billing.city}, {billing.state} {billing.zipCode}</div>
                    </div>
                  )}
                  <button type="button" style={{ ...buttonStyle, width: 'auto', marginTop: 16 }} onClick={() => {
                    setEditingCardID(card.cardID);
                    setEditedCard({
                      cardNo: card.cardNo,
                      type: card.type,
                      expirationDate: card.expirationDate
                    });
                    setEditedBilling({
                      street: billing.street,
                      city: billing.city,
                      state: billing.state,
                      zipCode: billing.zipCode
                    });
                    setEditCardStatus({ loading: false, error: '', success: '' });
                  }}>Edit Card</button>
                </>
              ) : (
                <form onSubmit={async e => {
                  e.preventDefault();
                  setEditCardStatus({ loading: true, error: '', success: '' });
                  // 1. Update billing address
                  const billingResp = await fetch('http://localhost:8080/api/edit/update-billing', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                      addressID: billing.addressID,
                      street: editedBilling.street,
                      city: editedBilling.city,
                      state: editedBilling.state,
                      zipCode: editedBilling.zipCode
                    })
                  });
                  const billingResult = await billingResp.json();
                  if (!billingResp.ok || !billingResult.message || !billingResult.message.toLowerCase().includes('success')) {
                    setEditCardStatus({ loading: false, error: 'Failed to update billing address: ' + (billingResult.message || 'Unknown error'), success: '' });
                    return;
                  }
                  // 2. Update payment card
                  const cardResp = await fetch('http://localhost:8080/api/edit/update-payment', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                      userID,
                      cardID: card.cardID,
                      cardNo: editedCard.cardNo,
                      type: editedCard.type,
                      expirationDate: editedCard.expirationDate,
                      billingAddressID: billing.addressID
                    })
                  });
                  const cardResult = await cardResp.json();
                  if (!cardResp.ok || !cardResult.message || !cardResult.message.toLowerCase().includes('success')) {
                    setEditCardStatus({ loading: false, error: 'Failed to update card: ' + (cardResult.message || 'Unknown error'), success: '' });
                    return;
                  }
                  // Refresh billing addresses and cards
                  fetch(`http://localhost:8080/api/user/${userID}`, { credentials: 'include' })
                    .then(res => res.json())
                    .then(data => {
                      if (data.paymentCards) {
                        const mappedCards = data.paymentCards.map(card => ({
                          cardNo: card.cardNo,
                          type: card.cardType,
                          expirationDate: card.expirationDate,
                          cardID: card.cardID,
                          billingAddressID: card.billingAddressID
                        }));
                        setCards(mappedCards);
                      }
                      if (data.billingAddresses) {
                        setBillingAddresses(data.billingAddresses);
                      }
                    });
                  setEditCardStatus({ loading: false, error: '', success: 'Card and billing address updated successfully.' });
                  setTimeout(() => {
                    setEditingCardID(null);
                    setEditCardStatus({ loading: false, error: '', success: '' });
                  }, 1200);
                }} style={{ margin: 0 }}>
                  <div style={{ fontWeight: 'bold', fontSize: '1.1em', marginBottom: 10 }}>Edit Card Details</div>
                  <Input label="Card Number" value={editedCard.cardNo} onChange={val => setEditedCard(c => ({ ...c, cardNo: val }))} />
                  <Input label="Card Type" value={editedCard.type} onChange={val => setEditedCard(c => ({ ...c, type: val }))} />
                  <Input label="Expiration Date" value={editedCard.expirationDate} onChange={val => setEditedCard(c => ({ ...c, expirationDate: val }))} />
                  <div style={{ fontWeight: 'bold', marginTop: 18, marginBottom: 4 }}>Billing Address</div>
                  <Input label="Street" value={editedBilling.street} onChange={val => setEditedBilling(b => ({ ...b, street: val }))} />
                  <Input label="City" value={editedBilling.city} onChange={val => setEditedBilling(b => ({ ...b, city: val }))} />
                  <Input label="State" value={editedBilling.state} onChange={val => setEditedBilling(b => ({ ...b, state: val }))} />
                  <Input label="Zip Code" value={editedBilling.zipCode} onChange={val => setEditedBilling(b => ({ ...b, zipCode: val }))} />
                  {editCardStatus.error && <div style={{ color: '#e94e77', marginTop: 8, marginBottom: 0, fontWeight: 500 }}>{editCardStatus.error}</div>}
                  {editCardStatus.success && <div style={{ color: '#4a90e2', marginTop: 8, marginBottom: 0, fontWeight: 500 }}>{editCardStatus.success}</div>}
                  <div style={{ display: 'flex', gap: 12, marginTop: 18 }}>
                    <button type="submit" style={{ ...buttonStyle, width: 'auto', minWidth: 90, opacity: editCardStatus.loading ? 0.7 : 1, pointerEvents: editCardStatus.loading ? 'none' : 'auto', position: 'relative' }} disabled={editCardStatus.loading}>
                      {editCardStatus.loading ? <span style={{ display: 'inline-block', width: 18, height: 18, border: '2px solid #fff', borderTop: '2px solid #4a90e2', borderRadius: '50%', animation: 'spin 1s linear infinite', verticalAlign: 'middle' }} /> : 'Save'}
                    </button>
                    <button type="button" style={{ ...buttonStyle, width: 'auto', background: '#aaa', minWidth: 90 }} onClick={() => { setEditingCardID(null); setEditCardStatus({ loading: false, error: '', success: '' }); }} disabled={editCardStatus.loading}>Cancel</button>
                  </div>
                </form>
              )}
              {card.cardID && !isEditing && (
                <button
                  type="button"
                  style={{ ...buttonStyle, background: '#e94e77', width: 'auto', marginTop: '10px' }}
                  onClick={async () => {
                    if (!window.confirm('Are you sure you want to remove this card?')) return;
                    const resp = await fetch('http://localhost:8080/api/user/delete-payment', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify({ cardID: card.cardID })
                    });
                    const result = await resp.json();
                    if (resp.ok && result.message && result.message.toLowerCase().includes('deleted')) {
                      // Refresh cards by refetching profile data
                      fetch(`http://localhost:8080/api/user/${userID}`, { credentials: 'include' })
                        .then(res => res.json())
                        .then(data => {
                          if (data.paymentCards) {
                            const mappedCards = data.paymentCards.map(card => ({
                              cardNo: card.cardNo,
                              type: card.cardType,
                              expirationDate: card.expirationDate,
                              cardID: card.cardID,
                              billingAddressID: card.billingAddressID
                            }));
                            setCards(mappedCards);
                          }
                          if (data.billingAddresses) {
                            setBillingAddresses(data.billingAddresses);
                          }
                        });
                      alert('Card removed successfully.');
                    } else {
                      alert('Failed to remove card: ' + (result.message || 'Unknown error'));
                    }
                  }}
                >
                  Remove Card
                </button>
              )}
            </div>
          );
        })}

        {cards.length < 4 && !showAddCardMenu && (
          <button
            type="button"
            onClick={() => {
              setNewCard({ cardNo: '', type: '', expirationDate: '' });
              setShowAddCardMenu(true);
            }}
          >
            Add Card
          </button>
        )}

        {showAddCardMenu && (
          <div style={{ marginBottom: '15px', border: '1px solid #ccc', borderRadius: '8px', padding: '15px' }}>
            <Input
              label="Card Number"
              value={newCard.cardNo}
              onChange={val => setNewCard({ ...newCard, cardNo: val })}
            />
            <Input
              label="Card Type"
              value={newCard.type}
              onChange={val => setNewCard({ ...newCard, type: val })}
            />
            <Input
              label="Expiration Date"
              value={newCard.expirationDate}
              onChange={val => setNewCard({ ...newCard, expirationDate: val })}
            />
            <div style={{ marginTop: '10px', marginBottom: '10px', fontWeight: 'bold' }}>Billing Address</div>
            <Input
              label="Street"
              value={billingStreet}
              onChange={setBillingStreet}
            />
            <Input
              label="City"
              value={billingCity}
              onChange={setBillingCity}
            />
            <Input
              label="State"
              value={billingState}
              onChange={setBillingState}
            />
            <Input
              label="Zip Code"
              value={billingZip}
              onChange={setBillingZip}
            />
            <button type="button" onClick={handleAddCard}>Add Card</button>
            <button type="button" onClick={() => { setShowAddCardMenu(false); setBillingStreet(''); setBillingCity(''); setBillingState(''); setBillingZip(''); }} style={{ marginLeft: '10px' }}>
              Cancel
            </button>
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

// Add spinner animation
const style = document.createElement('style');
style.innerHTML = `@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`;
document.head.appendChild(style);
