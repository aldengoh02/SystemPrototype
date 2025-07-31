import React, { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import CartService from '../CartService';

export default function CheckoutPage({ cartItems, setCartItems, setOrders }) {
  const [checkoutData, setCheckoutData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [userData, setUserData] = useState(null);
  const [selectedPaymentCardId, setSelectedPaymentCardId] = useState('');
  const [selectedPaymentCard, setSelectedPaymentCard] = useState(null);
  const [selectedBillingAddress, setSelectedBillingAddress] = useState(null);
  const [selectedShippingAddress, setSelectedShippingAddress] = useState(null);
  const [useManualEntry, setUseManualEntry] = useState(false);
  const [cardInfo, setCardInfo] = useState({
    cardNo: '',
    type: '',
    expirationDate: '',
    billingStreet: '',
    billingCity: '',
    billingState: '',
    billingZipCode: ''
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
  const [successMessage, setSuccessMessage] = useState('');


  useEffect(() => {
    const calculateTotal = async () => {
      if (cartItems.length === 0) return;
      setLoading(true);
      try {
        const cartItemsFormatted = cartItems.map(item => ({ id: item.id, quantity: item.quantity }));
        const response = await fetch('http://localhost:8080/api/books/calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
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
        const userID = parseInt(localStorage.getItem('userID'));
        const response = await fetch(`http://localhost:8080/api/user/${userID}`, {
          method: 'GET',
          credentials: 'include'
        });

        if (response.ok) {
          const data = await response.json();
          const userObj = data.user || data;
          let paymentCards;
          if (data.paymentCards) {
            paymentCards = data.paymentCards.map(card => ({
              cardNo: card.cardNo,
              type: card.cardType,
              expirationDate: card.expirationDate,
              cardID: card.cardID,
              billingAddressID: card.billingAddressID
            }));
          } else if (userObj.cards) {
            paymentCards = userObj.cards;
          } else {
            paymentCards = [];
          }
          let billingAddresses;
          if (data.billingAddresses) {
            billingAddresses = data.billingAddresses;
          } else if (data.user && data.user.billingAddresses) {
            billingAddresses = data.user.billingAddresses;
          } else {
            billingAddresses = [];
          }
          let shippingAddresses;
          if (data.shippingAddresses) {
            shippingAddresses = data.shippingAddresses;
          } else if (data.user && data.user.shippingAddresses) {
            shippingAddresses = data.user.shippingAddresses;
          } else {
            shippingAddresses = [];
          }
          setUserData({ paymentCards, billingAddresses, shippingAddresses });

          // Set defaults for all three selectors if not already set
          if (paymentCards.length > 0 && !selectedPaymentCard) {
            setSelectedPaymentCardId(paymentCards[0].cardID);
            setSelectedPaymentCard(paymentCards[0]);
            const billingAddr = billingAddresses.find(
              addr => addr.addressID === paymentCards[0].billingAddressID
            );
            if (billingAddr) setSelectedBillingAddress(billingAddr);
          }
          if (shippingAddresses.length > 0 && !selectedShippingAddress) {
            setSelectedShippingAddress(shippingAddresses[0]);
            setShippingInfo({
              street: shippingAddresses[0].street,
              city: shippingAddresses[0].city,
              state: shippingAddresses[0].state,
              zipCode: shippingAddresses[0].zipCode
            });
          }
        }
      } catch (err) {
        console.error('Error fetching user data:', err);
      }
    };

    fetchUserData();
  }, [auth.isLoggedIn]);

  // Reapply defaults when userData changes (fixes validation bug if data loads after render)
  useEffect(() => {
    if (userData) {
      if (userData.paymentCards && userData.paymentCards.length > 0 && !selectedPaymentCard) {
        setSelectedPaymentCardId(userData.paymentCards[0].cardID);
        setSelectedPaymentCard(userData.paymentCards[0]);
        const billingAddr = userData.billingAddresses.find(
          addr => addr.addressID === userData.paymentCards[0].billingAddressID
        );
        if (billingAddr) setSelectedBillingAddress(billingAddr);
      }
      if (userData.shippingAddresses && userData.shippingAddresses.length > 0 && !selectedShippingAddress) {
        setSelectedShippingAddress(userData.shippingAddresses[0]);
        setShippingInfo({
          street: userData.shippingAddresses[0].street,
          city: userData.shippingAddresses[0].city,
          state: userData.shippingAddresses[0].state,
          zipCode: userData.shippingAddresses[0].zipCode
        });
      }
    }
  }, [userData]);

  // Always use saved card info if selected, regardless of manual entry toggle
  const isCardInfoValid = () => {
    // Saved card mode
    if (!useManualEntry && selectedPaymentCard && selectedBillingAddress && shippingInfo.street && shippingInfo.city && shippingInfo.state && shippingInfo.zipCode) {
      return true;
    }
    // Manual entry mode
    if (useManualEntry) {
      return (
        cardInfo.cardNo.length >= 16 &&
        cardInfo.type.trim().length > 0 &&
        cardInfo.expirationDate.length >= 5 &&
        cardInfo.billingStreet.trim().length > 0 &&
        cardInfo.billingCity.trim().length > 0 &&
        cardInfo.billingState.trim().length > 0 &&
        cardInfo.billingZipCode.trim().length > 0
      );
    }
    return false;
  };

  const handleCardInfoChange = (field, value) => {
    setCardInfo(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleShippingInfoChange = (field, value) => {
    setShippingInfo(prev => ({
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

  // ----------- FIXED handleConfirm function -----------
  const handleConfirm = async () => {
    if (!isCardInfoValid()) {
      setError('Please fill in all required information before proceeding');
      return;
    }
    try {
      const finalTotal = appliedPromo ? calculateDiscountedTotal() : checkoutData.total;
      let paymentInfoToSend, billingAddressToSend, shippingAddressToSend;
      // Saved card mode
      if (!useManualEntry && selectedPaymentCard && selectedBillingAddress) {
        paymentInfoToSend = {
          cardID: selectedPaymentCard.cardID,
          maskedCardNo: '**** **** **** ' + selectedPaymentCard.cardNo.slice(-4),
          type: selectedPaymentCard.type,
          expirationDate: selectedPaymentCard.expirationDate,
          billingAddressID: selectedPaymentCard.billingAddressID
        };
        billingAddressToSend = selectedBillingAddress;
        shippingAddressToSend = shippingInfo;
      } else { // Manual entry mode
        paymentInfoToSend = {
          cardNumber: cardInfo.cardNo,
          cardType: cardInfo.type,
          expirationDate: cardInfo.expirationDate
        };
        billingAddressToSend = {
          street: cardInfo.billingStreet,
          city: cardInfo.billingCity,
          state: cardInfo.billingState,
          zipCode: cardInfo.billingZipCode
        };
        shippingAddressToSend = null;
      }

      const checkoutPayload = {
        cartItems: cartItems,
        totalAmount: finalTotal,
        paymentInfo: paymentInfoToSend,
        billingAddress: billingAddressToSend,
        ...(shippingAddressToSend !== null && { shippingAddress: shippingAddressToSend })
      };

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
      if (auth.isLoggedIn) {
        await CartService.clearAuthenticatedCart();
      } else {
        CartService.clearGuestCart();
      }
      setCartItems([]);
      setSuccessMessage('Your order has been successfully placed! Please check your email for a order confirmation.');
      window.scrollTo({ top: 0, behavior: 'smooth' });
      setTimeout(() => {
        window.location.href = '/order-history';
      }, 3000);
    } catch (error) {
      console.error('Error processing checkout:', error);
      setError('Failed to process checkout. Please try again.');
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '20px' }}>Calculating total...</div>;
  if (error) return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;
  if (cartItems.length === 0 && !successMessage) return <div style={{ textAlign: 'center', padding: '20px' }}>Your cart is empty</div>;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', padding: '20px' }}>
      <h2>Checkout</h2>
      {successMessage && (
        <div style={{ backgroundColor: '#e6ffe6', color: '#2e7d32', padding: '10px', borderRadius: '5px', marginBottom: '15px', textAlign: 'center' }}>
          {successMessage}
        </div>
      )}

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
          {/* Payment Selection UI */}
          {auth.isLoggedIn && userData && (
            <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
              <h4>Payment Method</h4>
              <div style={{ marginBottom: '10px' }}>
                <label>
                  <input
                    type="radio"
                    name="paymentMethod"
                    checked={!useManualEntry}
                    onChange={() => setUseManualEntry(false)}
                  />{' '}
                  Use a saved card
                </label>
                <label style={{ marginLeft: '20px' }}>
                  <input
                    type="radio"
                    name="paymentMethod"
                    checked={useManualEntry}
                    onChange={() => setUseManualEntry(true)}
                  />{' '}
                  Enter card manually
                </label>
              </div>
              {/* Saved Card Selection */}
              {!useManualEntry && userData.paymentCards && userData.paymentCards.length > 0 && (
                <div style={{ marginBottom: '15px' }}>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Select Payment Card:</label>
                  <select
                    value={selectedPaymentCardId}
                    onChange={(e) => {
                      const cardId = parseInt(e.target.value);
                      setSelectedPaymentCardId(cardId);
                      const card = userData.paymentCards.find(c => c.cardID === cardId);
                      setSelectedPaymentCard(card);
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
                    {userData.paymentCards.map(card => (
                      <option key={card.cardID} value={card.cardID}>
                        {'************' + card.cardNo.slice(-4)} ({card.type}) - Expires {card.expirationDate}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              {/* Saved Shipping Address Input */}
              <div style={{ marginBottom: '15px' }}>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Shipping Address:</label>
                <input
                  type="text"
                  placeholder="Street"
                  value={shippingInfo.street}
                  onChange={e => handleShippingInfoChange('street', e.target.value)}
                  style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '3px', width: '100%', marginBottom: '5px' }}
                />
                <input
                  type="text"
                  placeholder="City"
                  value={shippingInfo.city}
                  onChange={e => handleShippingInfoChange('city', e.target.value)}
                  style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '3px', width: '100%', marginBottom: '5px' }}
                />
                <input
                  type="text"
                  placeholder="State"
                  value={shippingInfo.state}
                  onChange={e => handleShippingInfoChange('state', e.target.value)}
                  style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '3px', width: '100%', marginBottom: '5px' }}
                />
                <input
                  type="text"
                  placeholder="Zip Code"
                  value={shippingInfo.zipCode}
                  onChange={e => handleShippingInfoChange('zipCode', e.target.value)}
                  style={{ padding: '8px', border: '1px solid #ccc', borderRadius: '3px', width: '100%' }}
                />
              </div>
              {!useManualEntry && selectedBillingAddress && (
                <div style={{ marginBottom: '10px', padding: '10px', backgroundColor: '#e8f5e8', borderRadius: '3px' }}>
                  <strong>Billing Address:</strong><br />
                  {selectedBillingAddress.street}<br />
                  {selectedBillingAddress.city}, {selectedBillingAddress.state} {selectedBillingAddress.zipCode}
                </div>
              )}
            </div>
          )}
          {/* Manual Entry UI */}
          {(useManualEntry || !auth.isLoggedIn || !userData) && (
            <div style={{ margin: '20px 0', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
              <h4>Card Information</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '10px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>Card Number*</label>
                  <input
                    type="text"
                    placeholder="1234 5678 9012 3456"
                    maxLength="19"
                    value={cardInfo.cardNo}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
                      handleCardInfoChange('cardNo', value);
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
                  <label style={{ display: 'block', marginBottom: '5px' }}>Card Type*</label>
                  <input
                    type="text"
                    placeholder="Visa, Mastercard, etc."
                    value={cardInfo.type}
                    onChange={(e) => handleCardInfoChange('type', e.target.value)}
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
                  <label style={{ display: 'block', marginBottom: '5px' }}>Expiration Date*</label>
                  <input
                    type="text"
                    placeholder="MM/YY"
                    maxLength="5"
                    value={cardInfo.expirationDate}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      const formatted = value.length >= 2 ? `${value.slice(0, 2)}/${value.slice(2, 4)}` : value;
                      handleCardInfoChange('expirationDate', formatted);
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
              <h4 style={{ marginTop: '20px' }}>Billing Address</h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px', marginBottom: '10px' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px' }}>Street*</label>
                  <input
                    type="text"
                    placeholder="123 Main St"
                    value={cardInfo.billingStreet}
                    onChange={(e) => handleCardInfoChange('billingStreet', e.target.value)}
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
                    value={cardInfo.billingCity}
                    onChange={(e) => handleCardInfoChange('billingCity', e.target.value)}
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
                    value={cardInfo.billingState}
                    onChange={(e) => handleCardInfoChange('billingState', e.target.value)}
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
                    value={cardInfo.billingZipCode}
                    onChange={(e) => handleCardInfoChange('billingZipCode', e.target.value)}
                    style={{
                      padding: '8px',
                      border: '1px solid #ccc',
                      borderRadius: '3px',
                      width: '100%'
                    }}
                  />
                </div>
              </div>
              {/* Removed extra shipping address for manual entry as requested */}
            </div>
          )}
          {!isCardInfoValid() && (
            <p style={{ color: 'red', margin: '10px 0', fontSize: '14px' }}>
              * Please complete all required information to proceed with checkout
            </p>
          )}
          <button
            onClick={handleConfirm}
            disabled={!isCardInfoValid()}
            style={{
              background: isCardInfoValid() ? '#4CAF50' : '#ccc',
              color: 'white',
              padding: '10px 20px',
              border: 'none',
              borderRadius: '5px',
              cursor: isCardInfoValid() ? 'pointer' : 'not-allowed',
              width: '100%',
              marginTop: '20px'
            }}
          >
            {isCardInfoValid() ? 'Confirm Order' : 'Please Complete Required Information'}
          </button>
        </div>
      )}
    </div>
  );
}