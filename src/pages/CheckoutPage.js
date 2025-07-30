import React, { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import CartService from '../CartService';

export default function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [checkoutData, setCheckoutData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [userData, setUserData] = useState(null);
  const [selectedPaymentCard, setSelectedPaymentCard] = useState(null);
  const [selectedBillingAddress, setSelectedBillingAddress] = useState(null);
  const [selectedShippingAddress, setSelectedShippingAddress] = useState(null);
  const [useManualEntry, setUseManualEntry] = useState(false);
  const [cardInfo, setCardInfo] = useState({
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    cardholderName: ''
  });
  const [billingInfo, setBillingInfo] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: ''
  });
  const [shippingInfo, setShippingInfo] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: ''
  });
  const [promoCode, setPromoCode] = useState('');
  const [appliedPromo, setAppliedPromo] = useState(null);
  const [promoError, setPromoError] = useState('');
  const [promoLoading, setPromoLoading] = useState(false);
  const { auth } = useAuth();

  useEffect(() => {
    const calculateTotal = async () => {
      if (cartItems.length === 0) return;

      setLoading(true);
      try {
        const cartItemsFormatted = cartItems.map(item => ({ id: item.id, quantity: item.quantity }));
        const response = await fetch('http://localhost:8080/api/books/calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(cartItemsFormatted)
        });

        if (!response.ok) throw new Error('Failed to calculate checkout total');
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

  // Fetch user's stored payment and address data
  useEffect(() => {
    const fetchUserData = async () => {
      if (!auth.isLoggedIn) return;

      try {
        const response = await fetch('http://localhost:8080/api/checkout/user-data', {
          method: 'GET',
          credentials: 'include'
        });

        if (response.ok) {
          const data = await response.json();
          setUserData(data);
          
          // Auto-select first available options if they exist
          if (data.paymentCards && data.paymentCards.length > 0) {
            setSelectedPaymentCard(data.paymentCards[0]);
          }
          if (data.shippingAddresses && data.shippingAddresses.length > 0) {
            setSelectedShippingAddress(data.shippingAddresses[0]);
          }
          // Find billing address for the selected payment card
          if (data.paymentCards && data.paymentCards.length > 0 && data.billingAddresses) {
            const billingAddr = data.billingAddresses.find(addr => 
              addr.addressID === data.paymentCards[0].billingAddressID
            );
            if (billingAddr) {
              setSelectedBillingAddress(billingAddr);
            }
          }
        }
      } catch (err) {
        console.error('Error fetching user data:', err);
      }
    };

    fetchUserData();
  }, [auth.isLoggedIn]);

  const isCardInfoValid = () => {
    if (useManualEntry || !auth.isLoggedIn) {
      return cardInfo.cardNumber.length >= 16 && 
             cardInfo.expiryDate.length >= 5 && 
             cardInfo.cvv.length >= 3 && 
             cardInfo.cardholderName.trim().length > 0;
    } else {
      return selectedPaymentCard && selectedBillingAddress && selectedShippingAddress;
    }
  };

  const isAddressInfoValid = () => {
    if (useManualEntry || !auth.isLoggedIn) {
      return billingInfo.street.trim().length > 0 && 
             billingInfo.city.trim().length > 0 && 
             billingInfo.state.trim().length > 0 && 
             billingInfo.zipCode.trim().length > 0 &&
             shippingInfo.street.trim().length > 0 && 
             shippingInfo.city.trim().length > 0 && 
             shippingInfo.state.trim().length > 0 && 
             shippingInfo.zipCode.trim().length > 0;
    } else {
      return selectedBillingAddress && selectedShippingAddress;
    }
  };

  const handleCardInfoChange = (field, value) => {
    setCardInfo(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleApplyPromo = async () => {
    if (!promoCode.trim()) {
      setPromoError('Please enter a promo code');
      return;
    }

    setPromoLoading(true);
    setPromoError('');

    try {
      const response = await fetch('http://localhost:8080/api/promotions');
      if (!response.ok) throw new Error('Failed to fetch promotions');
      
      const promotions = await response.json();
      const validPromo = promotions.find(promo => 
        promo.promoCode === promoCode.toUpperCase() && 
        new Date(promo.startDate) <= new Date() && 
        new Date(promo.endDate) >= new Date()
      );

      if (validPromo) {
        setAppliedPromo(validPromo);
        setPromoError('');
      } else {
        setPromoError('Invalid or expired promo code');
        setAppliedPromo(null);
      }
    } catch (err) {
      setPromoError('Error applying promo code');
      console.error('Promo error:', err);
    } finally {
      setPromoLoading(false);
    }
  };

  const calculateDiscountedTotal = () => {
    if (!checkoutData || !appliedPromo) return checkoutData?.total || 0;
    
    const discountAmount = (checkoutData.subtotal * appliedPromo.discount) / 100;
    return checkoutData.subtotal - discountAmount;
  };

  const handleConfirm = async () => {
    if (!isCardInfoValid() || !isAddressInfoValid()) {
      setError('Please fill in all required information before proceeding');
      return;
    }

    try {
      const finalTotal = appliedPromo ? calculateDiscountedTotal() : checkoutData.total;
      
      // Prepare checkout data
      const checkoutPayload = {
        cartItems: cartItems,
        totalAmount: finalTotal,
        paymentInfo: useManualEntry || !auth.isLoggedIn ? cardInfo : selectedPaymentCard,
        billingAddress: useManualEntry || !auth.isLoggedIn ? billingInfo : selectedBillingAddress,
        shippingAddress: useManualEntry || !auth.isLoggedIn ? shippingInfo : selectedShippingAddress
      };

      // If user is logged in, send to backend for processing and email
      if (auth.isLoggedIn) {
        const response = await fetch('http://localhost:8080/api/checkout/process', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(checkoutPayload)
        });

        if (!response.ok) {
          throw new Error('Failed to process checkout');
        }

        const result = await response.json();
        console.log('Checkout processed:', result);

        // Save order to database
        const orderData = {
          cartItems: cartItems,
          totalAmount: finalTotal,
          appliedPromo: appliedPromo
        };

        const orderResponse = await fetch('http://localhost:8080/api/orders', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(orderData)
        });

        if (!orderResponse.ok) {
          console.error('Failed to save order to database');
        }
      }
      
      setOrders(prev => [...prev, {
        id: Date.now(),
        items: cartItems,
        total: finalTotal,
        appliedPromo: appliedPromo,
        date: new Date().toLocaleDateString()
      }]);

      // Clear cart based on authentication status
      if (auth.isLoggedIn) {
        await CartService.clearAuthenticatedCart();
      } else {
        CartService.clearGuestCart();
      }
      
      setCartItems([]);
      window.location.href = '/order-history';
    } catch (error) {
      console.error('Error processing checkout:', error);
      setError('Failed to process checkout. Please try again.');
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Calculating total...</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;
  if (cartItems.length === 0) return <div style={{ textAlign: 'center', padding: '20px' }}>Your cart is empty</div>;

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
          {appliedPromo && (
            <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0', color: 'green' }}>
              <span>Discount ({appliedPromo.promoCode} - {appliedPromo.discount}%):</span>
              <span>-${((checkoutData.subtotal * appliedPromo.discount) / 100).toFixed(2)}</span>
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between', margin: '10px 0', fontWeight: 'bold' }}>
            <span>Total:</span>
            <span>${(appliedPromo ? calculateDiscountedTotal() : checkoutData.total).toFixed(2)}</span>
          </div>

          {/* Promo Code Section */}
          <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
            <h4>Promo Code</h4>
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
              <input
                type="text"
                placeholder="Enter promo code"
                value={promoCode}
                onChange={(e) => setPromoCode(e.target.value.toUpperCase())}
                style={{
                  padding: '8px',
                  border: '1px solid #ccc',
                  borderRadius: '3px',
                  flex: 1
                }}
              />
              <button
                onClick={handleApplyPromo}
                disabled={promoLoading || !promoCode.trim()}
                style={{
                  padding: '8px 15px',
                  backgroundColor: '#2196F3',
                  color: 'white',
                  border: 'none',
                  borderRadius: '3px',
                  cursor: promoLoading || !promoCode.trim() ? 'not-allowed' : 'pointer',
                  opacity: promoLoading || !promoCode.trim() ? 0.6 : 1
                }}
              >
                {promoLoading ? 'Applying...' : 'Apply Promo'}
              </button>
            </div>
            {promoError && <p style={{ color: 'red', margin: '5px 0' }}>{promoError}</p>}
            {appliedPromo && (
              <p style={{ color: 'green', margin: '5px 0' }}>
                Promo code "{appliedPromo.promoCode}" applied! {appliedPromo.discount}% discount
              </p>
            )}
          </div>

          {/* Payment and Address Selection for Logged-in Users */}
          {auth.isLoggedIn && userData && (
            <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h4>Payment & Address Information</h4>
                <label style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  <input
                    type="checkbox"
                    checked={useManualEntry}
                    onChange={(e) => setUseManualEntry(e.target.checked)}
                  />
                  Enter information manually
                </label>
              </div>

              {!useManualEntry && (
                <>
                  {/* Stored Payment Cards */}
                  {userData.paymentCards && userData.paymentCards.length > 0 && (
                    <div style={{ marginBottom: '15px' }}>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Select Payment Card:</label>
                      <select
                        value={selectedPaymentCard?.cardID || ''}
                        onChange={(e) => {
                          const cardId = parseInt(e.target.value);
                          const card = userData.paymentCards.find(c => c.cardID === cardId);
                          setSelectedPaymentCard(card);
                          
                          // Auto-select associated billing address
                          if (card && userData.billingAddresses) {
                            const billingAddr = userData.billingAddresses.find(addr => 
                              addr.addressID === card.billingAddressID
                            );
                            setSelectedBillingAddress(billingAddr);
                          }
                        }}
                        style={{
                          padding: '8px',
                          border: '1px solid #ccc',
                          borderRadius: '3px',
                          width: '100%'
                        }}
                      >
                        <option value="">Select a payment card</option>
                        {userData.paymentCards.map(card => (
                          <option key={card.cardID} value={card.cardID}>
                            {card.maskedCardNo} ({card.type}) - Expires {card.expirationDate}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  {/* Stored Shipping Addresses */}
                  {userData.shippingAddresses && userData.shippingAddresses.length > 0 && (
                    <div style={{ marginBottom: '15px' }}>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Select Shipping Address:</label>
                      <select
                        value={selectedShippingAddress?.addressID || ''}
                        onChange={(e) => {
                          const addressId = parseInt(e.target.value);
                          const address = userData.shippingAddresses.find(a => a.addressID === addressId);
                          setSelectedShippingAddress(address);
                        }}
                        style={{
                          padding: '8px',
                          border: '1px solid #ccc',
                          borderRadius: '3px',
                          width: '100%'
                        }}
                      >
                        <option value="">Select a shipping address</option>
                        {userData.shippingAddresses.map(address => (
                          <option key={address.addressID} value={address.addressID}>
                            {address.street}, {address.city}, {address.state} {address.zipCode}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  {/* Display selected addresses */}
                  {selectedBillingAddress && (
                    <div style={{ marginBottom: '10px', padding: '10px', backgroundColor: '#e8f5e8', borderRadius: '3px' }}>
                      <strong>Billing Address:</strong><br />
                      {selectedBillingAddress.street}<br />
                      {selectedBillingAddress.city}, {selectedBillingAddress.state} {selectedBillingAddress.zipCode}
                    </div>
                  )}

                  {selectedShippingAddress && (
                    <div style={{ marginBottom: '10px', padding: '10px', backgroundColor: '#e8f5e8', borderRadius: '3px' }}>
                      <strong>Shipping Address:</strong><br />
                      {selectedShippingAddress.street}<br />
                      {selectedShippingAddress.city}, {selectedShippingAddress.state} {selectedShippingAddress.zipCode}
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {/* Manual Entry or Guest Checkout - Card Information Section */}
          {(useManualEntry || !auth.isLoggedIn || !userData) && (
            <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
              <h4>Payment Information</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '10px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>Card Number*</label>
                  <input
                    type="text"
                    placeholder="1234 5678 9012 3456"
                    maxLength="19"
                    value={cardInfo.cardNumber}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
                      handleCardInfoChange('cardNumber', value);
                    }}
                    style={{
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '3px',
                      width: '100%'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>Cardholder Name*</label>
                  <input
                    type="text"
                    placeholder="John Doe"
                    value={cardInfo.cardholderName}
                    onChange={(e) => handleCardInfoChange('cardholderName', e.target.value)}
                    style={{
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '3px',
                      width: '100%'
                    }}
                  />
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>Expiry Date*</label>
                  <input
                    type="text"
                    placeholder="MM/YY"
                    maxLength="5"
                    value={cardInfo.expiryDate}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      const formatted = value.length >= 2 ? `${value.slice(0, 2)}/${value.slice(2, 4)}` : value;
                      handleCardInfoChange('expiryDate', formatted);
                    }}
                    style={{
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '3px',
                      width: '100%'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>CVV*</label>
                  <input
                    type="text"
                    placeholder="123"
                    maxLength="4"
                    value={cardInfo.cvv}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      handleCardInfoChange('cvv', value);
                    }}
                    style={{
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '3px',
                      width: '100%'
                    }}
                  />
                </div>
              </div>
            </div>
          )}

          {/* Manual Entry - Address Information Section */}
          {(useManualEntry || !auth.isLoggedIn || !userData) && (
            <>
              <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
                <h4>Billing Address</h4>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px', marginBottom: '10px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Street*</label>
                    <input
                      type="text"
                      placeholder="123 Main St"
                      value={billingInfo.street}
                      onChange={(e) => setBillingInfo(prev => ({ ...prev, street: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>City*</label>
                    <input
                      type="text"
                      placeholder="City"
                      value={billingInfo.city}
                      onChange={(e) => setBillingInfo(prev => ({ ...prev, city: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>State*</label>
                    <input
                      type="text"
                      placeholder="State"
                      value={billingInfo.state}
                      onChange={(e) => setBillingInfo(prev => ({ ...prev, state: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>ZIP Code*</label>
                    <input
                      type="text"
                      placeholder="12345"
                      value={billingInfo.zipCode}
                      onChange={(e) => setBillingInfo(prev => ({ ...prev, zipCode: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                </div>
              </div>

              <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
                <h4>Shipping Address</h4>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px', marginBottom: '10px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>Street*</label>
                    <input
                      type="text"
                      placeholder="123 Main St"
                      value={shippingInfo.street}
                      onChange={(e) => setShippingInfo(prev => ({ ...prev, street: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>City*</label>
                    <input
                      type="text"
                      placeholder="City"
                      value={shippingInfo.city}
                      onChange={(e) => setShippingInfo(prev => ({ ...prev, city: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>State*</label>
                    <input
                      type="text"
                      placeholder="State"
                      value={shippingInfo.state}
                      onChange={(e) => setShippingInfo(prev => ({ ...prev, state: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: '5px' }}>ZIP Code*</label>
                    <input
                      type="text"
                      placeholder="12345"
                      value={shippingInfo.zipCode}
                      onChange={(e) => setShippingInfo(prev => ({ ...prev, zipCode: e.target.value }))}
                      style={{
                        padding: '8px',
                        border: '1px solid #ccc',
                        borderRadius: '3px',
                        width: '100%'
                      }}
                    />
                  </div>
                </div>
              </div>
            </>
          )}

          {/* Validation Messages */}
          {(!isCardInfoValid() || !isAddressInfoValid()) && (
            <p style={{ color: 'red', margin: '10px 0', fontSize: '14px' }}>
              * Please complete all required information to proceed with checkout
            </p>
          )}

          <button
            onClick={handleConfirm}
            disabled={!isCardInfoValid() || !isAddressInfoValid()}
            style={{
              background: (isCardInfoValid() && isAddressInfoValid()) ? '#4CAF50' : '#ccc',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '5px',
              cursor: (isCardInfoValid() && isAddressInfoValid()) ? 'pointer' : 'not-allowed',
              width: '100%',
              marginTop: '20px'
            }}
          >
            {(isCardInfoValid() && isAddressInfoValid()) ? 'Confirm Order' : 'Please Complete Required Information'}
          </button>
        </div>
      )}
    </div>
  );
}
  

