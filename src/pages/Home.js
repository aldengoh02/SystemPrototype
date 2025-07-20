/*
import React from 'react';
import { FaStar } from 'react-icons/fa';
import { Link } from 'react-router-dom'; 

export default function Home({ featuredBooks, comingSoonBooks, handleAddToCart, loading, error, isSearching, searchTerm }) {
  const cardStyle = {
    background: '#fff',
    padding: '15px',
    borderRadius: '10px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    textAlign: 'center',
    transition: 'transform 0.2s',
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Loading books... hang tight!</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Oops! {error}</div>;

  return (
    <div>
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>
        {isSearching ? `🔍 Search Results for "${searchTerm}"` : '📘 Featured Books'}
      </h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.length > 0 ? featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
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
            <p style={{ fontWeight: 'bold', color: '#4a90e2', margin: '10px 0', fontSize: '1.2em' }}>
              ${book.sellingPrice ? book.sellingPrice.toFixed(2) : 'N/A'}
            </p>
            <button
              onClick={() => handleAddToCart(book)}
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
          </div>
        )) : (
          <p>No books found for your search term.</p>
        )}
      </div>

      {!isSearching && comingSoonBooks && comingSoonBooks.length > 0 && (
        <>
          <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
            {comingSoonBooks.map(book => (
              <div key={book.id} style={cardStyle}>
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
                <p style={{ fontWeight: 'bold', color: '#50c878', margin: '10px 0', fontSize: '1.2em' }}>
                  ${book.sellingPrice ? book.sellingPrice.toFixed(2) : 'N/A'}
                </p>
                <p style={{ color: '#50c878', fontWeight: 'bold', marginTop: '10px' }}><i>Coming Soon</i></p>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
  */

import React from 'react';
import { FaStar } from 'react-icons/fa';
import { Link } from 'react-router-dom';

export default function Home({ featuredBooks, comingSoonBooks, handleAddToCart, loading, error, isSearching, searchTerm }) {
  const cardStyle = {
    background: '#fff',
    padding: '15px',
    borderRadius: '10px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    textAlign: 'center',
    transition: 'transform 0.2s',
    cursor: 'pointer'
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Loading books... hang tight!</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Oops! {error}</div>;

  return (
    <div>
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>
        {isSearching ? `🔍 Search Results for "${searchTerm}"` : '📘 Featured Books'}
      </h2>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.length > 0 ? featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
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
              <p style={{ fontWeight: 'bold', color: '#4a90e2', margin: '10px 0', fontSize: '1.2em' }}>
                ${book.sellingPrice ? book.sellingPrice.toFixed(2) : 'N/A'}
              </p>
            </Link>

            <button
              onClick={() => handleAddToCart(book)}
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
          </div>
        )) : (
          <p>No books found for your search term.</p>
        )}
      </div>

      {!isSearching && comingSoonBooks && comingSoonBooks.length > 0 && (
        <>
          <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
            {comingSoonBooks.map(book => (
              <div key={book.id} style={cardStyle}>
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
                  <p style={{ fontWeight: 'bold', color: '#50c878', margin: '10px 0', fontSize: '1.2em' }}>
                    ${book.sellingPrice ? book.sellingPrice.toFixed(2) : 'N/A'}
                  </p>
                  <p style={{ color: '#50c878', fontWeight: 'bold', marginTop: '10px' }}><i>Coming Soon</i></p>
                </Link>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
