import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch } from 'react-icons/fa';
import { useState } from 'react';

export default function App() {
  const [search, setSearch] = useState('');
  const [cartItems, setCartItems] = useState([]);
  const [orders, setOrders] = useState([]);

  const featuredBooks = [
    { id: 1, title: 'Clean Code', author: 'Robert C. Martin', genre: 'Programming', price: 30 },
    { id: 2, title: 'The Pragmatic Programmer', author: 'Andrew Hunt', genre: 'Programming', price: 35 },
    { id: 3, title: 'Refactoring', author: 'Martin Fowler', genre: 'Programming', price: 32 },
    { id: 4, title: 'Design Patterns', author: 'GoF', genre: 'Programming', price: 38 },
  ];

  const comingSoonBooks = [
    { id: 5, title: 'AI for Everyone', author: 'Andrew Ng', genre: 'Technology', price: 40 },
    { id: 6, title: 'Next.js in Action', author: 'Vercel Team', genre: 'Web Development', price: 45 },
    { id: 7, title: 'ML Systems', author: 'Various', genre: 'AI', price: 50 },
    { id: 8, title: 'Cloud Native', author: 'Tech Writers', genre: 'Cloud', price: 42 },
  ];

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
    setCartItems(prev =>
      prev.map(item =>
        item.id === id ? { ...item, quantity: item.quantity + delta } : item
      ).filter(item => item.quantity > 0)
    );
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
          <Route path="/" element={<Home featuredBooks={featuredBooks} comingSoonBooks={comingSoonBooks} handleAddToCart={handleAddToCart} />} />
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

function Home({ featuredBooks, comingSoonBooks, handleAddToCart }) {
  const cardStyle = {
    background: '#fff',
    padding: '15px',
    borderRadius: '10px',
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
    textAlign: 'center'
  };

  return (
    <div>
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>📘 Featured Books</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '15px', marginBottom: '20px' }}>
        {featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            <h3>{book.title}</h3>
            <p>by {book.author}</p>
            <p>${book.price}</p>
            <button style={buttonStyle} onClick={() => handleAddToCart(book)}>Add to Cart</button>
          </div>
        ))}
      </div>

      <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '15px' }}>
        {comingSoonBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            <h3>{book.title}</h3>
            <p>by {book.author}</p>
            <p><i>Coming Soon</i></p>
          </div>
        ))}
      </div>
    </div>
  );
}

function CartPage({ cartItems, handleQuantityChange }) {
  return (
    <div>
      <h2>🛒 Your Cart</h2>
      {cartItems.length === 0 ? <p>Your cart is empty.</p> :
        cartItems.map(item => (
          <div key={item.id} style={{ background: '#fff', padding: '10px', borderRadius: '8px', margin: '10px 0', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
            <p>{item.title} - ${item.price} x {item.quantity}</p>
            <button onClick={() => handleQuantityChange(item.id, -1)} style={buttonStyle}>-</button>
            <button onClick={() => handleQuantityChange(item.id, 1)} style={{ ...buttonStyle, marginLeft: '5px' }}>+</button>
          </div>
        ))
      }
      {cartItems.length > 0 && <Link to="/checkout"><button style={{ ...buttonStyle, marginTop: '10px' }}>Proceed to Checkout</button></Link>}
    </div>
  );
}

function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [paymentInfo, setPaymentInfo] = useState('Visa **** 1234');
  const [address, setAddress] = useState('123 Main St, City');
  const [email, setEmail] = useState('user@example.com');
  const [confirmed, setConfirmed] = useState(false);

  const total = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const tax = total * 0.07;
  const grandTotal = total + tax;

  const handleConfirm = () => {
    setOrders(prev => [...prev, { id: Date.now(), items: cartItems, total: grandTotal }]);
    setCartItems([]);
    setConfirmed(true);
  };

  return (
    <div>
      <h2>Checkout</h2>
      <p><b>Payment:</b> {paymentInfo} <button onClick={() => setPaymentInfo(prompt('Enter new payment info', paymentInfo) || paymentInfo)}>Change</button></p>
      <p><b>Shipping Address:</b> {address} <button onClick={() => setAddress(prompt('Enter new address', address) || address)}>Change</button></p>
      <p><b>Email:</b> {email} <button onClick={() => setEmail(prompt('Enter new email', email) || email)}>Change</button></p>

      <h3>Order Summary</h3>
      {cartItems.map(item => <p key={item.id}>{item.title} x {item.quantity} - ${item.price * item.quantity}</p>)}
      <p>Subtotal: ${total.toFixed(2)}</p>
      <p>Tax (7%): ${tax.toFixed(2)}</p>
      <p><b>Total: ${grandTotal.toFixed(2)}</b></p>
      <button onClick={handleConfirm} style={buttonStyle}>Confirm Order</button>

      {confirmed && (
        <div>
          <h3>✅ Order Confirmed</h3>
          <p>A confirmation email has been sent to {email}.</p>
        </div>
      )}
    </div>
  );
}

function OrderHistoryPage({ orders, setCartItems }) {
  const handleReorder = (items) => {
    setCartItems(items);
    alert('Items re-added to cart.');
  };

  return (
    <div>
      <h2>Order History</h2>
      {orders.length === 0 ? <p>No past orders.</p> :
        orders.map(order => (
          <div key={order.id} style={{ background: '#fff', padding: '10px', borderRadius: '8px', margin: '10px 0' }}>
            <p>Order #{order.id} - ${order.total.toFixed(2)}</p>
            {order.items.map(item => (
              <p key={item.id}>{item.title} x {item.quantity}</p>
            ))}
            <button onClick={() => handleReorder(order.items)} style={buttonStyle}>Reorder</button>
          </div>
        ))}
    </div>
  );
}

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = () => {
    alert(`Logged in as ${email}`);
    navigate('/');
  };

  return (
    <div>
      <h2>Login</h2>
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} style={inputStyle} />
      <input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} style={inputStyle} />
      <button onClick={handleLogin} style={buttonStyle}>Login</button>
    </div>
  );
}

function RegisterPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleRegister = () => {
    alert(`Registered ${email}`);
    navigate('/login');
  };

  return (
    <div>
      <h2>Register</h2>
      <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} style={inputStyle} />
      <input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} style={inputStyle} />
      <button onClick={handleRegister} style={buttonStyle}>Register</button>
    </div>
  );
}

function ProfilePage() {
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [payment, setPayment] = useState('');

  const handleSave = () => {
    alert('Profile updated.');
  };

  return (
    <div>
      <h2>Profile</h2>
      <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} style={inputStyle} />
      <input placeholder="Address" value={address} onChange={e => setAddress(e.target.value)} style={inputStyle} />
      <input placeholder="Payment Info" value={payment} onChange={e => setPayment(e.target.value)} style={inputStyle} />
      <button onClick={handleSave} style={buttonStyle}>Save</button>
    </div>
  );
}

function AdminPage() {
  return (
    <div>
      <h2>Admin Panel</h2>
      <button style={buttonStyle}>Manage Books</button>
      <button style={{ ...buttonStyle, marginLeft: '10px' }}>Manage Users</button>
      <button style={{ ...buttonStyle, marginLeft: '10px' }}>Manage Promotions</button>
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
