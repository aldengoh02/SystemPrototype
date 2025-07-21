/*
import React, { useState } from 'react';

export default function ProfilePage() {
  const [name, setName] = useState('your name');
  const [email, setEmail] = useState('your-email@example.com');
  const [address, setAddress] = useState('123 Main St, City');
  const [paymentMethod, setPaymentMethod] = useState('Visa **** 1234');

  const handleSubmit = (e) => {
    e.preventDefault();
    alert('Profile updated successfully!');
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h2>Profile</h2>
      <form onSubmit={handleSubmit} style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Full Name:</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Shipping Address:</label>
          <textarea
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc', minHeight: '80px' }}
          />
        </div>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment Method:</label>
          <input
            type="text"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
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
  */
import React, { useState } from 'react';

export default function ProfilePage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    const userID = localStorage.getItem('userID');
    const addressID = localStorage.getItem('addressID');
    const cardID = localStorage.getItem('cardID');

    if (!userID || !addressID || !cardID) {
      alert('Missing user, address, or card info. Make sure you are logged in and data is loaded.');
      return;
    }

    const [firstName, ...rest] = name.trim().split(' ');
    const lastName = rest.join(' ') || '';

    const [street, cityStateZip] = address.split(',');
    const [city, stateZip] = (cityStateZip || '').trim().split(' ');
    const [state, zipCode] = (stateZip || '').trim().split(/(\d{5})/).filter(Boolean);

    const [cardNo, type, expirationDate] = paymentMethod.split(',').map(x => x.trim());

    try {
      // 1. Update name
      const nameRes = await fetch('http://localhost:8080/api/user/update-name', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, firstName, lastName }),
      });
      const nameJson = await nameRes.json();

      // 2. Update billing address
      const billingRes = await fetch('http://localhost:8080/api/user/update-billing', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, addressID, street, city, state, zipCode }),
      });
      const billingJson = await billingRes.json();

      // 3. Update payment card
      const paymentRes = await fetch('http://localhost:8080/api/user/update-payment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userID, cardID, cardNo, type, expirationDate, billingAddressID: addressID }),
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
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Full Name (First Last):</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="John Doe"
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
            placeholder="john@example.com"
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
            placeholder="123 Main St, Atlanta GA 30303"
            required
            style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          />
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment (CardNo, Type, ExpirationDate):</label>
          <input
            type="text"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            placeholder="4111111111111111, Visa, 12/2025"
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

