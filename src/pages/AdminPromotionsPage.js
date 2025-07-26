import React, { useState, useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function AdminPromotionsPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPromotions();
  }, []);

  const fetchPromotions = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/promotions');
      if (!response.ok) throw new Error('Failed to fetch promotions');
      const data = await response.json();
      setPromotions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
      console.error('Error fetching promotions:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddPromotionClick = () => {
    navigate('/admin/promotions/add');
  };

  const handleEditPromotion = (promotionId) => {
    navigate(`/admin/promotions/edit/${promotionId}`);
  };

  const handleDeletePromotion = async (promotionId) => {
    if (!window.confirm('Are you sure you want to delete this promotion?')) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/promotions/${promotionId}`, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to delete promotion');
      }
      
      // Refresh the promotions list
      await fetchPromotions();
    } catch (err) {
      setError('Error deleting promotion: ' + err.message);
      console.error('Error deleting promotion:', err);
    }
  };

  // Move the redirect inside the render, after all hooks
  if (auth.userRole !== 'admin') {
    return <Navigate to="/home" />;
  }

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '50px' }}>Loading promotions...</div>;
  }

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  const isPromotionActive = (startDate, endDate) => {
    const now = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);
    return now >= start && now <= end;
  };

  const getPromotionStatus = (startDate, endDate) => {
    const now = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);
    
    if (now < start) return { status: 'Upcoming', color: '#ffc107' };
    if (now > end) return { status: 'Expired', color: '#dc3545' };
    return { status: 'Active', color: '#28a745' };
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Manage Promotions</h2>
        <button
          onClick={handleAddPromotionClick}
          style={{
            background: '#4a90e2',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '5px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}
        >
          Add Promotion
        </button>
      </div>

      {error && (
        <div style={{
          background: '#f8d7da',
          color: '#721c24',
          padding: '10px',
          borderRadius: '5px',
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          Error: {error}
        </div>
      )}

      <div style={{ background: '#fff', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 5px rgba(0,0,0,0.1)' }}>
        <div style={{ padding: '20px', borderBottom: '1px solid #ddd' }}>
          <h3>Promotions ({promotions.length})</h3>
        </div>
        
        {promotions.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            No promotions found. Add your first promotion!
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f8f9fa' }}>
                  <th style={tableHeaderStyle}>ID</th>
                  <th style={tableHeaderStyle}>Promo Code</th>
                  <th style={tableHeaderStyle}>Discount (%)</th>
                  <th style={tableHeaderStyle}>Start Date</th>
                  <th style={tableHeaderStyle}>End Date</th>
                  <th style={tableHeaderStyle}>Status</th>
                  <th style={tableHeaderStyle}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {promotions.map((promotion) => {
                  const statusInfo = getPromotionStatus(promotion.startDate, promotion.endDate);
                  return (
                    <tr key={promotion.promoID} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={tableCellStyle}>{promotion.promoID}</td>
                      <td style={tableCellStyle}>
                        <strong>{promotion.promoCode}</strong>
                      </td>
                      <td style={tableCellStyle}>{promotion.discount}%</td>
                      <td style={tableCellStyle}>{formatDate(promotion.startDate)}</td>
                      <td style={tableCellStyle}>{formatDate(promotion.endDate)}</td>
                      <td style={tableCellStyle}>
                        <span style={{
                          background: statusInfo.color,
                          color: 'white',
                          padding: '4px 8px',
                          borderRadius: '4px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {statusInfo.status}
                        </span>
                      </td>
                      <td style={tableCellStyle}>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            onClick={() => handleEditPromotion(promotion.promoID)}
                            style={{
                              background: '#ffc107',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeletePromotion(promotion.promoID)}
                            style={{
                              background: '#dc3545',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

const tableHeaderStyle = {
  padding: '12px',
  textAlign: 'left',
  fontWeight: 'bold',
  borderBottom: '2px solid #ddd'
};

const tableCellStyle = {
  padding: '12px',
  borderBottom: '1px solid #eee'
};