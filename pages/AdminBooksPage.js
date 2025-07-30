import React, { useState, useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function AdminBooksPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchBooks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchBooks = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/books');
      if (!response.ok) throw new Error('Failed to fetch books');
      const data = await response.json();
      setBooks(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching books:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddBookClick = () => {
    navigate('/admin/books/add');
  };

  // Move the redirect inside the render, after all hooks
  if (auth.userRole !== 'admin') {
    return <Navigate to="/home" />;
  }

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '50px' }}>Loading books...</div>;
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Manage Books</h2>
        <button
          onClick={handleAddBookClick}
          style={{
            background: '#4a90e2',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}
        >
          Add Book
        </button>
      </div>

      {error && (
        <div style={{
          background: '#f8d7da',
          color: '#721c24',
          padding: '10px',
          borderRadius: '5px',
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          Error: {error}
        </div>
      )}

      <div style={{ background: '#fff', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
        <div style={{ padding: '20px', borderBottom: '1px solid #ddd' }}>
          <h3>Books ({books.length})</h3>
        </div>
        
        {books.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            No books found. Add your first book!
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f8f9fa' }}>
                  <th style={tableHeaderStyle}>ID</th>
                  <th style={tableHeaderStyle}>Title</th>
                  <th style={tableHeaderStyle}>Author</th>
                  <th style={tableHeaderStyle}>Category</th>
                  <th style={tableHeaderStyle}>Price</th>
                  <th style={tableHeaderStyle}>Stock</th>
                  <th style={tableHeaderStyle}>Rating</th>
                  <th style={tableHeaderStyle}>Featured</th>
                </tr>
              </thead>
              <tbody>
                {books.map((book) => (
                  <tr key={book.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={tableCellStyle}>{book.id}</td>
                    <td style={tableCellStyle}>{book.title}</td>
                    <td style={tableCellStyle}>{book.author}</td>
                    <td style={tableCellStyle}>{book.category}</td>
                    <td style={tableCellStyle}>${book.sellingPrice}</td>
                    <td style={tableCellStyle}>{book.quantityInStock}</td>
                    <td style={tableCellStyle}>{book.rating}/5</td>
                    <td style={tableCellStyle}>{book.featured ? 'Yes' : 'No'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

const tableHeaderStyle = {
  padding: '12px',
  textAlign: 'left',
  fontWeight: 'bold',
  borderBottom: '2px solid #ddd'
};

const tableCellStyle = {
  padding: '12px',
  borderBottom: '1px solid #eee'
};