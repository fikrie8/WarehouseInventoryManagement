import React, { useState, useEffect } from 'react';
import axios from 'axios';

const UserManagement = ({ user, setUser }) => {
    const [allUsers, setAllUsers] = useState([]);
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [editData, setEditData] = useState({ 
        username: user?.username || '', 
        email: user?.email || '' 
    });
    

    useEffect(() => {
        if (user.role === 'ADMIN' || user.role === 'MANAGER') {
            fetchAllUsers();
        }
    }, [user.role]);

    const fetchAllUsers = async () => {
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = { 
                headers: 
                { 
                    Authorization: `Bearer ${token}` 
                } 
            };
            let url = "";
            if (user.role === 'ADMIN') {
                url = `http://localhost:8080/admin/get-user-info/all`;
            } else if (user.role === 'MANAGER') {
                url = `http://localhost:8080/manager/get-user-info/all`;
            }
            const response = await axios.get(url, config);
            setAllUsers(response.data.listOfUsers || []);
        } catch (err) { console.error("Fetch users failed", err); }
    };

    const handleDeleteUser = async (targetId) => {
        if (window.confirm("Are you sure you want to delete this user? This action cannot be undone.")) {
            try {
                const token = JSON.parse(localStorage.getItem("refreshToken"));
                const config = { 
                    headers: {
                         Authorization: `Bearer ${token}` 
                        } 
                };
                await axios.delete(`http://localhost:8080/admin/delete-user/${targetId}`, config);
                alert("User deleted successfully.");
                fetchAllUsers();
            } catch (err) {
                alert("Delete failed: " + (err.response?.data?.message || "Unauthorized"));
            }
        }
    };

    const handleUpdateInfo = async (e) => {
        e.preventDefault();
        try {
            const token = JSON.parse(localStorage.getItem("refreshToken"));
            const config = { 
                headers: { 
                    Authorization: `Bearer ${token}` 
                } 
            };
            if (user.role === 'ADMIN') {
                url = `http://localhost:8080/admin/update-user`;
            } else if (user.role === 'MANAGER') {
                url = `http://localhost:8080/manager/update-user`;
            } else {
                url = `http://localhost:8080/user/update-user`;
            }            
            /*Still trying to figure out why this axios.put keep getting 401 Error while POSTMAN is working just fine*/
            const response = await axios.put(url, editData, config);
            setUser(response.data.user);
            localStorage.setItem("user", JSON.stringify(response.data));
            setShowUpdateModal(false);
            alert("Information updated!");
        } catch (err) { alert("Update failed."); }
    };

    return (
        <div className="animate-fade-in">
            {user.role === 'USER' ? (
                <div className="row justify-content-center py-4">
                    <div className="col-md-6 card p-5 border-0 bg-light text-center shadow-sm">
                        <i className="bi bi-person-circle display-1 text-primary mb-3"></i>
                        <h2 className="fw-bold">{user.username}</h2>
                        <p className="text-muted mb-4">{user.email}</p>
                        <span className="badge bg-primary px-3 py-2 mb-4">{user.role}</span>
                        <div className="d-grid gap-2">
                            <button className="btn btn-primary py-2" onClick={() => setShowUpdateModal(true)}>
                                <i className="bi bi-pencil-square me-2"></i> Update Information
                            </button>
                        </div>
                    </div>
                </div>
            ) : (
                /* This table/view is only applicable for other role beside USER. (which is MANAGER & ADMIN role)*/
                <div>
                    <div className="d-flex justify-content-between align-items-center mb-4">
                        <h4>{user.role} View : User Directory</h4>
                        <button className="btn btn-outline-primary btn-sm" onClick={() => setShowUpdateModal(true)}>
                            <i className="bi bi-person-gear me-1"></i> My Profile
                        </button>
                    </div>
                    <table className="table table-hover border shadow-sm align-middle">
                        <thead className="table-dark">
                            <tr>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Role</th>
                                {user.role === 'ADMIN' && <th className="text-center">Actions</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {allUsers.map(u => (
                                <tr key={u.id}>
                                    <td>{u.username} {u.id === user.id && <small className="text-muted">(You)</small>}</td>
                                    <td>{u.email}</td>
                                    <td>
                                        <span className={`badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-info'}`}>
                                            {u.role}
                                        </span>
                                    </td>
                                    {/* I need to make sure only ADMIN has this column */}
                                    {user.role === 'ADMIN' && (
                                        <td className="text-center">
                                            {u.id !== user.id ? (
                                                <button 
                                                    className="btn btn-sm btn-outline-danger" 
                                                    onClick={() => handleDeleteUser(u.id)}>
                                                    <i className="bi bi-trash"></i> Delete
                                                </button>
                                            ) : (
                                                <span className="text-muted small">System Owner</span>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* This is the Modal that will Popup when a user press MyProfile */}
            {showUpdateModal && (
                <div className="custom-modal-overlay">
                    <div className="custom-modal-content card p-4 border-0 shadow">
                        <h5 className="mb-3 fw-bold">Update My Information</h5>
                        <form onSubmit={handleUpdateInfo}>
                            <div className="mb-3">
                                <label className="form-label small fw-bold">Username</label>
                                <input type="text" className="form-control" value={editData.username} onChange={(e) => setEditData({...editData, username: e.target.value})} required />
                            </div>
                            <div className="mb-4">
                                <label className="form-label small fw-bold">Email Address</label>
                                <input type="email" className="form-control" value={editData.email} onChange={(e) => setEditData({...editData, email: e.target.value})} required />
                            </div>
                            <div className="d-flex justify-content-end gap-2">
                                <button type="button" className="btn btn-light" onClick={() => setShowUpdateModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
