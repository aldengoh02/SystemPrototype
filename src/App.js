// Various imports
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'; 
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch, FaStar } from 'react-icons/fa';
import { useState, useEffect } from 'react'; 

// Main App component the boss of our whole application
export default function App() {
  // For the search bar  remembers what you type
  const [search, setSearch] = useState(''); // Starts empty obviously
  
  // Where we keep all the books in your shopping cart
  const [cartItems, setCartItems] = useState([]); // Empty array at first
  
  // All the past orders people have made
  const [orders, setOrders] = useState([]); // Also empty at start
  
  // Featured books from our database
  const [featuredBooks, setFeaturedBooks] = useState([]); // Will fill this later
  
  // Books that aren't out yet ,  but we will  show them anyway
  const [comingSoonBooks, setComingSoonBooks] = useState([]); // Have to fetch these as well
  
  // Shows loading spinner when waiting for data
  const [loading, setLoading] = useState(true); // True at first cuz we loading
  
  // If something breaks,  then we will  store the error here
  const [error, setError] = useState(null); // Starts as null cuz no errors yet

  // Search results
  const [searchResults, setSearchResults] = useState(null);

  // Search in response to user input
  useEffect(() => {
    const searchBooks = async () => {
      if (!search.trim()) {
        setSearchResults(null);
        return;
      }

      try {
        setLoading(true);
        const response = await fetch(`http://localhost:8080/api/books?search=${encodeURIComponent(search.trim())}`);
        
        if (!response.ok) {
          throw new Error('Failed to search books');
        }

        const data = await response.json();
        setSearchResults(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error('Error searching books:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    // Add debounce to avoid too many requests
    const timeoutId = setTimeout(searchBooks, 300);
    return () => clearTimeout(timeoutId);
  }, [search]);

  // This runs when the  page loads to get our book data
  useEffect(() => {
    //  A Special async function  used toto fetch books
    const fetchBooks = async () => {
      try {
        setLoading(true); // Shows loading spinner
        
        // Gets both types of books at  the same time 
        const [featuredResponse, comingSoonResponse] = await Promise.all([
          fetch('http://localhost:8080/api/books?display=featured'), // Updated endpoint for featured books
          fetch('http://localhost:8080/api/books?display=comingsoon') // Updated endpoint for coming soon books
        ]);

        // Checks to see  if both requests worked
        if (!featuredResponse.ok || !comingSoonResponse.ok) {
          throw new Error('Failed to fetch books from server'); // Updated error message
        }

        // Turn  the responses into JSON we can use
        const [featuredData, comingSoonData] = await Promise.all([
          featuredResponse.json(), // Convert to JSON
          comingSoonResponse.json() //  also this one too
        ]);

        // Updates our state  with the book data - ensure we have arrays
        setFeaturedBooks(Array.isArray(featuredData) ? featuredData : []); // Sets the  featured books
        setComingSoonBooks(Array.isArray(comingSoonData) ? comingSoonData : []); // Sets the  coming soon
      } catch (err) {
        // If anything goes wrong
        setError(err.message); //  It saves an  error message
        console.error('Dang error getting books:', err); // Also will log it
      } finally {
        // No matter what happens
        setLoading(false); // Stops the  loading
      }
    };

    // Actually call our fetch function
    fetchBooks(); // fetches
  }, []); // Empty array = run once when  the page loads

  // When "Add to Cart" is clicked
  const handleAddToCart = (book) => {
    setCartItems(prev => {
      // Checks to see  if a  book already in the  cart
      const existing = prev.find(item => item.id === book.id);
      if (existing) {
        // If yes  then just add 1 to the  quantity
        return prev.map(item => 
          item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      } else {
        // If new then add to cart with quantity of  1
        return [...prev, { ...book, quantity: 1 }];
      }
    });
  };

  // For + and - buttons within the  cart
  const handleQuantityChange = (id, delta) => {
    setCartItems(prev =>
      // Updates the  quantity and  also remove if  it is zero
      prev.map(item =>
        item.id === id ? { ...item, quantity: item.quantity + delta } : item
      ).filter(item => item.quantity > 0) // Removes if  the quantity zero
    );
  };

  // Styles for navigation links
  const navStyle = {
    background: '#fff', // White bg
    padding: '5px 10px', // Some padding
    borderRadius: '5px', // Rounded corners
    textDecoration: 'none', //  Here is the has no underlines
    color: '#4a90e2', // Blue color
    fontWeight: 'bold', // Bold text
    display: 'flex', // Here is the Flex layout
    alignItems: 'center', // Centering of items
    gap: '5px', // Here is the Spaces between
  };

  // This is the styles for buttons
  const buttonStyle = {
    background: '#4a90e2', // Blue background
    color: '#fff', //  the White text
    border: 'none', //  the No borders
    padding: '8px 12px', //  the  size
    borderRadius: '5px', //  the Rounded
    cursor: 'pointer', //  the hand cursor
  };

  // Styles for input fields
  const inputStyle = {
    display: 'block', //  the Block level
    margin: '10px 0', //  the Some space
    padding: '8px', // the  padding
    borderRadius: '5px', // Rounded
    border: '1px solid #ccc', //the Light border
    width: '250px', // the width
  };

  // The actual rendered app
  return (
    // Router enables client side routing
    <Router>
      {/* This is the main container div */}
      <div style={{ 
        fontFamily: 'Arial, sans-serif', // Nice font
        padding: '20px', // Space around
        background: '#f4f4f4', // Light gray
        minHeight: '100vh' // Full height
      }}>
        {/*  The Header section */}
        <header style={{ 
          background: '#4a90e2', // Blue bg
          color: '#fff', // White text
          padding: '10px 20px', // Padding
          borderRadius: '8px', // Rounded
          marginBottom: '20px' // Space below
        }}>
          {/* HEre is the Home link */}
          <Link to="/" style={{ textDecoration: 'none', color: 'white' }}>
            <h1>📚 Online Bookstore</h1> {/* With  an book emoji */}
          </Link>
          {/* These are the Navigation links */}
          <div style={{ 
            display: 'flex', //  the flex layout
            alignItems: 'center', //  the center
            gap: '10px', // Spaces between
            flexWrap: 'wrap' // Wrap if it is needed
          }}>
            {/* All the nav links are here */}
            <Link style={navStyle} to="/login"><FaSignInAlt /> Login</Link>
            <Link style={navStyle} to="/register">Register</Link>
            <Link style={navStyle} to="/profile"><FaUser /> Profile</Link>
            <Link style={navStyle} to="/order-history">Order History</Link>
            <Link style={navStyle} to="/admin">Admin</Link>
            {/*  the cart is linked with count */}
            <Link style={navStyle} to="/cart">
              <FaShoppingCart /> Cart ({cartItems.reduce((sum, item) => sum + item.quantity, 0)})
            </Link>
            {/* This is the Search bar */}
            <div style={{ 
              display: 'flex', 
              alignItems:'center', 
              background:'#fff', 
              borderRadius:'5px', 
              padding: '5px 10px' 
            }}>
              <FaSearch color="#4a90e2" /> {/* The search icon */}
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

        {/* all our routes/pages */}
        <Routes>
          {/* The home page */}
          <Route path="/" element={
            <Home 
              featuredBooks={searchResults || featuredBooks.filter(book => book.featured)} 
              comingSoonBooks={!searchResults ? comingSoonBooks.filter(book => !book.featured) : []} 
              handleAddToCart={handleAddToCart}
              loading={loading}
              error={error}
              isSearching={!!searchResults}
              searchTerm={search}
            />} 
          />
          {/* These are the Other pages consist of Cartpage,Checkotpage etc.. */}
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

// This is the Home page component
function Home({ featuredBooks, comingSoonBooks, handleAddToCart, loading, error, isSearching, searchTerm }) {
  //  this Style is for the  book cards
  const cardStyle = {
    background: '#fff', // HEre is the White bg
    padding: '15px', // Here is the Padding
    borderRadius: '10px', // Here is the Rounded
    boxShadow: '0 4px 6px rgba(0,0,0,0.1)', //  the shadow
    textAlign: 'center', // Center text
    transition: 'transform 0.2s', // Here is the Animation
    ':hover': {
      transform: 'scale(1.02)' //  the Zoom effect is created
    }
  };

  // implemented to show loading or   the error states
  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Loading books... hang tight!</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Oops! {error}</div>;

  // Thi is the Main home page content
  return (
    <div>
      {/* The featured books section */}
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>
        {isSearching ? `🔍 Search Results for "${searchTerm}"` : '📘 Featured Books'}
      </h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.length > 0 ? featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            {/* The Book cover image */}
            <img 
              src={`/img/${book.coverImage.split('/').pop()}`}
              alt={book.title}
              style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
            />
            {/* Book details */}
            <h3 style={{ margin: '10px 0', fontSize: '1.2em', height: '2.4em', overflow: 'hidden' }}>{book.title}</h3>
            <p style={{ color: '#666', margin: '5px 0' }}>by {book.author}</p>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', margin: '5px 0' }}>
              <FaStar color="#ffd700" />
              <span>{book.rating ? book.rating.toFixed(1) : 'N/A'}</span>
            </div>
            <p style={{ 
              fontWeight: 'bold', 
              color: '#4a90e2', 
              margin: '10px 0',
              fontSize: '1.2em' 
            }}>
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
                transition: 'background 0.2s',
                ':hover': {
                  background: '#357abd'
                }
              }}
            >
              Add to Cart
            </button>
          </div>
        )) : (
          <p>No books found for your search term.</p>
        )}
      </div>

      {/* Coming Soon section */}
      {!isSearching && comingSoonBooks && comingSoonBooks.length > 0 && (
        <>
          <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
            {comingSoonBooks.map(book => (
              <div key={book.id} style={cardStyle}>
                {}
                <img 
                  src={`/img/${book.coverImage.split('/').pop()}`}
                  alt={book.title}
                  style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
                />
                {}
                <h3 style={{ margin: '10px 0', fontSize: '1.2em', height: '2.4em', overflow: 'hidden' }}>{book.title}</h3>
                <p style={{ color: '#666', margin: '5px 0' }}>by {book.author}</p>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '5px', margin: '5px 0' }}>
                  <FaStar color="#ffd700" />
                  <span>{book.rating ? book.rating.toFixed(1) : 'N/A'}</span>
                </div>
                <p style={{ 
                  fontWeight: 'bold', 
                  color: '#50c878', 
                  margin: '10px 0',
                  fontSize: '1.2em' 
                }}>
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

// Shopping Cart Page Component which shows all  of the items user has added to the cart
function CartPage({ cartItems, handleQuantityChange }) {
  // First we would  calculate the total price by multiplying each items price by its quantity and adding them all together
  const total = cartItems.reduce((sum, item) => sum + (item.sellingPrice * item.quantity), 0);

  // Now we will return the actual cart page UI that users will see
  return (
    // This is the Main container div with max width so it doesn't stretch too wide on big screens
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* The Carts title with shopping cart emoji because emojis make everything better */}
      <h2>🛒 Your Cart</h2>
      
      {/* If  the cart is empty,  then show a friendly message encouraging them to  the add items */}
     {cartItems.length === 0 ? (
        <p>Your cart is empty. Maybe add some books?</p>
     ) : (
        /* Otherwisee show all the  of cart items */
        <>
          {/* Loop through each item in the cart array and  then also display it */}
      {cartItems.map(item => (
            // Each item gets its own container with white background and shadow
            <div key={item.id} style={{ 
        display: 'flex', // Using a  flexbox layout
      alignItems: 'center', // Centering the  items vertically
             justifyContent: 'space-between', // Spacing  out the contents
          background: '#fff', // White background
       padding: '15px', // Some inner spacing
         borderRadius: '8px', // Rounded corners because sharp corners are scary
           margin: '10px 0', // Margin on top and bottom
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)' // Subtle shadow for depth
            }}>
              {/* Left side showing book title and author */}
              <div style={{ flex: 2 }}>
                {/* Book title will be in normal font size */}
                <h3 style={{ margin: 0 }}>{item.title}</h3>
                {/* Author's name in kinnd of muted  in gray color */}
               <p style={{ margin: '5px 0', color: '#666' }}>by {item.author}</p>
              </div>
              {/* The Right side  is showing price calculations */}
              <div style={{ flex: 1, textAlign: 'right' }}>
                {/* This shows price per item multiplied by quantity */}
                <p style={{ margin: 0 }}>${item.sellingPrice.toFixed(2)} × {item.quantity}</p>
                {/* This shows total for this specific book */}
               <p style={{ margin: '5px 0', fontWeight: 'bold' }}>${(item.sellingPrice * item.quantity).toFixed(2)}</p>
              </div>
            {/* Quantity adjustment for  the buttons */}
              <div style={{ flex: 0 }}>
                {/* The minus button to decrease quantity - red color for danger */}
                <button 
                  onClick={() => handleQuantityChange(item.id, -1)} // Decrease by 1
                  style={{ 
              background: '#ff4444', // Here is the Red color
      color: 'white', // Here is the White text
                    border: 'none', // created it to have No border
        padding: '5px 10px', //The  Comfortable size
              borderRadius: '4px', //Here is the  Slightly rounded
            cursor: 'pointer', // Here is the Hand cursor on hover
                    marginRight: '5px' // The Space between buttons
                  }}
                >
                  - {/* The Minus symbol */}
                </button>
                {/* The Plus button to increase quantity - blue color matching theme */}
                <button 
                onClick={() => handleQuantityChange(item.id, 1)} // This will Increase by 1
                  style={{ 
               background: '#4a90e2', //  the blue color
                 color: 'white', // collor  White  for the text
                  border: 'none', // there will be no  border
                  padding: '5px 10px', // Same as the  minus button
                    borderRadius: '4px', // Same rounding
                 cursor: 'pointer' // Hand cursor made to pointer
                  }}
                >
                  + {/*This is the  Plus symbol */}
                </button>
              </div>
            </div>
          ))}
          
          {/* Order summary section at the bottom */}
          <div style={{ 
           background: '#fff', // White backgrounds
          padding: '15px', // created an inner spacing
       borderRadius: '8px', // Rounded the  corners
       marginTop: '20px', // Space from  the items above
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)' // Same shadow as  the items
          }}>
            {/* Total price in larger text aligned to right */}
            <h3 style={{ textAlign: 'right' }}>Total: ${total.toFixed(2)}</h3>
            {/* Checkout button container also aligned right */}
            <div style={{ textAlign: 'right' }}>
              {/* Link to  the checkout page */}
            <Link to="/checkout">
                {/* Big green checkout button */}
                <button style={{ 
                  background: '#50c878', // the Green color
             color: 'white', // White text
                 border: 'none', // No border
                 padding: '10px 20px', // Comfortable clickable size
                  borderRadius: '5px', // Rounded corners
               cursor: 'pointer', // Hand cursor
                  fontSize: '1.1em' // Slightly larger text
                }}>
                  Proceed to Checkout {/* Clear call to action */}
                </button>
              </Link>
            </div>
          </div>
        </>
      )}
    </div>
  );
}

// Checkout Page Component - where users enter payment/shipping info
function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [checkoutData, setCheckoutData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const calculateTotal = async () => {
      if (cartItems.length === 0) return;
      
      setLoading(true);
      try {
        // Format cart items for the Java backend
        const cartItemsFormatted = cartItems.map(item => ({
          id: item.id,
          quantity: item.quantity
        }));

        const response = await fetch('http://localhost:8080/api/books/calculate', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(cartItemsFormatted)
        });

        if (!response.ok) {
          throw new Error('Failed to calculate checkout total');
        }

        const data = await response.json();
        setCheckoutData(data);
      } catch (err) {
        setError(err.message);
        console.error('Error calculating checkout:', err);
      } finally {
        setLoading(false);
      }
    };

    calculateTotal();
  }, [cartItems]);

  const handleConfirm = () => {
    // Add order to history
    setOrders(prev => [...prev, { 
      id: Date.now(), 
      items: cartItems,
      total: checkoutData.total,
      date: new Date().toLocaleDateString()
    }]);
    
    // Clear cart
    setCartItems([]);
    
    // Redirect to order history
    window.location.href = '/order-history';
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>Calculating total...</div>;
  }

  if (error) {
    return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;
  }

  if (cartItems.length === 0) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>Your cart is empty</div>;
  }

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <h2>Checkout</h2>
      
      <div style={{ marginBottom: '20px' }}>
        <h3>Order Summary</h3>
        {cartItems.map(item => (
          <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>{item.title} x {item.quantity}</span>
            <span>${(item.sellingPrice * item.quantity).toFixed(2)}</span>
          </div>
        ))}
      </div>

      {checkoutData && (
        <div style={{ borderTop: '1px solid #ccc', paddingTop: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>Subtotal:</span>
            <span>${checkoutData.subtotal.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0' }}>
            <span>Sales Tax:</span>
            <span>${checkoutData.salesTax.toFixed(2)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0', fontWeight: 'bold' }}>
            <span>Total:</span>
            <span>${checkoutData.total.toFixed(2)}</span>
          </div>
          
          <button 
            onClick={handleConfirm}
            style={{
              background: '#4CAF50',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              width: '100%',
              marginTop: '20px'
            }}
          >
            Confirm Order
          </button>
        </div>
      )}
    </div>
  );
}

// Order History Page Component which shows  the past orders books
function OrderHistoryPage({ orders, setCartItems }) {
  // Function to re add items from old order to cart
  const handleReorder = (items) => {
    setCartItems(items); // Sets the  cart to these items
    alert('We added those items back to your cart!'); // Let user know
  };

  return (
    // Container with max width
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* Page title */}
      <h2>Order History</h2>
      
      {/* Shows the  message if  there are no orders */}
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        /* Lists  of all past orders */
        orders.map(order => (
          // Each order in its own container
          <div key={order.id} style={{ 
            background: '#fff',
          padding: '20px',
            borderRadius: '8px',
         marginBottom: '20px',
          boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
          }}>
            {/* Orders header with  the date and also the  total */}
            <div style={{ 
              display: 'flex', 
          justifyContent: 'space-between',
             alignItems: 'center',
              marginBottom: '15px',
          borderBottom: '1px solid #eee',
              paddingBottom: '10px'
            }}>
              <div>
                {/* Th is the order number */}
                <h3 style={{ margin: 0 }}>Order #{order.id}</h3>
                {/* it  Formatted for the  date and status */}
                <p style={{ margin: '5px 0', color: '#666' }}>
                  {new Date(order.date).toLocaleDateString()} • {order.status}
                </p>
              </div>
              {/* The order total amount */}
              <div style={{ textAlign: 'right' }}>
                <p style={{ margin: 0, fontWeight: 'bold', fontSize: '1.2em' }}>
                  ${order.total.toFixed(2)}
                </p>
              </div>
            </div>
            
            {/* List all items in this order */}
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
            
            {/* Reorder button at  the bottom of the books */}
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

// Th is the Login Page Component  thi si swhere  the users can sign in to their account
function LoginPage() {
  // We need state to keep track of what the user types in the email and password fields
    const [email, setEmail] = useState(''); // would  starts empty 
  const [password, setPassword] = useState(''); // Also empty at first

  // When the form gets submitted, this function runs
  const handleSubmit = (e) => {
    e.preventDefault(); // Stop the page from reloading cuz we hate that
    alert(`Logged in as ${email}`); // In a real app we'd call an API here but this is just pretend
  };

  // The actual login form that the  user will end up seeing 
  return (
    // This the container with max width so it doesn't look weird on big screens
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      {/* Page title */}
      <h2>Login</h2>
      {/* The form wrapper with some  styling */}
      <form onSubmit={handleSubmit} style={{ 
      background: '#fff', // White background makes it look clean i feel
       padding: '20px', // Gives room.
        borderRadius: '8px', // Rounded corners.
      boxShadow: '0 2px 5px rgba(0,0,0,0.1)' // Subtle shadow for the  depth
      }}>
        {/*The  email input field with label */}
        <div style={{ marginBottom: '15px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
          <input
            type="email" // Makes sure it is a  a valid email format
           value={email} // an controlled  component
           onChange={(e) => setEmail(e.target.value)} // Updates the  state on typing
            style={{ 
           width: '100%', // Takes  up full width
              padding: '8px', // Padding space
             borderRadius: '4px', // Slightly rounded
              border: '1px solid #ccc' // Light gray border
            }}
            required // Don't let them submit without this here
          />
        </div>
        
        {/* Ths is the Password input field with a  label */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Password:</label>
      <input
            type="password" // Dots instead of letters for secrecy for the user's password
        value={password} // Controlled component
            onChange={(e) => setPassword(e.target.value)} //This will update the  state on typing
         style={{ 
              width: '100%', // here is the full width
           padding: '8px', // Here is the Same as email field
          borderRadius: '4px', // Here is the Consistent rounding
              border: '1px solid #ccc' // This is the Same border used most of implemenations
            }}
            required 
          />
        </div>
        
        {/* The big login button */}
        <button 
          type="submit" // Makes it submit the form
          style={{ 
         background: '#4a90e2', // Same blue as everywhere
            color: 'white', // White text 
        border: 'none', // No borders 
         padding: '10px 15px', // make sure its aa clickable size
          borderRadius: '5px', // it is  rounded
            cursor: 'pointer', // Hand cursor on hover
            width: '100%' //The  full width button
          }}
        >
          Login {/* the simple clear text */}
</button>
    </form>
    </div>
  );
}

// HEre is the  Registration Page Component , where new users can sign up if they like
function RegisterPage() {
  // We need an state for all the registration fields
const [email, setEmail] = useState(''); // Empty at  the start
  const [password, setPassword] = useState(''); // This make it a empty password
  const [confirmPassword, setConfirmPassword] = useState(''); //Make sure it is confirmed

  // When the form gets submitted
        const handleSubmit = (e) => {
    e.preventDefault(); // Stops  that page reload
    // Check if passwords actually match cuz users make mistakes
    if (password !== confirmPassword) {
      alert("Passwords don't match! Try again."); // Scold user to try again
      return; // Don't proceed
    }
  alert(`Registered ${email}`); // In real app would call API here
  };

  // The registration form UI
  return (
    // Same container style as login for consistency
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      {/* This is the Page title */}
      <h2>Register</h2>
      {/* The form wrapper with same styling as login */}
      <form onSubmit={handleSubmit} style={{ 
    background: '#fff',
    padding: '20px',
     borderRadius: '8px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
      }}>
        {/* This is the Email area which  is same as login */}
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
        
        {/*  implementation of tthe password field */}
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
        
        {/* The password confirmation field */}
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
        
        {/* This the  big register button */}
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
          Register {/* Clears the  action text */}
        </button>
      </form>
    </div>
  );
}

// This is the Profile Page Component  where  the users can manage their account
function ProfilePage() {
  // States  for all profile fields with some dummy defaults
  const [name, setName] = useState('your name'); //  here is default name
  const [email, setEmail] = useState('your-email@example.com'); // this is an default email
  const [address, setAddress] = useState('123 Main St, City'); // this is an default address
  const [paymentMethod, setPaymentMethod] = useState('Visa **** 1234'); // The payment that will be used

  // When saving profile changes
  const handleSubmit = (e) => {
    e.preventDefault(); // No page reload 
    alert('Profile updated successfully!'); // In real app would save to backend
  };

  // The profile form UI
  return (
    // Slightly wider container for  the profile information
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      {/* Here is the page title */}
      <h2>Profile</h2>
      {/* Here is the form wrapper */}
      <form onSubmit={handleSubmit} style={{ 
     background: '#fff', // background used
     padding: '20px',   // the padding i created
      borderRadius: '8px', // the radius size
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
      }}>
        {/* This is the full name field */}
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
        
        {/* Email field */}
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
        
        {/* Address field used so the  textarea for multiple lines */}
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
              minHeight: '80px' // I made this taller for the use of of the  addresses
            }}
          />
        </div>
        
        {/* again a payment method field */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment Method:</label>
          <input
            type="text"
        value={paymentMethod}
            onChange={(e) => setPaymentMethod(e.target.value)}
          style={{ 
         width: '100%',       // width for payemnt feild
            padding: '8px',   // the padding for payment feild
          borderRadius: '4px',  // borderradius for payment feild
              border: '1px solid #ccc' // the border for payment feild
            }}
          />
        </div>
        
        {/* Saves the  profile button */}
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
          Save Profile {/* Clears the  action text */}
        </button>
      </form>
    </div>
  );
}

// Admin Dashboard Component implemented  for store management
function AdminPage() {
  return (
    // Wider container for  the dashboard
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* The Page title */}
      <h2>Admin Panel</h2>
      {/* The grid of management tiles */}
      <div style={{ 
        display: 'grid', // Grid layout
        gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', // creation of columns that are responsive
        gap: '20px', //  so there is Spaces between tiles
        marginTop: '20px' //  so there s Spaces below title
      }}>
        {/* Books management tile */}
        <div style={{ 
          background: '#fff', // White bg
        padding: '20px', // Inner space
         borderRadius: '8px', // Rounded
        boxShadow: '0 2px 5px rgba(0,0,0,0.1)', // Shadow
          textAlign: 'center' // Center content
        }}>
          <h3>Manage Books</h3> {/* Tile title */}
          <p>Add, edit, or remove books from the store</p> {/* Description */}
          {/* Link to books management */}
          <Link to="/admin/books">
            <button style={{ 
             background: '#4a90e2',  // background for when manging books
             color: 'white',     // color of page when user selects mananging books
             border: 'none',    // thi sis the border 
          padding: '8px 15px',  // this is the padding 
            borderRadius: '5px',   // this is the borderr adius for manage books
             cursor: 'pointer',   // make the cursor a pointer
              marginTop: '10px' // Space above the  button
            }}>
              Go to Books {/* Action text */}
            </button>
          </Link>
        </div>
        
        {/* Users management tile */}
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
        
        {/* This is the  Orders management tile */}
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
        
        {/* This is the Reports tile */}
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