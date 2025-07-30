import React, { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';

export default function OrderHistoryPage({ orders, setCartItems }) {
  const [dbOrders, setDbOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { auth } = useAuth();

  useEffect(() => {
    const fetchOrderHistory = async () => {
      if (!auth.isLoggedIn) {
        setLoading(false);
        return;
      }

      try {
        const response = await fetch('http://localhost:8080/api/orders', {
          method: 'GET',
          credentials: 'include'
        });

        if (response.ok) {
          const ordersData = await response.json();
          setDbOrders(ordersData);
        } else {
          setError('Failed to fetch order history');
        }
      } catch (err) {
        console.error('Error fetching order history:', err);
        setError('Error fetching order history');
      } finally {
        setLoading(false);
      }
    };

    fetchOrderHistory();
  }, [auth.isLoggedIn]);



  // Use database orders for authenticated users, local state for guests
  const ordersToDisplay = auth.isLoggedIn ? dbOrders : orders;

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>Loading order history...</div>;
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Order History</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {ordersToDisplay.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        ordersToDisplay.map(order => (
          <div key={order.id} style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
              <div>
                <h3 style={{ margin: 0 }}>Order #{order.id}</h3>
                <p style={{ margin: '5px 0', color: '#666' }}>
                  {auth.isLoggedIn 
                    ? new Date(order.date).toLocaleDateString()
                    : new Date(order.date).toLocaleDateString()
                  } • {order.status || 'Processed'}
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.2em' }}>${order.total.toFixed(2)}</p>
              </div>
            </div>
            {order.items.map(item => (
              <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f5f5f5' }}>
                <span>{item.title} × {item.quantity}</span>
                <span>${((item.price || item.sellingPrice) * item.quantity).toFixed(2)}</span>
              </div>
            ))}

          </div>
        ))
      )}
    </div>
  );
}
