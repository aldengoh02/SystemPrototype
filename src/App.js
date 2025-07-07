import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch, FaStar } from 'react-icons/fa';
import { useState, useEffect } from 'react';

export default function App() {
  const [search, setSearch] = useState('');
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [featuredBooks, setFeaturedBooks] = useState([]);
  const [comingSoonBooks, setComingSoonBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        setLoading(true);
        
        const [featuredResponse, comingSoonResponse] = await Promise.all([
          fetch('http://localhost:5000/api/books/featured'),
          fetch('http://localhost:5000/api/books/coming-soon')
        ]);

        if (!featuredResponse.ok || !comingSoonResponse.ok) {
          throw new Error('Failed to fetch books');
        }

        const [featuredData, comingSoonData] = await Promise.all([
          featuredResponse.json(),
          comingSoonResponse.json()
        ]);

        setFeaturedBooks(featuredData);
        setComingSoonBooks(comingSoonData);
      } catch (err) {
        setError(err.message);
        console.error('Error fetching books:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchBooks();
  }, []);

  const handleAddToCart = (book) => {
    setCartItems(prev => {
      const existing = prev.find(item => item.id === book.id);
      if (existing) {
        return prev.map(item => 
          item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      } else {
        return [...prev, { ...book, quantity: 1 }];
      }
    });
  };

  const handleQuantityChange = (id, delta) => {
    setCartItems(prev =>
      prev.map(item =>
        item.id === id ? { ...item, quantity: item.quantity + delta } : item
      ).filter(item => item.quantity > 0)
    );
  };

  const navStyle = {
    background: '#fff',
    padding: '5px 10px',
    borderRadius: '5px',
    textDecoration: 'none',
    color: '#4a90e2',
    fontWeight: 'bold',
    display: 'flex',
    alignItems: 'center',
    gap: '5px',
  };

  const buttonStyle = {
    background: '#4a90e2',
    color: '#fff',
    border: 'none',
    padding: '8px 12px',
    borderRadius: '5px',
    cursor: 'pointer',
  };

  const inputStyle = {
    display: 'block',
    margin: '10px 0',
    padding: '8px',
    borderRadius: '5px',
    border: '1px solid #ccc',
    width: '250px',
  };

  return (
    <Router>
      <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', background: '#f4f4f4', minHeight: '100vh' }}>
        <header style={{ background: '#4a90e2', color: '#fff', padding: '10px 20px', borderRadius: '8px', marginBottom: '20px' }}>
          <Link to="/" style={{ textDecoration: 'none', color: 'white' }}>
            <h1>📚 Online Bookstore</h1>
          </Link>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            <Link style={navStyle} to="/login"><FaSignInAlt /> Login</Link>
            <Link style={navStyle} to="/register">Register</Link>
            <Link style={navStyle} to="/profile"><FaUser /> Profile</Link>
            <Link style={navStyle} to="/order-history">Order History</Link>
            <Link style={navStyle} to="/admin">Admin</Link>
            <Link style={navStyle} to="/cart"><FaShoppingCart /> Cart ({cartItems.reduce((sum, item) => sum + item.quantity, 0)})</Link>
            <div style={{ display: 'flex', alignItems: 'center', background: '#fff', borderRadius: '5px', padding: '5px 10px' }}>
              <FaSearch color="#4a90e2" />
              <input
                type="text"
                placeholder="Search books..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ border: 'none', outline: 'none', marginLeft: '5px' }}
              />
            </div>
          </div>
        </header>

        <Routes>
          <Route path="/" element={
            <Home 
              featuredBooks={featuredBooks} 
              comingSoonBooks={comingSoonBooks} 
              handleAddToCart={handleAddToCart}
              loading={loading}
              error={error}
            />} 
          />
          <Route path="/cart" element={<CartPage cartItems={cartItems} handleQuantityChange={handleQuantityChange} />} />
          <Route path="/checkout" element={<CheckoutPage cartItems={cartItems} setCartItems={setCartItems} setOrders={setOrders} />} />
          <Route path="/order-history" element={<OrderHistoryPage orders={orders} setCartItems={setCartItems} />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </div>
    </Router>
  );
}

function Home({ featuredBooks, comingSoonBooks, handleAddToCart, loading, error }) {
  const cardStyle = {
    background: '#fff',
    padding: '15px',
    borderRadius: '10px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    textAlign: 'center',
    transition: 'transform 0.2s',
    ':hover': {
      transform: 'scale(1.02)'
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Loading books...</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;

  return (
    <div>
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>📘 Featured Books</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            {book.coverImage && (
              <img 
                src={`http://localhost:5000/img/${book.coverImage.split('/').pop()}`} 
                alt={book.title}
                style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
                onError={(e) => {
                  e.target.src = 'http://localhost:5000/img/placeholder.jpg';
                }}
              />
            )}
            <h3 style={{ margin: '10px 0' }}>{book.title}</h3>
            <p style={{ color: '#666' }}>by {book.author}</p>
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', margin: '10px 0' }}>
              {[...Array(5)].map((_, i) => (
                <FaStar 
                  key={i} 
                  color={i < Math.floor(book.rating || 0) ? '#FFD700' : '#C0C0C0'} 
                  style={{ margin: '0 2px' }}
                />
              ))}
              <span style={{ marginLeft: '5px' }}>({book.rating?.toFixed(1) || '0.0'})</span>
            </div>
            <p style={{ fontWeight: 'bold', fontSize: '1.2em', margin: '10px 0' }}>${book.price}</p>
            <button 
              style={{
                background: '#4a90e2',
                color: '#fff',
                border: 'none',
                padding: '10px 15px',
                borderRadius: '5px',
                cursor: 'pointer',
                width: '100%'
              }} 
              onClick={() => handleAddToCart(book)}
            >
              Add to Cart
            </button>
          </div>
        ))}
      </div>

      <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
        {comingSoonBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            {book.coverImage && (
              <img 
                src={`http://localhost:5000/img/${book.coverImage.split('/').pop()}`} 
                alt={book.title}
                style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
                onError={(e) => {
                  e.target.src = 'http://localhost:5000/img/placeholder.jpg';
                }}
              />
            )}
            <h3 style={{ margin: '10px 0' }}>{book.title}</h3>
            <p style={{ color: '#666' }}>by {book.author}</p>
            <p style={{ color: '#50c878', fontWeight: 'bold' }}><i>Coming Soon</i></p>
          </div>
        ))}
      </div>
    </div>
  );
}

