import React, { useState } from 'react';
import { Link } from 'react-router-dom';

export default function CartPage({ cartItems, handleQuantityChange, isLoggedIn }) {
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);

  // Safe calculation of total
  const total = cartItems.reduce(
    (sum, item) =>
      sum +
      (Number(item.sellingPrice ?? 0) * Number(item.quantity ?? 0)),
    0
  );

  const handleProceedClick = (e) => {
    if (!isLoggedIn) {
      e.preventDefault();
      setShowLoginPrompt(true);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '40px auto', fontFamily: 'Arial, sans-serif' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>🛒 Your Cart</h2>

      {cartItems.length === 0 ? (
        <p style={{ textAlign: 'center', fontSize: '18px' }}>Your cart is empty. Maybe add some books?</p>
      ) : (
        <>
          {cartItems.map(item => (
            <div key={item.id} style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              background: '#fff',
              padding: '20px',
              borderRadius: '10px',
              margin: '15px 0',
              boxShadow: '0 2px 6px rgba(0,0,0,0.08)'
            }}>
              <div style={{ flex: 2 }}>
                <h3 style={{ margin: '0 0 5px' }}>{item.title}</h3>
                <p style={{ margin: 0, color: '#777' }}>by {item.author}</p>
              </div>

              <div style={{ flex: 1, textAlign: 'right' }}>
                <p style={{ margin: 0 }}>
                  ${Number(item.sellingPrice ?? 0).toFixed(2)} × {Number(item.quantity ?? 0)}
                </p>
                <p style={{ margin: '5px 0', fontWeight: 'bold' }}>
                  ${(Number(item.sellingPrice ?? 0) * Number(item.quantity ?? 0)).toFixed(2)}
                </p>
              </div>

              <div style={{ flexShrink: 0, display: 'flex', gap: '6px', marginLeft: '20px' }}>
                <button
                  onClick={() => handleQuantityChange(item.id, -1)}
                  style={{
                    background: '#f44336',
                    color: '#fff',
                    border: 'none',
                    width: '32px',
                    height: '32px',
                    borderRadius: '6px',
                    fontSize: '18px',
                    lineHeight: '1',
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                  }}
                >−</button>
                <button
                  onClick={() => handleQuantityChange(item.id, 1)}
                  style={{
                    background: '#4a90e2',
                    color: '#fff',
                    border: 'none',
                    width: '32px',
                    height: '32px',
                    borderRadius: '6px',
                    fontSize: '18px',
                    lineHeight: '1',
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                  }}
                >+</button>
              </div>
            </div>
          ))}

          <div style={{ background: '#fff', padding: '15px', borderRadius: '8px', marginTop: '20px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
            <h3 style={{ textAlign: 'right' }}>
              Total: ${Number(total ?? 0).toFixed(2)}
            </h3>
            <div style={{ textAlign: 'right' }}>
              <Link to={isLoggedIn ? "/checkout" : "#"} onClick={handleProceedClick}>
                <button style={{
                  background: '#50c878',
                  color: 'white',
                  border: 'none',
                  padding: '12px 24px',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontSize: '16px',
                  fontWeight: 'bold',
                  transition: 'background 0.3s ease'
                }}>
                  Proceed to Checkout
                </button>
              </Link>
            </div>

            {showLoginPrompt && !isLoggedIn && (
              <div style={{ marginTop: '15px', color: '#d9534f', textAlign: 'right', fontSize: '14px' }}>
                <span>
                  You need to{' '}
                  <Link to="/login" style={{ color: '#4a90e2', textDecoration: 'underline' }}>log in</Link>{' '}
                  to complete your purchase.
                </span>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
