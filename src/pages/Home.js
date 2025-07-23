import React from 'react';
import { FaStar } from 'react-icons/fa';
import { Link, useNavigate } from 'react-router-dom';

export default function Home({
  featuredBooks,
  comingSoonBooks,
  handleAddToCart,
  loading,
  error,
  isSearching,
  searchTerm,
  isLoggedIn
}) {
  const navigate = useNavigate();

  const cardStyle = {
    background: '#fff',
    padding: '15px',
    borderRadius: '10px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    textAlign: 'center',
    transition: 'transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease',
    cursor: 'pointer'
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Loading books... hang tight!</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Oops! {error}</div>;
/*
  const handleCartClick = (book) => {
    if (!isLoggedIn) {
      navigate('/register');
    } else {  
      handleAddToCart(book);
    }
  };
*/
  const handleCartClick = (book) => {
    handleAddToCart(book);
  };
  const renderBooks = (books, color = '#4a90e2') =>
    books.map(book => (
      <div key={book.id} className='book-card' style={cardStyle}>
        <Link to={`/book/${book.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <img
            src={`/img/${book.coverImage.split('/').pop()}`}
            alt={book.title}
            style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
          />
          <h3 style={{ margin: '10px 0', fontSize: '1.2em', height: '2.4em', overflow: 'hidden' }}>{book.title}</h3>
          <p style={{ color: '#666', margin: '5px 0' }}>by {book.author}</p>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', margin: '5px 0' }}>
            <FaStar color="#ffd700" />
            <span>{book.rating ? book.rating.toFixed(1) : 'N/A'}</span>
          </div>
          <p style={{ fontWeight: 'bold', color, margin: '10px 0', fontSize: '1.2em' }}>
            ${book.sellingPrice ? book.sellingPrice.toFixed(2) : 'N/A'}
          </p>
        </Link>

        {color !== '#50c878' && (
          <button
            onClick={() => handleCartClick(book)}
            style={{
              background: '#4a90e2',
              color: '#fff',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '5px',
              cursor: 'pointer',
              width: '100%',
              marginTop: '10px',
              transition: 'background 0.2s'
            }}
          >
            Add to Cart
          </button>
        )}
      </div>
    ));

  return (
    <div>
      <style>
        {`
          .book-card:hover {
            transform: scale(1.05);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.25);
            border-color: #4a90e2;
          }
        `}
      </style>

      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>
        {isSearching ? `🔍 Search Results for "${searchTerm}"` : '📘 Featured Books'}
      </h2>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.length > 0 ? renderBooks(featuredBooks) : <p>No books found for your search term.</p>}
      </div>

      {!isSearching && comingSoonBooks?.length > 0 && (
        <>
          <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
            {renderBooks(comingSoonBooks, '#50c878')}
          </div>
        </>
      )}
    </div>
  );
}