function CartPage({ cartItems, handleQuantityChange }) {
  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>🛒 Your Cart</h2>
      {cartItems.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          {cartItems.map(item => (
            <div key={item.id} style={{ 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'space-between',
              background: '#fff',
              padding: '15px',
              borderRadius: '8px',
              margin: '10px 0',
              boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
            }}>
              <div style={{ flex: 2 }}>
                <h3 style={{ margin: 0 }}>{item.title}</h3>
                <p style={{ margin: '5px 0', color: '#666' }}>by {item.author}</p>
              </div>
              <div style={{ flex: 1, textAlign: 'right' }}>
                <p style={{ margin: 0 }}>${item.price} × {item.quantity}</p>
                <p style={{ margin: '5px 0', fontWeight: 'bold' }}>${(item.price * item.quantity).toFixed(2)}</p>
              </div>
              <div style={{ flex: 0 }}>
                <button 
                  onClick={() => handleQuantityChange(item.id, -1)} 
                  style={{ 
                    background: '#ff4444',
                    color: 'white',
                    border: 'none',
                    padding: '5px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginRight: '5px'
                  }}
                >
                  -
                </button>
                <button 
                  onClick={() => handleQuantityChange(item.id, 1)} 
                  style={{ 
                    background: '#4a90e2',
                    color: 'white',
                    border: 'none',
                    padding: '5px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  +
                </button>
              </div>
            </div>
          ))}
          <div style={{ 
            background: '#fff',
            padding: '15px',
            borderRadius: '8px',
            marginTop: '20px',
            boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ textAlign: 'right' }}>Total: ${total.toFixed(2)}</h3>
            <div style={{ textAlign: 'right' }}>
              <Link to="/checkout">
                <button style={{ 
                  background: '#50c878',
                  color: 'white',
                  border: 'none',
                  padding: '10px 20px',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '1.1em'
                }}>
                  Proceed to Checkout
                </button>
              </Link>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [paymentInfo, setPaymentInfo] = useState('Visa **** 1234');
  const [address, setAddress] = useState('123 Main St, City');
  const [email, setEmail] = useState('user@example.com');
  const [confirmed, setConfirmed] = useState(false);

  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const tax = total * 0.07;
  const grandTotal = total + tax;

  const handleConfirm = () => {
    const newOrder = {
      id: Date.now(),
      items: [...cartItems],
      total: grandTotal,
      date: new Date().toISOString(),
      status: 'Completed'
    };
    setOrders(prev => [...prev, newOrder]);
    setCartItems([]);
    setConfirmed(true);
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Checkout</h2>
      
      {!confirmed ? (
        <>
          <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
            <h3>Shipping Information</h3>
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{email}</span>
                <button 
                  onClick={() => setEmail(prompt('Enter new email', email) || email)}
                  style={{ 
                    background: '#4a90e2',
                    color: 'white',
                    border: 'none',
                    padding: '5px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginLeft: '10px'
                  }}
                >
                  Change
                </button>
              </div>
            </div>
            
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Shipping Address:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{address}</span>
                <button 
                  onClick={() => setAddress(prompt('Enter new address', address) || address)}
                  style={{ 
                    background: '#4a90e2',
                    color: 'white',
                    border: 'none',
                    padding: '5px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginLeft: '10px'
                  }}
                >
                  Change
                </button>
              </div>
            </div>
            
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment Method:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{paymentInfo}</span>
                <button 
                  onClick={() => setPaymentInfo(prompt('Enter new payment info', paymentInfo) || paymentInfo)}
                  style={{ 
                    background: '#4a90e2',
                    color: 'white',
                    border: 'none',
                    padding: '5px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    marginLeft: '10px'
                  }}
                >
                  Change
                </button>
              </div>
            </div>
          </div>
          
          <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
            <h3>Order Summary</h3>
            {cartItems.map(item => (
              <div key={item.id} style={{ 
                display: 'flex', 
                justifyContent: 'space-between',
                padding: '10px 0',
                borderBottom: '1px solid #eee'
              }}>
                <span>{item.title} × {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              padding: '10px 0',
              borderBottom: '1px solid #eee'
            }}>
              <span>Subtotal:</span>
              <span>${total.toFixed(2)}</span>
            </div>
            
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              padding: '10px 0',
              borderBottom: '1px solid #eee'
            }}>
              <span>Tax (7%):</span>
              <span>${tax.toFixed(2)}</span>
            </div>
            
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              padding: '10px 0',
              fontWeight: 'bold',
              fontSize: '1.1em'
            }}>
              <span>Total:</span>
              <span>${grandTotal.toFixed(2)}</span>
            </div>
          </div>
          
          <button 
            onClick={handleConfirm}
            style={{ 
              background: '#50c878',
              color: 'white',
              border: 'none',
              padding: '15px 30px',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '1.1em',
              width: '100%'
            }}
          >
            Confirm Order
          </button>
        </>
      ) : (
        <div style={{ 
          background: '#fff', 
          padding: '30px', 
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <div style={{ 
            fontSize: '4em',
            color: '#50c878',
            marginBottom: '20px'
          }}>
            ✓
          </div>
          <h2>Order Confirmed!</h2>
          <p style={{ fontSize: '1.2em', marginBottom: '30px' }}>
            Thank you for your purchase. A confirmation email has been sent to {email}.
          </p>
          <Link to="/" style={{ textDecoration: 'none' }}>
            <button style={{ 
              background: '#4a90e2',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '1.1em'
            }}>
              Continue Shopping
            </button>
          </Link>
        </div>
      )}
    </div>
  );
}

function OrderHistoryPage({ orders, setCartItems }) {
  const handleReorder = (items) => {
    setCartItems(items);
    alert('Items have been added to your cart!');
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Order History</h2>
      
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        orders.map(order => (
          <div key={order.id} style={{ 
            background: '#fff',
            padding: '20px',
            borderRadius: '8px',
            marginBottom: '20px',
            boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
          }}>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '15px',
              borderBottom: '1px solid #eee',
              paddingBottom: '10px'
            }}>
              <div>
                <h3 style={{ margin: 0 }}>Order #{order.id}</h3>
                <p style={{ margin: '5px 0', color: '#666' }}>
                  {new Date(order.date).toLocaleDateString()} • {order.status}
                </p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.2em' }}>
                  ${order.total.toFixed(2)}
                </p>
              </div>
            </div>
            
            {order.items.map(item => (
              <div key={item.id} style={{ 
                display: 'flex',
                justifyContent: 'space-between',
                padding: '8px 0',
                borderBottom: '1px solid #f5f5f5'
              }}>
                <span>{item.title} × {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
            
            <div style={{ textAlign: 'right', marginTop: '15px' }}>
              <button 
                onClick={() => handleReorder(order.items)}
                style={{ 
                  background: '#4a90e2',
                  color: 'white',
                  border: 'none',
                  padding: '8px 15px',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                Reorder
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    alert(`Logged in as ${email}`);
    // Here you would typically make an API call to authenticate
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit} style={{ 
        background: '#fff',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
      }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
            required
          />
        </div>
        
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
            required
          />
        </div>
        
        <button 
          type="submit"
          style={{ 
            background: '#4a90e2',
            color: 'white',
            border: 'none',
            padding: '10px 15px',
            borderRadius: '5px',
            cursor: 'pointer',
            width: '100%'
          }}
        >
          Login
        </button>
      </form>
    </div>
  );
}

function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      alert("Passwords don't match!");
      return;
    }
    alert(`Registered ${email}`);
    // Here you would typically make an API call to register
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <h2>Register</h2>
      <form onSubmit={handleSubmit} style={{ 
        background: '#fff',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
      }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
            required
          />
        </div>
        
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
            required
          />
        </div>
        
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Confirm Password:</label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
            required
          />
        </div>
        
        <button 
          type="submit"
          style={{ 
            background: '#4a90e2',
            color: 'white',
            border: 'none',
            padding: '10px 15px',
            borderRadius: '5px',
            cursor: 'pointer',
            width: '100%'
          }}
        >
          Register
        </button>
      </form>
    </div>
  );
}

function ProfilePage() {
  const [name, setName] = useState('John Doe');
  const [email, setEmail] = useState('john.doe@example.com');
  const [address, setAddress] = useState('123 Main St, City');
  const [paymentMethod, setPaymentMethod] = useState('Visa **** 1234');

  const handleSubmit = (e) => {
    e.preventDefault();
    alert('Profile updated successfully!');
    // Here you would typically make an API call to update profile
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h2>Profile</h2>
      <form onSubmit={handleSubmit} style={{ 
        background: '#fff',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
      }}>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Full Name:</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
          />
        </div>
        
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
          />
        </div>
        
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Shipping Address:</label>
          <textarea
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc',
              minHeight: '80px'
            }}
          />
        </div>
        
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment Method:</label>
          <input
            type="text"
            value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
            style={{ 
              width: '100%',
              padding: '8px',
              borderRadius: '4px',
              border: '1px solid #ccc'
            }}
          />
        </div>
        
        <button 
          type="submit"
          style={{ 
            background: '#4a90e2',
            color: 'white',
            border: 'none',
            padding: '10px 15px',
            borderRadius: '5px',
            cursor: 'pointer',
            width: '100%'
          }}
        >
          Save Profile
        </button>
      </form>
    </div>
  );
}

function AdminPage() {
  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h2>Admin Panel</h2>
      <div style={{ 
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
        gap: '20px',
        marginTop: '20px'
      }}>
        <div style={{ 
          background: '#fff',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
          textAlign: 'center'
        }}>
          <h3>Manage Books</h3>
          <p>Add, edit, or remove books from the store</p>
          <Link to="/admin/books">
            <button style={{ 
              background: '#4a90e2',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '5px',
              cursor: 'pointer',
              marginTop: '10px'
            }}>
              Go to Books
            </button>
          </Link>
        </div>
        
        <div style={{ 
          background: '#fff',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
          textAlign: 'center'
        }}>
          <h3>Manage Users</h3>
          <p>View and manage customer accounts</p>
          <Link to="/admin/users">
            <button style={{ 
              background: '#4a90e2',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '5px',
              cursor: 'pointer',
              marginTop: '10px'
            }}>
              Go to Users
            </button>
          </Link>
        </div>
        
        <div style={{ 
          background: '#fff',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
          textAlign: 'center'
        }}>
          <h3>Manage Orders</h3>
          <p>View and process customer orders</p>
          <Link to="/admin/orders">
            <button style={{ 
              background: '#4a90e2',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '5px',
              cursor: 'pointer',
              marginTop: '10px'
            }}>
              Go to Orders
            </button>
          </Link>
        </div>
        
        <div style={{ 
          background: '#fff',
          padding: '20px',
          borderRadius: '8px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
          textAlign: 'center'
        }}>
          <h3>Reports</h3>
          <p>View sales and inventory reports</p>
          <Link to="/admin/reports">
            <button style={{ 
              background: '#4a90e2',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '5px',
              cursor: 'pointer',
              marginTop: '10px'
            }}>
              View Reports
            </button>
          </Link>
        </div>
      </div>
    </div>
  );
}