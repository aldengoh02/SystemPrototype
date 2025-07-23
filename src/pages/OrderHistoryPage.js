import React from 'react';

export default function OrderHistoryPage({ orders, setCartItems }) {
  const handleReorder = (items) => {
    setCartItems(items);
    alert('We added those items back to your cart!');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Order History</h2>
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        orders.map(order => (
          <div key={order.id} style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', borderBottom: '1px solid #eee', paddingBottom: '10px' }}>
              <div>
                <h3 style={{ margin: 0 }}>Order #{order.id}</h3>
                <p style={{ margin: '5px 0', color: '#666' }}>{new Date(order.date).toLocaleDateString()} • {order.status}</p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.2em' }}>${order.total.toFixed(2)}</p>
              </div>
            </div>
            {order.items.map(item => (
              <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f5f5f5' }}>
                <span>{item.title} × {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            <div style={{ textAlign: 'right', marginTop: '15px' }}>
              <button onClick={() => handleReorder(order.items)} style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer' }}>
                Reorder
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
