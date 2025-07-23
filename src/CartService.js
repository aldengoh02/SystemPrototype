const CART_STORAGE_KEY = 'bookstore_cart';

export const CartService = {
  // Get cart from localStorage
  getGuestCart: () => {
    try {
      const cart = localStorage.getItem(CART_STORAGE_KEY);
      return cart ? JSON.parse(cart) : [];
    } catch (error) {
      console.error('[CartService:getGuestCart] Error:', error);
      return [];
    }
  },

  // Save cart to localStorage
  saveGuestCart: (cartItems) => {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cartItems));
    } catch (error) {
      console.error('[CartService:saveGuestCart] Error:', error);
    }
  },

  // Clear guest cart
  clearGuestCart: () => {
    try {
      localStorage.removeItem(CART_STORAGE_KEY);
    } catch (error) {
      console.error('[CartService:clearGuestCart] Error:', error);
    }
  },

  // Add item to guest cart
  addToGuestCart: (book) => {
    try {
      const cart = CartService.getGuestCart();
      const existingItem = cart.find(item => item.id === book.id);

      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        cart.push({ ...book, quantity: 1 });
      }

      CartService.saveGuestCart(cart);
      return cart;
    } catch (error) {
      console.error('[CartService:addToGuestCart] Error:', error);
      return [];
    }
  },

  // Update guest cart item quantity
  updateGuestCartQuantity: (id, delta) => {
    try {
      const cart = CartService.getGuestCart();
      const updatedCart = cart.map(item =>
        item.id === id ? { ...item, quantity: item.quantity + delta } : item
      ).filter(item => item.quantity > 0);

      CartService.saveGuestCart(updatedCart);
      return updatedCart;
    } catch (error) {
      console.error('[CartService:updateGuestCartQuantity] Error:', error);
      return [];
    }
  },

  // Get cart from API (authenticated users)
  getAuthenticatedCart: async () => {
    try {
      const response = await fetch('/api/cart', {
        method: 'GET',
        credentials: 'include',
      });

      if (response.status === 401) {
        const errorMsg = await response.text();
        console.error('[CartService:getAuthenticatedCart] Unauthorized:', errorMsg);
        throw new Error('Unauthorized: Please login to view your cart.');
      }

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('[CartService:getAuthenticatedCart] API Error:', errorMsg);
        throw new Error('Failed to fetch cart: ' + errorMsg);
      }

      return await response.json();
    } catch (error) {
      console.error('[CartService:getAuthenticatedCart] Error:', error);
      throw error;
    }
  },

  // Add item to authenticated user cart
  addToAuthenticatedCart: async (bookId, quantity = 1) => {
    try {
      const response = await fetch('/api/cart', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ bookID: bookId, quantity }),
      });

      if (response.status === 401) {
        const errorMsg = await response.text();
        console.error('[CartService:addToAuthenticatedCart] Unauthorized:', errorMsg);
        throw new Error('Unauthorized: Please login to add items to your cart.');
      }

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('[CartService:addToAuthenticatedCart] API Error:', errorMsg);
        throw new Error('Failed to add to cart: ' + errorMsg);
      }

      return await response.json();
    } catch (error) {
      console.error('[CartService:addToAuthenticatedCart] Error:', error);
      throw error;
    }
  },

  // Update authenticated user cart item
  updateAuthenticatedCart: async (bookId, quantity) => {
    try {
      const response = await fetch('/api/cart', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ bookID: bookId, quantity }),
      });

      if (response.status === 401) {
        const errorMsg = await response.text();
        console.error('[CartService:updateAuthenticatedCart] Unauthorized:', errorMsg);
        throw new Error('Unauthorized: Please login to update your cart.');
      }

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('[CartService:updateAuthenticatedCart] API Error:', errorMsg);
        throw new Error('Failed to update cart: ' + errorMsg);
      }

      return await response.json();
    } catch (error) {
      console.error('[CartService:updateAuthenticatedCart] Error:', error);
      throw error;
    }
  },

  // Clear authenticated user cart
  clearAuthenticatedCart: async () => {
    try {
      const response = await fetch('/api/cart', {
        method: 'DELETE',
        credentials: 'include',
      });

      if (response.status === 401) {
        const errorMsg = await response.text();
        console.error('[CartService:clearAuthenticatedCart] Unauthorized:', errorMsg);
        throw new Error('Unauthorized: Please login to clear your cart.');
      }

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('[CartService:clearAuthenticatedCart] API Error:', errorMsg);
        throw new Error('Failed to clear cart: ' + errorMsg);
      }

      return await response.json();
    } catch (error) {
      console.error('[CartService:clearAuthenticatedCart] Error:', error);
      throw error;
    }
  },

  // Merge guest cart with authenticated cart on login
  mergeGuestCart: async () => {
    try {
      const guestCart = CartService.getGuestCart();
      if (guestCart.length === 0) return;

      const guestCartForAPI = guestCart.map(item => ({
        bookID: item.id,
        quantity: item.quantity
      }));

      console.log('[CartService:mergeGuestCart] Payload:', guestCartForAPI);

      const response = await fetch('/api/cart/merge', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(guestCartForAPI),
      });

      if (response.status === 401) {
        const errorMsg = await response.text();
        console.error('[CartService:mergeGuestCart] Unauthorized:', errorMsg);
        throw new Error('Unauthorized: Please login to merge carts.');
      }

      if (!response.ok) {
        const errorMsg = await response.text();
        console.error('[CartService:mergeGuestCart] API Error:', errorMsg);
        throw new Error('Failed to merge cart: ' + errorMsg);
      }

      CartService.clearGuestCart();

      return await response.json();
    } catch (error) {
      console.error('[CartService:mergeGuestCart] Error:', error);
      throw error;
    }
  },

  // Update cart item quantity based on authentication status
  updateCartQuantity: async (id, delta, isAuthenticated) => {
    try {
      if (isAuthenticated) {
        const cart = await CartService.getAuthenticatedCart();
        const item = cart.find(cartItem => cartItem.id === id);
        if (item) {
          const newQuantity = item.quantity + delta;
          await CartService.updateAuthenticatedCart(id, newQuantity);
        }
        return await CartService.getAuthenticatedCart();
      } else {
        return CartService.updateGuestCartQuantity(id, delta);
      }
    } catch (error) {
      console.error('[CartService:updateCartQuantity] Error:', error);
      if (isAuthenticated) throw error;
      return [];
    }
  },

  // Add item to cart based on authentication status
  addToCart: async (book, isAuthenticated) => {
    try {
      if (isAuthenticated) {
        await CartService.addToAuthenticatedCart(book.id, 1);
        return await CartService.getAuthenticatedCart();
      } else {
        return CartService.addToGuestCart(book);
      }
    } catch (error) {
      console.error('[CartService:addToCart] Error:', error);
      if (isAuthenticated) throw error;
      return [];
    }
  },

  // Get cart based on authentication status
  getCart: async (isAuthenticated) => {
    try {
      if (isAuthenticated) {
        return await CartService.getAuthenticatedCart();
      } else {
        return CartService.getGuestCart();
      }
    } catch (error) {
      console.error('[CartService:getCart] Error:', error);
      if (isAuthenticated) throw error;
      return [];
    }
  }
};

export default CartService;