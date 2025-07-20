// This will be the updated App.js with all page imports and routing
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom'; 
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch } from 'react-icons/fa';
import { useState, useEffect } from 'react'; 

// Page Components
import Home from './pages/Home';
import CartPage from './pages/CartPage';
import CheckoutPage from './pages/CheckoutPage';
import OrderHistoryPage from './pages/OrderHistoryPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import WelcomePage from './pages/WelcomePage';
import BookDetailPage from './pages/BookDetailPage';


export default function App() {
  const [search, setSearch] = useState('');
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [featuredBooks, setFeaturedBooks] = useState([]);
  const [comingSoonBooks, setComingSoonBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchResults, setSearchResults] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);


  useEffect(() => {
    const searchBooks = async () => {
      if (!search.trim()) {
        setSearchResults(null);
        return;
      }
      try {
        setLoading(true);
        const response = await fetch(`http://localhost:8080/api/books?search=${encodeURIComponent(search.trim())}`);
        if (!response.ok) throw new Error('Failed to search books');
        const data = await response.json();
        setSearchResults(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error('Error searching books:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    const timeoutId = setTimeout(searchBooks, 300);
    return () => clearTimeout(timeoutId);
  }, [search]);

  useEffect(() => {
    const fetchBooks = async () => {
      try {
        setLoading(true);
        const [featuredResponse, comingSoonResponse] = await Promise.all([
          fetch('http://localhost:8080/api/books?display=featured'),
          fetch('http://localhost:8080/api/books?display=comingsoon')
        ]);
        if (!featuredResponse.ok || !comingSoonResponse.ok) throw new Error('Failed to fetch books');
        const [featuredData, comingSoonData] = await Promise.all([
          featuredResponse.json(),
          comingSoonResponse.json()
        ]);
        setFeaturedBooks(Array.isArray(featuredData) ? featuredData : []);
        setComingSoonBooks(Array.isArray(comingSoonData) ? comingSoonData : []);
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
        return prev.map(item => item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item);
      } else {
        return [...prev, { ...book, quantity: 1 }];
      }
    });
  };

  const handleQuantityChange = (id, delta) => {
    setCartItems(prev => prev.map(item => item.id === id ? { ...item, quantity: item.quantity + delta } : item).filter(item => item.quantity > 0));
  };
/*
  return (
    <Router>
      <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', background: '#f4f4f4', minHeight: '100vh' }}>
        <header style={{ background: '#4a90e2', color: '#fff', padding: '10px 20px', borderRadius: '8px', marginBottom: '20px' }}>
          <Link to="/" style={{ textDecoration: 'none', color: 'white' }}><h1>📚 Online Bookstore</h1></Link>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            <Link to="/login" style={navStyle}><FaSignInAlt /> Login</Link>
            <Link to="/register" style={navStyle}>Register</Link>
            <Link to="/profile" style={navStyle}><FaUser /> Profile</Link>
            <Link to="/order-history" style={navStyle}>Order History</Link>
            <Link to="/admin" style={navStyle}>Admin</Link>
            <Link to="/cart" style={navStyle}><FaShoppingCart /> Cart ({cartItems.reduce((sum, item) => sum + item.quantity, 0)})</Link>
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
          <Route path="/" element={<Home featuredBooks={searchResults || featuredBooks.filter(book => book.featured)} comingSoonBooks={!searchResults ? comingSoonBooks.filter(book => !book.featured) : []} handleAddToCart={handleAddToCart} loading={loading} error={error} isSearching={!!searchResults} searchTerm={search} />} />
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
*/

return (
    <Router>
      <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', background: '#f4f4f4', minHeight: '100vh' }}>
        <header style={{ background: '#4a90e2', color: '#fff', padding: '10px 20px', borderRadius: '8px', marginBottom: '20px' }}>
          <Link to="/home" style={{ textDecoration: 'none', color: 'white' }}><h1>📚 University Bookstore</h1></Link>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            {!isLoggedIn && <>
              <Link to="/login" style={navStyle}><FaSignInAlt /> Login</Link>
              <Link to="/register" style={navStyle}>Register</Link>
            </>}
            {isLoggedIn && <>
              <Link to="/profile" style={navStyle}><FaUser /> Profile</Link>
              <Link to="/order-history" style={navStyle}>Order History</Link>
              <Link to="/admin" style={navStyle}>Admin</Link>
              <Link to="/cart" style={navStyle}><FaShoppingCart /> Cart ({cartItems.reduce((sum, item) => sum + item.quantity, 0)})</Link>
            </>}
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
          {/* Step #1: Welcome screen at root */}
          <Route path="/" element={<WelcomePage />} />

          {/* Step #2: Login/Register update with setIsLoggedIn */}
          <Route path="/login" element={<LoginPage setIsLoggedIn={setIsLoggedIn} />} />
          <Route path="/register" element={<RegisterPage setIsLoggedIn={setIsLoggedIn} />} />

          {/* Step #3: Only show book store if logged in */}
          {/*
          <Route
            path="/home"
            element={isLoggedIn
              ? <Home
                  featuredBooks={searchResults || featuredBooks}
                  comingSoonBooks={!searchResults ? comingSoonBooks : []}
                  handleAddToCart={handleAddToCart}
                  loading={loading}
                  error={error}
                  isSearching={!!searchResults}
                  searchTerm={search}
                />
              : <Navigate to="/" replace />
            }
          />
          */}
          <Route
            path="/home"
            element={
           <Home
              featuredBooks={searchResults || featuredBooks}
              comingSoonBooks={!searchResults ? comingSoonBooks : []}
              handleAddToCart={handleAddToCart}
              loading={loading}
              error={error}
              isSearching={!!searchResults}
              searchTerm={search}
            />
          }
          />

          <Route path="/book/:id" element={<BookDetailPage handleAddToCart={handleAddToCart} />} />
          {/* Protected routes */}
          <Route path="/cart" element={isLoggedIn ? <CartPage cartItems={cartItems} handleQuantityChange={handleQuantityChange} /> : <Navigate to="/" />} />
          <Route path="/checkout" element={isLoggedIn ? <CheckoutPage cartItems={cartItems} setCartItems={setCartItems} setOrders={setOrders} /> : <Navigate to="/" />} />
          <Route path="/order-history" element={isLoggedIn ? <OrderHistoryPage orders={orders} setCartItems={setCartItems} /> : <Navigate to="/" />} />
          <Route path="/profile" element={isLoggedIn ? <ProfilePage /> : <Navigate to="/" />} />
          <Route path="/admin" element={isLoggedIn ? <AdminPage /> : <Navigate to="/" />} />
        </Routes>
      </div>
    </Router>
  );
}

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
