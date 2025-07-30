import React, { useState, useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function AdminPromotionsPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [promotions, setPromotions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pushModalOpen, setPushModalOpen] = useState(false);
  const [pushMessage, setPushMessage] = useState('');
  const [sendingPush, setSendingPush] = useState(false);
  const [selectedPromotion, setSelectedPromotion] = useState(null);

  useEffect(() => {
    fetchPromotions();
  }, []);

  const fetchPromotions = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/promotions', {
        credentials: 'include'
      });
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
        method: 'DELETE',
        credentials: 'include'
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

  const handlePushPromotion = (promotion) => {
    setSelectedPromotion(promotion);
    setPushModalOpen(true);
    setPushMessage('');
    setError(null);
  };

  const handlePushSubmit = async (e) => {
    e.preventDefault();
    
    if (!pushMessage.trim()) {
      setError('Message is required');
      return;
    }

    setSendingPush(true);
    
    try {
      const response = await fetch(`http://localhost:8080/api/promotions/push/${selectedPromotion.promoID}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          message: pushMessage
        })
      });
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.error || 'Failed to push promotion');
      }
      
      setPushModalOpen(false);
      alert('Promotion pushed successfully!');
      
      // Refresh promotions to update pushed status
      await fetchPromotions();
      
    } catch (err) {
      setError('Error pushing promotion: ' + err.message);
      console.error('Error pushing promotion:', err);
    } finally {
      setSendingPush(false);
    }
  };

  const handlePushModalClose = () => {
    setPushModalOpen(false);
    setPushMessage('');
    setSelectedPromotion(null);
    setError(null);
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
        <div style={{ display: 'flex', gap: '10px' }}>
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
                  <th style={tableHeaderStyle}>Push Status</th>
                  <th style={tableHeaderStyle}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {promotions.map((promotion) => {
                  const statusInfo = getPromotionStatus(promotion.startDate, promotion.endDate);
                  const isActive = statusInfo.status === 'Active';
                  const isPushed = promotion.pushed;
                  const canEdit = !(isPushed && isActive);
                  const canDelete = !(isPushed && isActive);
                  
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
                        <span style={{
                          background: isPushed ? '#28a745' : '#6c757d',
                          color: 'white',
                          padding: '4px 8px',
                          borderRadius: '4px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {isPushed ? 'Pushed' : 'Not Pushed'}
                        </span>
                      </td>
                      <td style={tableCellStyle}>
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                          {!isPushed && (
                            <button
                              onClick={() => handlePushPromotion(promotion)}
                              style={{
                                background: '#17a2b8',
                                color: 'white',
                                border: 'none',
                                padding: '6px 12px',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '12px'
                              }}
                            >
                              Push to Actions
                            </button>
                          )}
                          <button
                            onClick={() => handleEditPromotion(promotion.promoID)}
                            disabled={!canEdit}
                            style={{
                              background: canEdit ? '#ffc107' : '#6c757d',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: canEdit ? 'pointer' : 'not-allowed',
                              fontSize: '12px',
                              opacity: canEdit ? 1 : 0.6
                            }}
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeletePromotion(promotion.promoID)}
                            disabled={!canDelete}
                            style={{
                              background: canDelete ? '#dc3545' : '#6c757d',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: canDelete ? 'pointer' : 'not-allowed',
                              fontSize: '12px',
                              opacity: canDelete ? 1 : 0.6
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
      
      {/* Push Modal */}
      {pushModalOpen && selectedPromotion && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '10px',
            width: '500px',
            maxWidth: '90vw',
            maxHeight: '80vh',
            overflow: 'auto'
          }}>
            <h3>Push Promotion to Actions</h3>
            <div style={{ marginBottom: '20px', padding: '15px', background: '#f8f9fa', borderRadius: '5px' }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#495057' }}>Promotion Details:</h4>
              <p style={{ margin: '5px 0' }}><strong>Code:</strong> {selectedPromotion.promoCode}</p>
              <p style={{ margin: '5px 0' }}><strong>Discount:</strong> {selectedPromotion.discount}%</p>
              <p style={{ margin: '5px 0' }}><strong>Valid:</strong> {formatDate(selectedPromotion.startDate)} - {formatDate(selectedPromotion.endDate)}</p>
            </div>
            <form onSubmit={handlePushSubmit}>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Message for customers:
                </label>
                <textarea
                  value={pushMessage}
                  onChange={(e) => setPushMessage(e.target.value)}
                  placeholder="Enter a message to customers about this promotion..."
                  rows="6"
                  style={{
                    width: '100%',
                    padding: '10px',
                    border: '1px solid #ddd',
                    borderRadius: '5px',
                    fontSize: '14px',
                    resize: 'vertical'
                  }}
                  required
                />
                <small style={{ color: '#666', fontSize: '12px' }}>
                  This message will be included in the promotional email along with the promotion code and dates.
                </small>
              </div>
              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                <button
                  type="button"
                  onClick={handlePushModalClose}
                  style={{
                    background: '#6c757d',
                    color: 'white',
                    border: 'none',
                    padding: '10px 20px',
                    borderRadius: '5px',
                    cursor: 'pointer'
                  }}
                  disabled={sendingPush}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  style={{
                    background: '#17a2b8',
                    color: 'white',
                    border: 'none',
                    padding: '10px 20px',
                    borderRadius: '5px',
                    cursor: sendingPush ? 'not-allowed' : 'pointer',
                    opacity: sendingPush ? 0.6 : 1
                  }}
                  disabled={sendingPush}
                >
                  {sendingPush ? 'Pushing...' : 'Push to Actions'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
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