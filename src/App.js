import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom'; 
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch } from 'react-icons/fa';
import { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from "./AuthContext";
import CartService from './CartService';

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
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  );
}

function AppContent() {
  const [search, setSearch] = useState('');
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);
  const [featuredBooks, setFeaturedBooks] = useState([]);
  const [comingSoonBooks, setComingSoonBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchResults, setSearchResults] = useState(null);
  const [logoutMessage, setLogoutMessage] = useState('');
  const [cartErrorMessage, setCartErrorMessage] = useState('');
  const { auth, setAuth } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetch('/api/auth/check-session', {
      method: 'GET',
      credentials: 'include',
    })
      .then(res => res.json())
      .then(data => {
        if (data.authenticated) {
          setAuth({
            isLoggedIn: true,
            userRole: data.user_role,
            userName: data.user_name,
            userEmail: data.user_email,
          });
          // Merge guest cart with user cart and load user cart
          handleUserLogin();
        } else {
          setAuth({
            isLoggedIn: false,
            userRole: null,
            userName: null,
            userEmail: null,
          });
          // Load guest cart
          loadGuestCart();
        }
      })
      .catch(() => {
        setAuth({
          isLoggedIn: false,
          userRole: null,
          userName: null,
          userEmail: null,
        });
        // Load guest cart
        loadGuestCart();
      });
  }, [setAuth]);

  // Watch for authentication state changes
  useEffect(() => {
    if (auth.isLoggedIn) {
      loadAuthenticatedCart();
    } else {
      loadGuestCart();
    }
  }, [auth.isLoggedIn]);

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

  // Load guest cart from localStorage
  const loadGuestCart = () => {
    try {
      const guestCart = CartService.getGuestCart();
      setCartItems(guestCart);
    } catch (error) {
      console.error('Error loading guest cart:', error);
      setCartItems([]);
    }
  };

  // Load authenticated user cart from database
  const loadAuthenticatedCart = async () => {
    try {
      const userCart = await CartService.getAuthenticatedCart();
      setCartItems(userCart);
    } catch (error) {
      console.error('Error loading authenticated cart:', error);
      setCartItems([]);
      setCartErrorMessage(error.message || 'Failed to load authenticated cart.');
      if (error.message && error.message.includes('Unauthorized')) {
        setTimeout(() => {
          setCartErrorMessage('');
          navigate('/login');
        }, 1500);
      }
    }
  };

  // Handle user login - merge guest cart and load user cart
  const handleUserLogin = async () => {
    try {
      // Merge guest cart with user cart
      await CartService.mergeGuestCart();
      // Load the merged cart
      await loadAuthenticatedCart();
    } catch (error) {
      console.error('Error handling user login:', error);
      setCartErrorMessage(error.message || 'Error merging guest cart.');
      // Fallback to loading authenticated cart without merge
      await loadAuthenticatedCart();
    }
  };

  const handleAddToCart = async (book) => {
    try {
      const updatedCart = await CartService.addToCart(book, auth.isLoggedIn);
      setCartItems(updatedCart);
      setCartErrorMessage('');
    } catch (error) {
      setCartErrorMessage(error.message || 'Error adding to cart.');
      if (error.message && error.message.includes('Unauthorized')) {
        // Show message and redirect to login after a short delay
        setTimeout(() => {
          setCartErrorMessage('');
          navigate('/login');
        }, 1500);
        return;
      }
      // Fallback to current cart behavior for guests
      if (!auth.isLoggedIn) {
        setCartItems(prev => {
          const existing = prev.find(item => item.id === book.id);
          if (existing) {
            return prev.map(item => item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item);
          } else {
            return [...prev, { ...book, quantity: 1 }];
          }
        });
      }
    }
  };

  const handleQuantityChange = async (id, delta) => {
    try {
      const updatedCart = await CartService.updateCartQuantity(id, delta, auth.isLoggedIn);
      setCartItems(updatedCart);
      setCartErrorMessage('');
    } catch (error) {
      setCartErrorMessage(error.message || 'Error updating cart quantity.');
      if (error.message && error.message.includes('Unauthorized')) {
        setTimeout(() => {
          setCartErrorMessage('');
          navigate('/login');
        }, 1500);
        return;
      }
      // Fallback to current cart behavior
      setCartItems(prev => prev.map(item => item.id === id ? { ...item, quantity: item.quantity + delta } : item).filter(item => item.quantity > 0));
    }
  };

  const handleLogout = async () => {
    try {
      const res = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
      if (res.ok) {
        setAuth({
          isLoggedIn: false,
          userRole: null,
          userName: null,
          userEmail: null,
        });
        // Clear cart on logout and load guest cart
        setCartItems([]);
        CartService.clearGuestCart();
        setLogoutMessage('You have been successfully logged out.');
        navigate('/home');
        setTimeout(() => setLogoutMessage(''), 3000);
      } else {
        console.error('Logout failed');
      }
    } catch (err) {
      console.error('Logout error:', err);
      // Ensure cart is cleared even if logout request fails
      setCartItems([]);
      CartService.clearGuestCart();
    }
  };

  // Debug: log the userRole value
  console.log('userRole:', auth.userRole);
  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', background: '#f4f4f4', minHeight: '100vh' }}>
      <header style={{ background: '#4a90e2', color: '#fff', padding: '10px 20px', borderRadius: '8px', marginBottom: '20px' }}>
        <Link to="/home" style={{ textDecoration: 'none', color: 'white' }}><h1>📚 University Bookstore</h1></Link>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          {auth.isLoggedIn ? (
            <>
              <Link to="/profile" style={navStyle}><FaUser /> Profile</Link>
              <Link to="/order-history" style={navStyle}>Order History</Link>
              {auth.userRole === 'admin' && (
                <Link to="/admin" style={navStyle}>Admin</Link>
              )}
              <button
                onClick={handleLogout}
                style={{ ...navStyle, background: '#fff', cursor: 'pointer', border: 'none' }}
              >
                Logout
              </button>
            </>
          ) : (
            <Link to="/register" style={navStyle}><FaUser /> Register/Login</Link>
          )}
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
          {/*<Link to="/reset-password?token=test123"> Test Reset Password Page</Link>*/}
        </div>
        {logoutMessage && <p style={{ color: 'white', marginTop: '10px' }}>{logoutMessage}</p>}
        {cartErrorMessage && <p style={{ color: 'yellow', marginTop: '10px' }}>{cartErrorMessage}</p>}
      </header>

      <Routes>
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/home" element={
          <Home
            featuredBooks={searchResults || featuredBooks}
            comingSoonBooks={!searchResults ? comingSoonBooks : []}
            handleAddToCart={handleAddToCart}
            loading={loading}
            error={error}
            isSearching={!!searchResults}
            searchTerm={search}
            isLoggedIn={auth.isLoggedIn}
          />
        } />
        <Route path="/book/:id" element={<BookDetailPage handleAddToCart={handleAddToCart} />} />
        <Route path="/cart" element={<CartPage cartItems={cartItems} handleQuantityChange={handleQuantityChange} isLoggedIn={auth.isLoggedIn} />} />
        <Route path="/checkout" element={
          auth.isLoggedIn ? (
            <CheckoutPage cartItems={cartItems} setCartItems={setCartItems} setOrders={setOrders} />
          ) : (
            <Navigate to="/login" replace />
          )
        } />
        <Route path="/order-history" element={auth.isLoggedIn ? <OrderHistoryPage orders={orders} setCartItems={setCartItems} /> : <Navigate to="/register" />} />
        <Route path="/profile" element={auth.isLoggedIn ? <ProfilePage /> : <Navigate to="/register" />} />
        <Route path="/admin" element={auth.isLoggedIn && auth.userRole === 'admin' ? <AdminPage /> : <Navigate to="/register" />} />
      </Routes>
    </div>
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