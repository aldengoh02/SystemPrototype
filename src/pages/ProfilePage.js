import React, { useState, useEffect } from 'react';

export default function ProfilePage() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('');

  useEffect(() => {
    async function fetchProfile() {
      try {
        const res = await fetch('http://localhost:8080/api/edit/info', {
          credentials: 'include',
        });
        const data = await res.json();
        if (res.ok && data.user) {
          setFirstName(data.user.firstName || '');
          setLastName(data.user.lastName || '');
          setEmail(data.user.email || '');
          // Use first billing address if available
          if (data.billingAddresses && data.billingAddresses.length > 0) {
            const addr = data.billingAddresses[0];
            setAddress(`${addr.street}, ${addr.city} ${addr.state} ${addr.zipCode}`);
            localStorage.setItem('addressID', addr.addressID);
          }
          // Use first payment card if available
          if (data.paymentCards && data.paymentCards.length > 0) {
            const card = data.paymentCards[0];
            // Show type, last4, and expiration
            setPaymentMethod(`${card.type} ending in ${card.last4}, ${card.expirationDate}`);
            localStorage.setItem('cardID', card.cardID);
          }
          // Store userID for update requests
          if (data.user && data.user.userID) {
            localStorage.setItem('userID', data.user.userID);
          }
        }
      } catch (err) {
        // Optionally handle error
      }
    }
    fetchProfile();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const userID = localStorage.getItem('userID');
    const addressID = localStorage.getItem('addressID');
    const cardID = localStorage.getItem('cardID');

    if (!userID || !addressID || !cardID) {
      alert('Missing user, address, or card info. Make sure you are logged in and data is loaded.');
      return;
    }

    // Split address
    const [street, cityStateZip] = address.split(',');
    const [city, stateZip] = (cityStateZip || '').trim().split(' ');
    const [state, zipCode] = (stateZip || '').trim().split(/(\d{5})/).filter(Boolean);
    const [type, expirationDate] = paymentMethod.split(',').map(x => x.trim());

    try {
      // 1. Update name
      const nameRes = await fetch('http://localhost:8080/api/edit/update-name', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ userID, firstName, lastName }),
      });
      const nameJson = await nameRes.json();

      // 2. Update billing address
      const billingRes = await fetch('http://localhost:8080/api/edit/update-billing', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ userID, addressID, street, city, state, zipCode }),
      });
      const billingJson = await billingRes.json();

      // 3. Update payment card
      const paymentRes = await fetch('http://localhost:8080/api/edit/update-payment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ userID, cardID, cardNo: '', type, expirationDate, billingAddressID: addressID }),
      });
      const paymentJson = await paymentRes.json();

      alert([
        nameJson.message,
        billingJson.message,
        paymentJson.message
      ].join('\n'));

    } catch (err) {
      alert('Error updating profile: ' + err.message);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h2>Profile</h2>
      <form onSubmit={handleSubmit} style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>First Name:</label>
          <input
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Last Name:</label>
          <input
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email (Not editable):</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc', background: '#f4f4f4' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Billing Address (Street, City ST 12345):</label>
          <input
            type="text"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment (Type, ExpirationDate):</label>
          <input
            type="text"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            required
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <button type="submit" style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '10px 15px', borderRadius: '5px', cursor: 'pointer', width: '100%' }}>
          Save Profile
        </button>
      </form>
    </div>
  );
}

