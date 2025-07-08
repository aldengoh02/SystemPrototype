// The Importing stuff we need , these are like our tools for building the app
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'; // Used for switching pages
import { FaUser, FaShoppingCart, FaSignInAlt, FaSearch, FaStar } from 'react-icons/fa'; // Pretty icons
import { useState, useEffect } from 'react'; // Basic React hooks we gotta have

// Main App component - the big boss of our whole application
export default function App() {
  // For the search bar - remembers what you type
  const [search, setSearch] = useState(''); // Starts empty obviously
  
  // Where we keep all the books in your shopping cart
  const [cartItems, setCartItems] = useState([]); // Empty array at first
  
  // All the past orders people have made
  const [orders, setOrders] = useState([]); // Also empty at start
  
  // Featured books from our database
  const [featuredBooks, setFeaturedBooks] = useState([]); // Will fill this later
  
  // Books that aren't out yet but we show them anyway
  const [comingSoonBooks, setComingSoonBooks] = useState([]); // Gotta fetch these too
  
  // Shows loading spinner when waiting for data
  const [loading, setLoading] = useState(true); // True at first cuz we loading
  
  // If something breaks, we store the error here
  const [error, setError] = useState(null); // Starts as null cuz no errors yet

  // This runs when page loads to get our book data
  useEffect(() => {
    // Special async function to fetch books
    const fetchBooks = async () => {
      try {
        setLoading(true); // Show loading spinner
        
        // Get both types of books at same time (faster this way)
        const [featuredResponse, comingSoonResponse] = await Promise.all([
          fetch('http://localhost:5000/api/books/featured'), // Main books
          fetch('http://localhost:5000/api/books/coming-soon') // Coming soon
        ]);

        // Checks to see  if both requests worked
        if (!featuredResponse.ok || !comingSoonResponse.ok) {
          throw new Error('Oops server said no to our books'); // Error message
        }

        // Turn  the responses into JSON we can use
        const [featuredData, comingSoonData] = await Promise.all([
          featuredResponse.json(), // Convert to JSON
          comingSoonResponse.json() // This one too
        ]);

        // Updates our state  with the book data
        setFeaturedBooks(featuredData); // Sets the  featured books
        setComingSoonBooks(comingSoonData); // Sets the  coming soon
      } catch (err) {
        // If anything goes wrong
        setError(err.message); // Saves error message
        console.error('Dang error getting books:', err); // Logs it
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
      // Checks  if a  book already in the  cart
      const existing = prev.find(item => item.id === book.id);
      if (existing) {
        // If yes  then just add 1 to the  quantity
        return prev.map(item => 
          item.id === book.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      } else {
        // If new then add to cart with quantity 1
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
              alignItems: 'center', 
              background: '#fff', 
              borderRadius: '5px', 
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
              featuredBooks={featuredBooks} 
              comingSoonBooks={comingSoonBooks} 
              handleAddToCart={handleAddToCart}
              loading={loading}
              error={error}
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
function Home({ featuredBooks, comingSoonBooks, handleAddToCart, loading, error }) {
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
      <h2 style={{ borderBottom: '3px solid #4a90e2', paddingBottom: '5px', color: '#4a90e2' }}>📘 Featured Books</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px', marginBottom: '30px' }}>
        {featuredBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            {/* The  Book cover image */}
            {book.coverImage && (
              <img 
                src={`http://localhost:5000/img/${book.coverImage.split('/').pop()}`} 
                alt={book.title}
                style={{ width: '100%', height: '300px', objectFit: 'cover', borderRadius: '5px' }}
                onError={(e) => {
                  e.target.src = 'http://localhost:5000/img/placeholder.jpg'; // Fallback just incase.
                }}
              />
            )}
            {/* Thi is the Book informtion */}
            <h3 style={{ margin: '10px 0' }}>{book.title}</h3>
            <p style={{ color: '#666' }}>by {book.author}</p>
            {/* The star rating */}
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
            {/* These are the price and  the button */}
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

      {/* HEre is is the coming soon section */}
      <h2 style={{ borderBottom: '3px solid #50c878', paddingBottom: '5px', color: '#50c878' }}>📗 Coming Soon</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
        {comingSoonBooks.map(book => (
          <div key={book.id} style={cardStyle}>
            {/* This is the Book cover */}
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
            {/* The book information that shows an title, author and shows book imagee */}
            <h3 style={{ margin: '10px 0' }}>{book.title}</h3>
   <p style={{ color: '#666' }}>by {book.author}</p>
     <p style={{ color: '#50c878', fontWeight: 'bold' }}><i>Coming Soon</i></p>
          </div>
        ))}
      </div>
    </div>
  );
}

// Shopping Cart Page Component which shows all  of the items user has added to the cart
function CartPage({ cartItems, handleQuantityChange }) {
  // First we would  calculate the total price by multiplying each items price by its quantity and adding them all together
  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

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
        display: 'flex', // Use flexbox layout
      alignItems: 'center', // Center items vertically
             justifyContent: 'space-between', // Space out the contents
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
                {/* Author's name in slightly muted  in gray color */}
                <p style={{ margin: '5px 0', color: '#666' }}>by {item.author}</p>
              </div>
              {/* The Right side  is showing price calculations */}
              <div style={{ flex: 1, textAlign: 'right' }}>
                {/* This shows price per item multiplied by quantity */}
                <p style={{ margin: 0 }}>${item.price} × {item.quantity}</p>
                {/* This shows total for this specific book */}
                <p style={{ margin: '5px 0', fontWeight: 'bold' }}>${(item.price * item.quantity).toFixed(2)}</p>
              </div>
              {/* Quantity adjustment  the buttons */}
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
  // State for all the checkout form fields with default values
  const [paymentInfo, setPaymentInfo] = useState('Visa **** 1234'); // Payment method
  const [address, setAddress] = useState('123 Main St, City'); // Shipping address
  const [email, setEmail] = useState('user@example.com'); // Contact email
  const [confirmed, setConfirmed] = useState(false); // Order confirmation status

  // Calculate the  order's totals.  The math is done  here so users don't have to
  const total = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0); // Subtotal
  const tax = total * 0.07; //  implnented a 7% tax adjust if your bookstore is tax free!
  const grandTotal = total + tax; // What they actually will  pay

  // When  the user confirms their order
  const handleConfirm = () => {
    // Create new order object with all necessary info
    const newOrder = {
      id: Date.now(), // Simple unique ID using timestamp
      items: [...cartItems], // Copy of cart items
      total: grandTotal, // Final amount charged
      date: new Date().toISOString(), // When order was placed
      status: 'Completed' // Initial status
    };
    
    // Update orders array and clear the cart
    setOrders(prev => [...prev, newOrder]); // Add new order to history
    setCartItems([]); // Empty the cart
    setConfirmed(true); // Show confirmation message
  };

  // The checkout page UI
  return (
    // Container with max width for better readability
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      {/* Page title */}
      <h2>Checkout</h2>
      
      {/* Show either checkout form or confirmation message */}
      {!confirmed ? (
        /* Checkout form */
        <>
          {/* Shipping information section */}
          <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
            <h3>Shipping Information</h3>
            {/* Email field with change button */}
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{email}</span>
                {/* Button to update email */}
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
            
            {/* Address field with change button */}
            <div style={{ marginBottom: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Shipping Address:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{address}</span>
                {/* Button to update address */}
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
            
            {/* Payment method field with change button */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Payment Method:</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ flex: 1 }}>{paymentInfo}</span>
                {/* Button to update payment info */}
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
          
          {/* Th is the rOrder summary section */}
          <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', marginBottom: '20px' }}>
            <h3>Order Summary</h3>
            {/* List  of all cart items */}
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
            
            {/* Here is the Subtotal line */}
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              padding: '10px 0',
              borderBottom: '1px solid #eee'
            }}>
              <span>Subtotal:</span>
              <span>${total.toFixed(2)}</span>
            </div>
            
            {/* Here is the Tax line */}
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              padding: '10px 0',
              borderBottom: '1px solid #eee'
            }}>
              <span>Tax (7%):</span>
              <span>${tax.toFixed(2)}</span>
            </div>
            
            {/* Grand total line - more prominent */}
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
          
          {/* Big confirm order button */}
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
        /* Order confirmation message */
        <div style={{ 
          background: '#fff', 
          padding: '30px', 
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          {/* Big checkmark for visual confirmation */}
          <div style={{ 
            fontSize: '4em',
            color: '#50c878',
            marginBottom: '20px'
          }}>
            ✓
          </div>
          {/* Confirmation heading */}
          <h2>Order Confirmed!</h2>
          {/* Personalized confirmation message */}
          <p style={{ fontSize: '1.2em', marginBottom: '30px' }}>
            Thank you for your purchase! We've sent a confirmation to {email}.
          </p>
          {/* Button to return to shopping */}
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

// Order History Page Component which shows past orders
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
      
      {/* Show message if no orders */}
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        /* List  of all past orders */
        orders.map(order => (
          // Each order in its own container
          <div key={order.id} style={{ 
            background: '#fff',
            padding: '20px',
            borderRadius: '8px',
            marginBottom: '20px',
            boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
          }}>
            {/* Order header with  the date and also the  total */}
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
      background: '#fff', // White background makes it look clean
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
        
        {/* Ths is the Password input field with label */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Password:</label>
      <input
            type="password" // Dots instead of letters for secrecy for the user
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
         padding: '10px 15px', // clickable size
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
        {/* This is the Email area which same as login */}
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
          Register {/* Clears the  call to action */}
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
    e.preventDefault(); // No page reload plz
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