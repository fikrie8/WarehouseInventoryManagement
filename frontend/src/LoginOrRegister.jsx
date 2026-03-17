import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

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
        email: '',
        role: 'USER'
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
            const response = await axios.post(`http://localhost:8080/${endpoint}`, formData); 
            if (activeTab === 'login') {
                if(response.data.statusCode === 200) {
                    console.log(response.status);
                    console.log(response.data.statusCode);
                    console.log(response.data.message);
                    setMessage("Welcome back, " + response.data.username);
                    console.log("response.data : " + JSON.stringify(response.data));
                    console.log("response.data.users : " + JSON.stringify(response.data.user));
                    localStorage.setItem("user", JSON.stringify(response.data.user));
                    localStorage.setItem("refreshToken", JSON.stringify(response.data.refreshToken));
                    console.log("refreshToken : " + JSON.parse(localStorage.getItem("refreshToken")));
                    navigate('/home');
                } else {
                    setMessage("Login failed : " + response.data.message);
                }
            } else {
                if(response.data.statusCode === 200) {
                    setMessage("Registration Successful! Switching to Login tab.");
                    setFormData({ ...formData, password: '' });
                    setTimeout(() => {
                        setActiveTab('login');
                        setMessage("");
                    }, 2000);
                } else {
                    setMessage("Registration failed : " + response.data.message);
                }
            }
        } catch (error) {
            setMessage("Error: " + (error.response?.data?.message || "Check your Connection/API"));
        }
    };

    const handleResetRequest = async () => {
    try {
        const response = await axios.put('http://localhost:8080/reset-password', {
            username: resetUsername,
            password: resetPassword
        });
        alert(response.data.message || "Password has been reset!");
        setShowReset(false);
    } catch (error) {
        alert("Error: " + (error.response?.data?.message || "Username not found"));
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
                                <label className="form-label">Role</label>
                                <select 
                                    name="role" 
                                    className="form-select" 
                                    value={formData.role} 
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="USER">User</option>
                                    <option value="MANAGER">Manager</option>
                                    <option value="ADMIN">Admin</option>
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
                        )}
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
                            type="username" 
                            className="form-control mb-3" 
                            value={resetUsername}
                            onChange={(e) => setResetUsername(e.target.value)}/>
                        <input 
                            type="password" 
                            className="form-control mb-3" 
                            value={resetPassword}
                            onChange={(e) => setResetPassword(e.target.value)}/>
                        <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-secondary btn-sm" onClick={() => setShowReset(false)}>Cancel</button>
                            <button className="btn btn-primary btn-sm" onClick={handleResetRequest}>Reset</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LoginOrRegister;
