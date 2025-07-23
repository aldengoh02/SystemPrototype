import React from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function AdminPage() {
  const { auth } = useAuth();
  if (auth.userRole !== 'admin') {
    return <Navigate to="/home" />;
  }
  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Admin Panel</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginTop: '20px' }}>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)', textAlign: 'center' }}>
          <h3>Manage Books</h3>
          <p>Add, edit, or remove books from the store</p>
          <Link to="/admin/books">
            <button style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer', marginTop: '10px' }}>
              Go to Books
            </button>
          </Link>
        </div>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)', textAlign: 'center' }}>
          <h3>Manage Users</h3>
          <p>View and manage customer accounts</p>
          <Link to="/admin/users">
            <button style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer', marginTop: '10px' }}>
              Go to Users
            </button>
          </Link>
        </div>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)', textAlign: 'center' }}>
          <h3>Manage Orders</h3>
          <p>View and process customer orders</p>
          <Link to="/admin/orders">
            <button style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer', marginTop: '10px' }}>
              Go to Orders
            </button>
          </Link>
        </div>
        <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 5px rgba(0,0,0,0.1)', textAlign: 'center' }}>
          <h3>Reports</h3>
          <p>View sales and inventory reports</p>
          <Link to="/admin/reports">
            <button style={{ background: '#4a90e2', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '5px', cursor: 'pointer', marginTop: '10px' }}>
              View Reports
            </button>
          </Link>
        </div>
      </div>
    </div>
  );
}
