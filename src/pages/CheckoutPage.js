import React, { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import CartService from '../CartService';

export default function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [checkoutData, setCheckoutData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { auth } = useAuth();

  useEffect(() => {
    const calculateTotal = async () => {
      if (cartItems.length === 0) return;

      setLoading(true);
      try {
        const cartItemsFormatted = cartItems.map(item => ({ id: item.id, quantity: item.quantity }));
        const response = await fetch('http://localhost:8080/api/books/calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(cartItemsFormatted)
        });

        if (!response.ok) throw new Error('Failed to calculate checkout total');
        const data = await response.json();
        setCheckoutData(data);
      } catch (err) {
        setError(err.message);
        console.error('Error calculating checkout:', err);
      } finally {
        setLoading(false);
      }
    };

    calculateTotal();
  }, [cartItems]);

  const handleConfirm = async () => {
    try {
      setOrders(prev => [...prev, {
        id: Date.now(),
        items: cartItems,
        total: checkoutData.total,
        date: new Date().toLocaleDateString()
      }]);

      // Clear cart based on authentication status
      if (auth.isLoggedIn) {
        await CartService.clearAuthenticatedCart();
      } else {
        CartService.clearGuestCart();
      }
      
      setCartItems([]);
      window.location.href = '/order-history';
    } catch (error) {
      console.error('Error clearing cart after checkout:', error);
      // Still proceed with checkout even if cart clear fails
      setCartItems([]);
      window.location.href = '/order-history';
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Calculating total...</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;
  if (cartItems.length === 0) return <div style={{ textAlign: 'center', padding: '20px' }}>Your cart is empty</div>;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <h2>Checkout</h2>

      <div style={{ marginBottom: '20px' }}>
        <h3>Order Summary</h3>
        {cartItems.map(item => (
          <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>{item.title} x {item.quantity}</span>
            <span>${(item.sellingPrice * item.quantity).toFixed(2)}</span>
          </div>
        ))}
      </div>

      {checkoutData && (
        <div style={{ borderTop: '1px solid #ccc', paddingTop: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>Subtotal:</span>
            <span>${checkoutData.subtotal.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>Sales Tax:</span>
            <span>${checkoutData.salesTax.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0', fontWeight: 'bold' }}>
            <span>Total:</span>
            <span>${checkoutData.total.toFixed(2)}</span>
          </div>

          <button
            onClick={handleConfirm}
            style={{
              background: '#4CAF50',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              width: '100%',
              marginTop: '20px'
            }}
          >
            Confirm Order
          </button>
        </div>
      )}
    </div>
  );
}