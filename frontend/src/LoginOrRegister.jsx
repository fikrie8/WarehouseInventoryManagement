import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { getErrorMessage } from './utilities/ErrorHandler';

import './App.css';

const LoginOrRegister = () => {
    
    const [activeTab, setActiveTab] = useState('login');
    const [showReset, setShowReset] = useState(false);
    const [resetUsername, setResetUsername] = useState('');
    const [resetPassword, setResetPassword] = useState('');
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        firstName: '',
        lastName: '',
        department: 'USER',
        email: '',
        role: ''
    });
    
    const [message, setMessage] = useState('');

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");
        const endpoint = activeTab === 'login' ? 'login' : 'register';
        try {
            let payload = { ...formData };
            if (activeTab === 'register') {
                const roleMapping = {
                    'ADMIN': 'ADMIN',
                    'MANAGEMENT': 'MANAGER',
                    'LOGISTIC': 'LOGISTIC',
                    'USER': 'USER'
                };
                payload.role = roleMapping[formData.department] || 'USER';
            } else {
                payload = { 
                    username: formData.username, 
                    password: formData.password 
                };
            }
            
            const response = await axios.post(`/api/${endpoint}`, payload); 
            if (activeTab === 'login') {
                if(response.data.statusCode === 200) {
                    setMessage("Welcome back, " + response.data.username);

                    const userData = response.data.user? response.data.user:response.data;
                    localStorage.setItem("user", JSON.stringify(userData));
                    localStorage.setItem("refreshToken", JSON.stringify(response.data.refreshToken));
                    navigate('/home');
                } else {
                    setMessage("Login failed : " + response.data.message);
                }
            } else {
                if(response.data.statusCode === 200) {
                    setMessage("Registration Successful! Switching to Login tab.");
                    setFormData({
                        username: '', password: '', firstName: '', 
                        lastName: '', department: '', email: '', role: 'USER'
                    });
                    setTimeout(() => {
                        setActiveTab('login');
                        setMessage("");
                    }, 2000);
                } else {
                    setMessage("Registration failed : " + response.data.message);
                }
            }
        } catch (err) {
            const errorMessage = getErrorMessage(err);
            alert(`Failed to ${endpoint}, Server/API error : ` + errorMessage);
            console.error(`Failed to ${endpoint}, Server/API error : ` + errorMessage);
        }
    };

    const handleResetRequest = async () => {
    try {
        const response = await axios.put('/api/reset-password', {
            username: resetUsername,
            password: resetPassword
        });
        if(response.data.statusCode === 200) {
            alert(response.data.message || "Password has been reset!");
            setShowReset(false);
            setResetUsername('');
            setResetPassword('');
        }
    } catch (err) {
        if(err.response) {
            alert("Failed to reset password, Server/API error : " + err.response.data);
            console.error("Failed to reset password, Server/API error : " + err.response.data);
        } else {
            console.error("Failed to reset password, Client error : " + err.message);
        }
        setResetUsername('');
        setResetPassword('');
    }
};

    return (
        <div className="login-page-container">
            <div className="text-center mb-4 main-title">
                <h1 className="display-4 fw-bold text-white text-uppercase">Warehouse Inventory</h1>
                <p className="text-white-50">Logistics & Supply Chain Management System</p>
            </div>
            <div className="card shadow-lg auth-card">
                <div className="card-header bg-light p-0">
                    <div className="nav nav-tabs nav-fill">
                        <button 
                            className={`nav-link py-3 ${activeTab === 'login' ? 'active fw-bold' : ''}`} 
                            onClick={() => setActiveTab('login')}>LOGIN</button>
                        <button 
                            className={`nav-link py-3 ${activeTab === 'register' ? 'active fw-bold' : ''}`} 
                            onClick={() => setActiveTab('register')}>REGISTER</button>
                    </div>
                </div>

                <div className="card-body p-4">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label">Username</label>
                            <input 
                                type="text" name="username" className="form-control" 
                                value={formData.username} onChange={handleChange} required 
                            />
                        </div>

                        {activeTab === 'register' && (
                            <div className="mb-3">
                                <label className="form-label">Department</label>
                                <select 
                                    name="department" 
                                    className="form-select" 
                                    value={formData.department} 
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="ADMIN">Human Resource</option>
                                    <option value="MANAGEMENT">Management</option>
                                    <option value="LOGISTIC">Logistic</option>
                                    <option value="USER">User</option>
                                </select>
                            </div>   
                        )
                        }
                        {activeTab === 'register' && 
                            (
                                <div className="mb-3">
                                    <label className="form-label">Email</label>
                                    <input 
                                        type="text" name="email" className="form-control" 
                                        value={formData.email} onChange={handleChange}
                                    />
                                </div>
                                
                            )
                        }
                        {activeTab === 'register' && 
                            (
                                <div className="mb-3">
                                    <label className="form-label">First Name</label>
                                    <input 
                                        type="text" name="firstName" className="form-control" 
                                        value={formData.firstName} onChange={handleChange}
                                    />
                                </div>
                            )
                        }
                        {activeTab === 'register' && 
                            (
                                <div className="mb-3">
                                    <label className="form-label">Last Name</label>
                                    <input 
                                        type="text" name="lastName" className="form-control" 
                                        value={formData.lastName} onChange={handleChange}
                                    />
                                </div>                            
                            )
                        }                        
                        <div className="mb-3">
                            <label className="form-label">Password</label>
                            <input 
                                type="password" name="password" className="form-control" 
                                value={formData.password} onChange={handleChange} required 
                            />
                        </div>
                        {activeTab === 'login' && (
                            <div className="text-end mb-3">
                                <small className="text-primary cursor-pointer" onClick={() => setShowReset(true)} style={{cursor: 'pointer'}}>
                                    Forgot Password?
                                </small>
                            </div>
                        )}
                        <button type="submit" className="btn btn-primary w-100 mt-2 py-2">
                            {activeTab === 'login' ? 'SIGN IN' : 'CREATE ACCOUNT'}
                        </button>
                    </form>

                    {message && (
                        <div className={`mt-3 alert ${message.includes('Error') ? 'alert-danger' : 'alert-success'}`}>
                            {message}
                        </div>
                    )}
                </div>
            </div>
            {showReset && (
                <div className="custom-modal-overlay">
                    <div className="custom-modal-content card p-4 shadow">
                        <h5 className="mb-3">Reset Password</h5>
                        <p className="text-muted small">Enter your username and new password.</p>
                        <input 
                            type="text" 
                            className="form-control mb-3" 
                            value={resetUsername}
                            onChange={(e) => setResetUsername(e.target.value)}/>
                        <input 
                            type="password" 
                            className="form-control mb-3" 
                            value={resetPassword}
                            onChange={(e) => setResetPassword(e.target.value)}/>
                        <div className="d-flex justify-content-end gap-2">
                            <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowReset(false)}>Cancel</button>
                            <button type="button" className="btn btn-primary btn-sm" onClick={handleResetRequest}>Reset</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LoginOrRegister;
