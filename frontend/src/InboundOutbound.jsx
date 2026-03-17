import React, { useState, useEffect } from 'react';
import axios from 'axios';

const InboundOutbound = ({ user }) => {
    const [subTab, setSubTab] = useState('inbound');
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);

    const [formData, setFormData] = useState({
        reference: '',
        dateReceived: '',
        dateShipped: '',
        productSku: '',
        quantity: 0,
        location: '',   
        destination: '',
        remarks: ''
    });

    const canManage = user?.role?.toUpperCase().includes("USER") || 
                        user?.role?.toUpperCase().includes("ADMIN") || 
                        user?.role?.toUpperCase().includes("MANAGER");

    const fetchData = async () => {
        setLoading(true);
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = { 
                headers: { 
                    Authorization: `Bearer ${token}` 
                } 
            };
            const endpoint = subTab === 'inbound' ? '/inbound/get-all' : '/outbound/get-all';
            const res = await axios.get(`http://localhost:8080${endpoint}`, config);
            const list = res.data.listOfInbound || res.data.listOfOutbound || [];
            setData(Array.isArray(list) ? list : []);
        } catch (err) {
            console.error("Fetch error:", err);
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [subTab]);

    const handleAddMovement = async (e) => {
        e.preventDefault();
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = { 
                headers: { 
                    Authorization: `Bearer ${token}` 
                } 
            };
            const endpoint = subTab === 'inbound' ? '/inbound/register' : '/outbound/register';
            const response = await axios.post(`http://localhost:8080${endpoint}`, formData, config);
            if(response.data.statusCode == 200) {
                alert(`${subTab.toUpperCase()} successfully recorded!`);
                setShowModal(false);
                resetForm();
                fetchData();
            } else {
                alert(`Failed to Add ${subTab.toUpperCase()}`)
                setShowModal(false);
            }
        } catch (err) {
            alert("Error: " + (err.response?.data?.message || "Failed to save record"));
        }
    };

    const resetForm = () => {
        setFormData({
            reference: '', dateReceived: '', dateShipped: '',
            productSku: '', quantity: 0, location: '',
            destination: '', remarks: ''
        });
    };

    return (
        <div className="animate-fade-in">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div className="btn-group shadow-sm">
                    <button 
                        className={`btn ${subTab === 'inbound' ? 'btn-primary' : 'btn-outline-primary'}`}
                        onClick={() => setSubTab('inbound')}>
                        <i className="bi bi-download me-2"></i>Inbound Logs
                    </button>
                    <button 
                        className={`btn ${subTab === 'outbound' ? 'btn-primary' : 'btn-outline-primary'}`}
                        onClick={() => setSubTab('outbound')}>
                        <i className="bi bi-upload me-2"></i>Outbound Logs
                    </button>
                </div>

                {/* This is how to change the ADD BUTTON based on tab */}
                {canManage && (
                    <button 
                        className={`btn ${subTab === 'inbound' ? 'btn-success' : 'btn-warning text-dark'}`}
                        onClick={() => { resetForm(); setShowModal(true); }}>
                        <i className="bi bi-plus-circle me-2"></i>
                        Add {subTab === 'inbound' ? 'Inbound' : 'Outbound'}
                    </button>
                )}
            </div>

            <div className="table-responsive border rounded bg-white shadow-sm">
                <table className="table table-hover mb-0">
                    <thead className={subTab === 'inbound' ? "table-success" : "table-warning"}>
                        <tr>
                            <th>Reference</th>
                            <th>Date {subTab === 'inbound' ? 'Received' : 'Shipped'}</th>
                            <th>Product SKU</th>
                            <th>Quantity</th>
                            <th>{subTab === 'inbound' ? 'Location' : 'Destination'}</th>
                            <th>Remarks</th>
                        </tr>
                    </thead>
                    <tbody>
                        {!loading && data.length > 0 ? data.map(item => (
                            <tr key={item.id}>
                                <td className="fw-bold">{item.reference}</td>
                                <td>{item.dateReceived || item.dateShipped || '-'}</td>
                                <td><span className="badge bg-light text-dark border">{item.productSku || item.inventory?.sku}</span></td>
                                <td className={subTab === 'inbound' ? "text-success fw-bold" : "text-danger fw-bold"}>
                                    {subTab === 'inbound' ? '+' : '-'}{item.quantity}
                                </td>
                                <td>{item.location || item.destination || '-'}</td>
                                <td className="small text-muted">{item.remarks}</td>
                            </tr>
                        )) : (
                            <tr><td colSpan="6" className="text-center py-4 text-muted">
                                {loading ? 'Loading records...' : `No ${subTab} records found.`}
                            </td></tr>
                        )}
                    </tbody>
                </table>
            </div>

            {showModal && (
                <div className="custom-modal-overlay">
                    <div className="custom-modal-content card p-4 shadow-lg border-0" style={{maxWidth: '500px'}}>
                        <h4 className="fw-bold mb-4">New {subTab === 'inbound' ? 'Inbound Receipt' : 'Outbound Shipment'}</h4>
                        <form onSubmit={handleAddMovement}>
                            <div className="row g-3">
                                <div className="col-12">
                                    <label className="form-label small fw-bold">Reference Number</label>
                                    <input type="text" className="form-control" placeholder="REF-0000"
                                        value={formData.reference} onChange={(e) => setFormData({...formData, reference: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Date {subTab === 'inbound' ? 'Received' : 'Shipped'}</label>
                                    <input type="date" className="form-control" required
                                        value={subTab === 'inbound' ? formData.dateReceived : formData.dateShipped} 
                                        onChange={(e) => setFormData({...formData, [subTab === 'inbound' ? 'dateReceived' : 'dateShipped']: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Product SKU</label>
                                    <input type="text" className="form-control" required placeholder="SKU Code"
                                        value={formData.productSku} onChange={(e) => setFormData({...formData, productSku: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Quantity</label>
                                    <input type="number" className="form-control" required min="1"
                                        value={formData.quantity} onChange={(e) => setFormData({...formData, quantity: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">{subTab === 'inbound' ? 'Storage Location' : 'Destination'}</label>
                                    <input type="text" className="form-control" required placeholder={subTab === 'inbound' ? "e.g. Aisle 5" : "e.g. Customer Name"}
                                        value={subTab === 'inbound' ? formData.location : formData.destination} 
                                        onChange={(e) => setFormData({...formData, [subTab === 'inbound' ? 'location' : 'destination']: e.target.value})} />
                                </div>
                                <div className="col-12">
                                    <label className="form-label small fw-bold">Remarks</label>
                                    <textarea className="form-control" rows="2"
                                        value={formData.remarks} onChange={(e) => setFormData({...formData, remarks: e.target.value})}></textarea>
                                </div>
                            </div>
                            <div className="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
                                <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className={`btn ${subTab === 'inbound' ? 'btn-success' : 'btn-warning px-4'}`}>
                                    Save {subTab}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default InboundOutbound;
