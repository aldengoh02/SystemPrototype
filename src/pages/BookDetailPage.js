import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

export default function BookDetailPage({ handleAddToCart }) {
  const { id } = useParams();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cartMessage, setCartMessage] = useState('');

  useEffect(() => {
    fetch(`http://localhost:8080/api/books/${id}`)
      .then(res => res.json())
      .then(data => {
        setBook(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [id]);

  const handleAddClick = () => {
    handleAddToCart(book);
    setCartMessage(`${book.title} has been added to your cart:)`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    setTimeout(() => setCartMessage(''), 3000);
  };


  if (loading) return <p>Loading book...</p>;
  if (!book) return <p>Book not found</p>;

  const isComingSoon = book.releaseDate && new Date(book.releaseDate) > new Date();

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', background: '#fff', padding: '20px', borderRadius: '10px' }}>
      {cartMessage && (
        <div style={{
          backgroundColor: '#e6ffe6',
          color: '#2e7d32',
          padding: '10px',
          borderRadius: '5px',
          marginBottom: '15px',
          textAlign: 'center',
          fontWeight: 'bold'
        }}>
          {cartMessage}
        </div>
      )}
      <img
        src={`/img/${book.coverImage?.split('/').pop()}`}
        alt={book.title}
        style={{ width: '100%', height: '400px', objectFit: 'cover', borderRadius: '10px' }}
      />
      <h2 style={{ marginTop: '20px' }}>{book.title}</h2>
      <p><strong>Author:</strong> {book.author}</p>
      <p><strong>ISBN:</strong> {book.isbn}</p>
      <p><strong>Edition:</strong> {book.edition}</p>
      <p><strong>Publisher:</strong> {book.publisher}</p>
      <p><strong>Publication Year:</strong> {book.publicationYear}</p>
      <p><strong>Price:</strong> ${book.sellingPrice?.toFixed(2)}</p>
      <p><strong>Rating:</strong> {book.rating}</p>
      <p style={{ marginTop: '15px' }}><strong>Description:</strong><br />{book.description}</p>

      {book.featured && !isComingSoon && (
      <button
        onClick={handleAddClick}
        style={{
          background: '#4a90e2',
          color: '#fff',
          border: 'none',
          padding: '10px 20px',
          borderRadius: '5px',
          cursor: 'pointer',
          marginTop: '20px',
          fontSize: '1em',
          width: '100%'
        }}
      >
        Add to Cart
      </button>
      )}
    </div>
  );
}
