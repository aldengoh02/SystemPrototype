import React, { useState, useEffect } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function EditPromotionPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [updateMessage, setUpdateMessage] = useState('');
  const [formData, setFormData] = useState({
    promoCode: '',
    discount: 0,
    startDate: '',
    endDate: ''
  });
  const [initialLoad, setInitialLoad] = useState(true);

  useEffect(() => {
    fetchPromotion();
  }, [id]);

  // Redirect if not admin
  if (auth.userRole !== 'admin') {
    return <Navigate to="/home" />;
  }

  const fetchPromotion = async () => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8080/api/promotions/${id}`, {
        credentials: 'include'
      });
      if (!response.ok) {
        throw new Error('Failed to fetch promotion');
      }
      const promotion = await response.json();
      
      // Format dates for input fields
      const formatDateForInput = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toISOString().split('T')[0];
      };

      setFormData({
        promoCode: promotion.promoCode || '',
        discount: promotion.discount || 0,
        startDate: formatDateForInput(promotion.startDate),
        endDate: formatDateForInput(promotion.endDate)
      });
    } catch (err) {
      setError('Error loading promotion: ' + err.message);
      console.error('Error fetching promotion:', err);
    } finally {
      setLoading(false);
      setInitialLoad(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? parseFloat(value) || 0 : value
    }));
  };

  const handleUpdatePromotion = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);

      // Validate form data
      if (!formData.promoCode.trim()) {
        throw new Error('Promo code is required');
      }
      if (formData.discount <= 0 || formData.discount > 100) {
        throw new Error('Discount must be between 1 and 100 percent');
      }
      if (!formData.startDate) {
        throw new Error('Start date is required');
      }
      if (!formData.endDate) {
        throw new Error('End date is required');
      }
      if (new Date(formData.startDate) >= new Date(formData.endDate)) {
        throw new Error('End date must be after start date');
      }

      const response = await fetch(`http://localhost:8080/api/promotions/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || `Failed to update promotion: ${response.status}`);
      }

      const result = await response.json();
      setUpdateMessage('Promotion updated successfully! Redirecting to promotions page...');

      setTimeout(() => {
        navigate('/admin/promotions');
      }, 2000);

    } catch (err) {
      setError('Error updating promotion: ' + err.message);
      console.error('Error updating promotion:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/admin/promotions');
  };

  if (initialLoad && loading) {
    return <div style={{ textAlign: 'center', padding: '50px' }}>Loading promotion...</div>;
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', padding: '20px' }}>
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={handleCancel}
          style={{
            background: '#6c757d',
            color: 'white',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '14px',
            marginBottom: '10px'
          }}
        >
          ← Back to Promotions
        </button>
        <h2>Edit Promotion</h2>
      </div>

      {updateMessage && (
        <div style={{
          background: '#d4edda',
          color: '#155724',
          padding: '15px',
          borderRadius: '5px',
          marginBottom: '20px',
          border: '1px solid #c3e6cb'
        }}>
          {updateMessage}
        </div>
      )}

      {error && (
        <div style={{
          background: '#f8d7da',
          color: '#721c24',
          padding: '15px',
          borderRadius: '5px',
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          {error}
        </div>
      )}

      <div style={{
        background: '#fff',
        padding: '30px',
        borderRadius: '8px',
        boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
      }}>
        <form onSubmit={handleUpdatePromotion}>
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Promo Code *
            </label>
            <input
              type="text"
              name="promoCode"
              value={formData.promoCode}
              onChange={handleInputChange}
              required
              placeholder="e.g., SAVE20, WINTER25"
              style={{ 
                width: '100%', 
                padding: '12px', 
                border: '1px solid #ddd', 
                borderRadius: '4px',
                fontSize: '16px'
              }}
            />
            <small style={{ color: '#666', fontSize: '14px' }}>
              Enter a unique promotional code (letters and numbers only)
            </small>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Discount Percentage *
            </label>
            <input
              type="number"
              name="discount"
              value={formData.discount}
              onChange={handleInputChange}
              required
              min="1"
              max="100"
              step="0.1"
              placeholder="e.g., 20"
              style={{ 
                width: '100%', 
                padding: '12px', 
                border: '1px solid #ddd', 
                borderRadius: '4px',
                fontSize: '16px'
              }}
            />
            <small style={{ color: '#666', fontSize: '14px' }}>
              Enter discount percentage (1-100%)
            </small>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Start Date *
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleInputChange}
                required
                style={{ 
                  width: '100%', 
                  padding: '12px', 
                  border: '1px solid #ddd', 
                  borderRadius: '4px',
                  fontSize: '16px'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                End Date *
              </label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleInputChange}
                required
                min={formData.startDate}
                style={{ 
                  width: '100%', 
                  padding: '12px', 
                  border: '1px solid #ddd', 
                  borderRadius: '4px',
                  fontSize: '16px'
                }}
              />
            </div>
          </div>

          <div style={{ marginBottom: '20px', padding: '15px', background: '#f8f9fa', borderRadius: '5px' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#495057' }}>Promotion Preview</h4>
            <p style={{ margin: '5px 0', color: '#6c757d' }}>
              <strong>Code:</strong> {formData.promoCode || 'Not set'}
            </p>
            <p style={{ margin: '5px 0', color: '#6c757d' }}>
              <strong>Discount:</strong> {formData.discount}% off
            </p>
            <p style={{ margin: '5px 0', color: '#6c757d' }}>
              <strong>Valid:</strong> {formData.startDate && formData.endDate 
                ? `${new Date(formData.startDate).toLocaleDateString()} - ${new Date(formData.endDate).toLocaleDateString()}` 
                : 'Dates not set'}
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              type="submit"
              disabled={loading}
              style={{
                background: loading ? '#ccc' : '#ffc107',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '5px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '16px',
                fontWeight: 'bold'
              }}
            >
              {loading ? 'Updating Promotion...' : 'Update Promotion'}
            </button>
            <button
              type="button"
              onClick={handleCancel}
              disabled={loading}
              style={{
                background: '#6c757d',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '5px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '16px'
              }}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}