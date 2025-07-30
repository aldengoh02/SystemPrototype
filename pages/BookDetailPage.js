import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

export default function BookDetailPage({ handleAddToCart }) {
  const { id } = useParams();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`http://localhost:8080/api/books/${id}`)
      .then(res => res.json())
      .then(data => {
        setBook(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [id]);

  if (loading) return <p>Loading book...</p>;
  if (!book) return <p>Book not found</p>;

  const isComingSoon = book.releaseDate && new Date(book.releaseDate) > new Date();

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', background: '#fff', padding: '20px', borderRadius: '10px' }}>
      <img
        src={`/img/${book.coverImage?.split('/').pop()}`}
        alt={book.title}
        style={{ width: '100%', height: '400px', objectFit: 'cover', borderRadius: '10px' }}
      />
      <h2 style={{ marginTop: '20px' }}>{book.title}</h2>
      <p><strong>Author:</strong> {book.author}</p>
      <p><strong>Price:</strong> ${book.sellingPrice?.toFixed(2)}</p>
      <p><strong>Rating:</strong> {book.rating}</p>
      <p style={{ marginTop: '15px' }}><strong>Description:</strong><br />{book.description}</p>

      {book.featured && !isComingSoon && (
      <button
        onClick={() => handleAddToCart(book)}
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
