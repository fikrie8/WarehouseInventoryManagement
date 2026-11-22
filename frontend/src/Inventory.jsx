import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { validateExcelHeader } from './utilities/ExcelValidation';
import { EXCEL_TEMPLATES } from './config/excelTemplates';
import { prepareExcelUploadPayload } from './utilities/UploadExcelHandler';

const Inventory = ({ user }) => {
    const [inventory, setInventory] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);
    const [newInventory, setNewInventory] = useState({ sku: '', category: '', name: '', location: '', quantity: 0, supplier: '' });

    const [showUploadModal, setShowUploadModal] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [uploadStatus, setUploadStatus] = useState({ type: '', message: ''});

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
            const response = await axios.get("/api/inventory/get-all-inventory", config);
            if(response.data.statusCode === 200) {
                setInventory(response.data.listOfInventoryDTO || []);
            }
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
            const response = await axios.post("/api/inventory/register-inventory/single-upload", newInventory, config);
            if(response.data.statusCode === 200) {
                alert("Inventory successfully registered!");
                setShowAddModal(false);
                setNewInventory({ sku: '', category: '', name: '', location: '', quantity: 0, supplier: '' }); 
                fetchInventory();
            }
        } catch (err) {
            const errorMessage = getErrorMessage(err);
            alert("Failed to add inventory, Server error : " + errorMessage);
            console.error("Failed to add inventory. Error details: " + errorMessage);
        }
    };

    const handleUploadInventoryByExcel = async (e) => {
        e.preventDefault();
        if (!selectedFile) return;

        try {
            setUploading(true);
            setUploadStatus({ type: '', message: '' });

            // 💡 1. Let the utility handle the Excel validation and package building
            const { formData } = await prepareExcelUploadPayload(selectedFile, EXCEL_TEMPLATES.INVENTORY);

            // 💡 2. The component takes the output bundle and executes its local API post call
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = {
                headers: { Authorization: `Bearer ${token}` }
            };

            const response = await axios.post("/api/inventory/register-inventory/bulk-upload", formData, config);
            
            if (response.data?.statusCode === 200 || response.status === 200) {
                setUploadStatus({ type: "success", message: "🎉 Bulk data imported successfully!" });
                setSelectedFile(null);
                fetchInventory(); // Sync the active table values
                
                setTimeout(() => {
                    setShowUploadModal(false);
                    setUploadStatus({ type: "", message: "" });
                }, 2500);
            }
        } catch (err) {
            // Automatically captures template mismatches from the utility OR API errors from Axios
            const errorMessage = getErrorMessage(err);
            setUploadStatus({ 
                type: 'danger', 
                message: errorMessage 
            });
        } finally {
            setUploading(false);
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
                        <div className="d-flex gap-2">
                            <button className="btn btn-outline-success d-flex align-items-center gap-1" 
                                onClick={() => setShowUploadModal(true)}>
                                + Import Excel
                            </button>
                            <button className="btn btn-success"
                                onClick={() => setShowAddModal(true)}>
                                + Add Inventory
                            </button>
                        </div>
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
        
            {/* Bulk Excel File Import Modal */}
            {showUploadModal && (
            <div className="custom-modal-overlay">
                <div className="custom-modal-content card p-4 shadow-lg border-0" style={{ maxWidth: '480px' }}>
                
                {/* Modal Header */}
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h4 className="fw-bold mb-0">Bulk Spreadsheet Import</h4>
                    <button 
                        type="button"
                        className="btn-close" 
                        disabled={uploading} 
                        onClick={() => { 
                            setShowUploadModal(false); 
                            setUploadStatus({ type: '', message: '' }); 
                            setSelectedFile(null); 
                        }}
                    ></button>
                </div>
                
                {/* Modal Body / Instructions */}
                <p className="text-muted small mb-4">
                    Upload a standard Excel spreadsheet (<code className="text-dark">.xlsx</code> or <code className="text-dark">.xls</code>) 
                    containing the following ordered data layout columns:
                    <strong className="d-block mt-1 text-dark">No, SKU Code, Category, Inventory Name, Location, Quantity, Supplier</strong>
                </p>

                {/* Form Content */}
                <form onSubmit={handleUploadInventoryByExcel}>
                    <div className="mb-3">
                        <label className="form-label small fw-bold">Choose Excel File</label>
                        <input 
                            type="file"
                            className="form-control"
                            accept=".xlsx, .xls"
                            disabled={uploading}
                            required
                            onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                        />
                    </div>

                    {/* Status Alert messaging blocks dynamically mapping colors via bootstrap classes */}
                    {uploadStatus.message && (
                        <div className={`alert alert-${uploadStatus.type} small py-2 my-3`} role="alert">
                            {uploadStatus.message}
                        </div>
                    )}

                    {/* Modal Footer Actions */}
                    <div className="d-flex justify-content-end gap-2 mt-4">
                        <button 
                            type="button"
                            className="btn btn-secondary"
                            disabled={uploading}
                            onClick={() => { 
                                setShowUploadModal(false); 
                                setUploadStatus({ type: '', message: '' }); 
                                setSelectedFile(null); 
                            }}
                        >
                            Cancel
                        </button>
                        
                        <button 
                            type="submit"
                            className="btn btn-primary d-flex align-items-center gap-2"
                            disabled={!selectedFile || uploading}
                        >
                            {uploading ? (
                                <>
                                    <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                                    Processing...
                                </>
                            ) : (
                                'Upload & Process All'
                            )}
                        </button>
                    </div>
                </form>
                </div>
            </div>
            )}
        </div>
    );
};

export default Inventory;
