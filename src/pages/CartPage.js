import React, { useState } from 'react';
import { Link } from 'react-router-dom';

export default function CartPage({ cartItems, handleQuantityChange, isLoggedIn }) {
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);
  const total = cartItems.reduce((sum, item) => sum + (item.sellingPrice * item.quantity), 0);

  const handleProceedClick = (e) => {
    if (!isLoggedIn) {
      e.preventDefault();
      setShowLoginPrompt(true);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>🛒 Your Cart</h2>
      {cartItems.length === 0 ? (
        <p>Your cart is empty. Maybe add some books?</p>
      ) : (
        <>
          {cartItems.map(item => (
            <div key={item.id} style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              background: '#fff',
              padding: '15px',
              borderRadius: '8px',
              margin: '10px 0',
              boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
            }}>
              <div style={{ flex: 2 }}>
                <h3 style={{ margin: 0 }}>{item.title}</h3>
                <p style={{ margin: '5px 0', color: '#666' }}>by {item.author}</p>
              </div>
              <div style={{ flex: 1, textAlign: 'right' }}>
                <p style={{ margin: 0 }}>${item.sellingPrice.toFixed(2)} × {item.quantity}</p>
                <p style={{ margin: '5px 0', fontWeight: 'bold' }}>${(item.sellingPrice * item.quantity).toFixed(2)}</p>
              </div>
              <div style={{ flex: 0 }}>
                <button onClick={() => handleQuantityChange(item.id, -1)} style={{ background: '#ff4444', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', marginRight: '5px' }}>-</button>
                <button onClick={() => handleQuantityChange(item.id, 1)} style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}>+</button>
              </div>
            </div>
          ))}

          <div style={{ background: '#fff', padding: '15px', borderRadius: '8px', marginTop: '20px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
            <h3 style={{ textAlign: 'right' }}>Total: ${total.toFixed(2)}</h3>
            <div style={{ textAlign: 'right' }}>
              <Link to={isLoggedIn ? "/checkout" : "#"} onClick={handleProceedClick}>
                <button style={{ background: '#50c878', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '5px', cursor: 'pointer', fontSize: '1.1em' }}>
                  Proceed to Checkout
                </button>
              </Link>
            </div>
            {showLoginPrompt && !isLoggedIn && (
              <div style={{ marginTop: '15px', color: '#d9534f', textAlign: 'right' }}>
                <span>You need to <Link to="/login" style={{ color: '#4a90e2', textDecoration: 'underline' }}>log in</Link> to complete your purchase.</span>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}