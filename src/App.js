//  imported things from the  React router dom for  the routing.
 import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
// Allso I imported some  icons for the UI.
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch } from 'react-icons/fa';
// Aso, I imported the   useState hook in order  to manage the  state.
import { useState } from 'react';

// export of  the App component.
export default function App() {
  // Here  is the  state which will  store  the input used within the search.
   const [search, setSearch] = useState('');
  // Here is the state which will  store the eleements put into the cart.
  const [cartItems, setCartItems] = useState([]);
  // Here is the  state to store the  orders made.
   const [orders, setOrders] = useState([]);

  // This array here is  of  the featured books that shows on webpage
  const featuredBooks = [
   { id: 1, title: 'Clean Code', author: 'Robert C. Martin', genre: 'Programming', price: 30 },
    { id: 2, title: 'The Pragmatic Programmer', author: 'Andrew Hunt', genre: 'Programming', price: 35 },
    { id: 3, title: 'Refactoring', author: 'Martin Fowler', genre: 'Programming', price: 32 },
    { id: 4, title: 'Design Patterns', author: 'GoF', genre: 'Programming', price: 38 },
  ];

  // This is the array of vriety of books that  shown on the wbeoage as coming soon.
  const comingSoonBooks = [
    { id: 5, title: 'AI for Everyone', author: 'Andrew Ng', genre: 'Technology', price: 40 },
    { id: 6, title: 'Next.js in Action', author: 'Vercel Team', genre: 'Web Development', price: 45 },
    { id: 7, title: 'ML Systems', author: 'Various', genre: 'AI', price: 50 },
    { id: 8, title: 'Cloud Native', author: 'Tech Writers', genre: 'Cloud', price: 42 },
  ];

  // This functionhere is implemented  to add  books to the cart.
  const handleAddToCart = (book) => {
    // Here updates the cartItems state based on the  previous value.
     setCartItems(prev => {
      //condition  to  find if the book already exists within cart
      const existing = prev.find(item => item.id === book.id);
      //  so , if it exists, increase  the quantity.
      if (existing) {
        return prev.map(item => item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item);
      } else {
        // this else ststement  make sure to  add new book with quantity 1.
        return [...prev, { ...book, quantity: 1 }];
      }
    });
  };

  // function to change quantity of a cart item.
  const handleQuantityChange = (id, delta) => {
    // This will  update  the cartItems based on previous value.
    setCartItems(prev =>
      // this  maps  through the  items and  find the one to update
      prev.map(item =>
        item.id === id ? { ...item, quantity: item.quantity + delta } : item
      )
      //  removes the  items with  a quantity of  < = 0.
       .filter(item => item.quantity > 0)
    );
  };

  //Here will return the JSX for this app.
  return (
    //Here the  Router is  implemented and used to enable routing within the  app
    <Router>
      {/*  the  main container div with  its styles */}
      <div style ={{ fontFamily: 'Arial, sans-serif', padding: '20px', background: '#f4f4f4', minHeight: '100vh' }}>
        {/*  the header with  its background and styles */}
        <header style={{ background: '#4a90e2', color: '#fff', padding: '10px 20px', borderRadius: '8px', marginBottom: '20px' }}>
          {/* a  link to  the  home  with site title */}
          <Link to ="/" style={{ textDecoration: 'none', color: 'white' }}>
            <h1>📚 Online Bookstore</h1>
          </Link>
          {/*  thee  nav bar with links and search */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            {/*  the login link with its designated  icon */}
            <Link style ={navStyle} to="/login"><FaSignInAlt /> Login</Link>
            {/*  here is the register link */}
            <Link style ={navStyle} to="/register">Register</Link>
            {/*  here is the profile link with icon */}
            <Link style={navStyle} to="/profile"><FaUser /> Profile</Link>
            {/*  thi s is the order history link */}
            <Link style={navStyle} to="/order-history">Order History</Link>
            {/*  this is the admin link */}
            <Link style={navStyle} to="/admin">Admin</Link>
            {/*  Here the cart will link with  the cart count */}
            <Link style={navStyle} to="/cart"><FaShoppingCart /> Cart ({cartItems.reduce((sum, item) => sum + item.quantity, 0)})</Link>
            {/*  this is the search bar container */}
            <div style={{ display: 'flex', alignItems: 'center', background: '#fff', borderRadius: '5px', padding: '5px 10px' }}>
              {/*  this is the  the search icon */}
               <FaSearch color = "#4a90e2" />
              {/*  Here is the sinput feild for search */}
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

        {/*  Here I am defining the routes for the  different pages */}
        <Routes>
          {/* This is the route for the  webppage,  its passing featuredBooks and also the comingSoonBooks as props */}
          <Route path ="/" element={<Home featuredBooks={featuredBooks} comingSoonBooks={comingSoonBooks} handleAddToCart={handleAddToCart} />} />
          {/* Here is the route for cart page */}
          <Route path="/cart" element={<CartPage cartItems={cartItems} handleQuantityChange={handleQuantityChange} />} />
          {/* This is the route for checkout page */}
          <Route path ="/checkout" element={<CheckoutPage cartItems={cartItems} setCartItems={setCartItems} setOrders={setOrders} />} />
          {/* HEre is the route for orderr history page */}
          <Route path="/order-history" element={<OrderHistoryPage orders={orders} setCartItems={setCartItems} />} />
          {/* Here is the route for login page */}
          <Route path ="/login" element={<LoginPage />} />
          {/*  Here is the route for register page */}
          <Route path="/register" element={<RegisterPage />} />
          {/* This is the route for profile page */}
          <Route path="/profile" element={<ProfilePage />} />
          {/* This is the route for admin page */}
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </div>
    </Router>
  );
}
