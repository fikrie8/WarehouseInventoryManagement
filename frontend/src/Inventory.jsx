import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Inventory = ({ user }) => {
    const [inventory, setInventory] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);
    const [newInventory, setNewInventory] = useState({ sku: '', category: '', name: '', location: '', quantity: 0, supplier: '' });

    // 1. DIAGNOSTIC LOGS
    // console.log("Current User Object:", user);
    // console.log("Current User Role:", user?.role);

    /*Need to do checking for role cause different role should have different function*/
    const role = user?.role?.toUpperCase() || "";
    const isPowerUser = role.includes("ADMIN") || role.includes("MANAGER");

    const fetchInventory = async () => {
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = {
                headers: { 
                    Authorization: `Bearer ${token}` 
                }
            };
            
            /*Still trying to find out why no ListOfInventory is displayed*/
            const response = await axios.get("http://localhost:8080/inventory/get-all-inventory", config);
            console.log("testing getListOfInventory" + response.data.listOfInventory);
            setInventory(response.data.listOfInventory || []);
            console.log();
        } catch (err) {
            console.error("API Error:", err.response?.status, err.message);
        } finally {
            setLoading(false); // ALWAYS stop loading, even on error
        }
    };

    useEffect(() => {
        fetchInventory();
    }, []);

    if (loading) {
        return (
            <div className="text-center p-5">
                <div className="spinner-border text-primary"></div>
                <p className="mt-2">Connecting to Warehouse Server...</p>
            </div>
        );
    }

    const handleAddInventory = async (e) => {
        e.preventDefault();
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = {
                headers: { Authorization: `Bearer ${token}` }
            };
            await axios.post("http://localhost:8080/inventory/register-inventory", newInventory, config);
            
            alert("Inventory successfully registered!");
            setShowAddModal(false);
            setNewInventory({ sku: '', category: '', name: '', location: '', quantity: 0, supplier: '' }); 
            fetchInventory();
        } catch (err) {
            alert("Failed to add inventory: " + (err.response?.data?.message || "Check Server"));
        }
    };

    return (
        <div className="animate-fade-in">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h3>Inventory List</h3>
                <div className="d-flex gap-3">
                    <input 
                        type="text" 
                        className="form-control" 
                        placeholder="Search SKU..." 
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    
                    {isPowerUser && (
                        <button className="btn btn-success" onClick={() => setShowAddModal(true)}>
                            + Add Inventory
                        </button>
                    )}
                </div>
            </div>

            <div className="table-responsive border rounded bg-white">
                <table className="table table-hover mb-0">
                    <thead className="table-dark">
                        <tr>
                            <th>SKU</th>
                            <th>Name</th>
                            <th>Qty</th>
                            <th>Category</th>
                            <th>Supplier</th>
                            <th>Location</th>

                        </tr>
                    </thead>
                    <tbody>
                        {inventory.length > 0 ? inventory.map(p => (
                            <tr key={p.id}>
                                <td>{p.sku}</td>
                                <td>{p.name}</td>
                                <td>{p.quantity}</td>
                                <td>{p.category}</td>
                                <td>{p.supplier}</td>
                                <td>{p.location}</td>
                            </tr>
                        )) : (
                            <tr><td colSpan="3" className="text-center">No Data Found</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
            
            {/* This is the Modal that will Popup when a user press Add Inventory */}
             {showAddModal && (
                <div className="custom-modal-overlay">
                    <div className="custom-modal-content card p-4 shadow-lg border-0" style={{maxWidth: '550px'}}>
                        <div className="d-flex justify-content-between align-items-center mb-4">
                            <h4 className="fw-bold mb-0">New Inventory Entry</h4>
                            <button className="btn-close" onClick={() => setShowAddModal(false)}></button>
                        </div>
                        
                        <form onSubmit={handleAddInventory}>
                            <div className="row g-3">
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">SKU Code</label>
                                    <input type="text" className="form-control" required 
                                        value={newInventory.sku} onChange={(e) => setNewInventory({...newInventory, sku: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Category</label>
                                    <input type="text" className="form-control" required 
                                        value={newInventory.category} onChange={(e) => setNewInventory({...newInventory, category: e.target.value})} />
                                </div>
                                <div className="col-12">
                                    <label className="form-label small fw-bold">Inventory Name</label>
                                    <input type="text" className="form-control" required 
                                        value={newInventory.name} onChange={(e) => setNewInventory({...newInventory, name: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Location</label>
                                    <input type="text" className="form-control" required 
                                        value={newInventory.location} onChange={(e) => setNewInventory({...newInventory, location: e.target.value})} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small fw-bold">Quantity</label>
                                    <input type="number" className="form-control" required min="0"
                                        value={newInventory.quantity} onChange={(e) => setNewInventory({...newInventory, quantity: e.target.value})} />
                                </div>
                                <div className="col-12">
                                    <label className="form-label small fw-bold">Supplier</label>
                                    <input type="text" className="form-control" required 
                                        value={newInventory.supplier} onChange={(e) => setNewInventory({...newInventory, supplier: e.target.value})} />
                                </div>
                            </div>
                            
                            <div className="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
                                <button type="button" className="btn btn-light" onClick={() => setShowAddModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-success px-4">Register Inventory</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Inventory;
