import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import UserManagement from './UserManagement';
import Inventory from './Inventory';
import InboundOutbound from './InboundOutbound';
import './App.css';

const Home = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('inventory');
    const [user, setUser] = useState(JSON.parse(localStorage.getItem("user")));

    const handleLogout = () => {
        localStorage.removeItem("user");
        navigate('/');
    };

    return (
        <div className="min-vh-100 bg-light">
            <nav className="navbar navbar-dark bg-dark px-4 py-3 shadow">
                <span className="navbar-brand fw-bold">WAREHOUSE INVENTORY MANAGEMENT</span>
                <div className="text-white d-flex align-items-center">
                    <span className="me-3">Welcome, <strong>{user?.username}</strong></span>
                    <button className="btn btn-outline-danger btn-sm" onClick={handleLogout}>Logout</button>
                </div>
            </nav>

            <div className="container mt-4">
                <div className="card shadow-sm border-0 mb-4">
                    <ul className="nav nav-pills nav-justified p-2 bg-white rounded">
                        <li className="nav-item">
                            <button className={`nav-link py-3 ${activeTab === 'users' ? 'active' : 'text-dark'}`} onClick={() => setActiveTab('users')}>
                                <i className="bi bi-people-fill me-2"></i> User Management
                            </button>
                        </li>
                        <li className="nav-item">
                            <button className={`nav-link py-3 ${activeTab === 'inventory' ? 'active' : 'text-dark'}`} onClick={() => setActiveTab('inventory')}>
                                <i className="bi bi-box-seam me-2"></i> Inventory
                            </button>
                        </li>
                        <li className="nav-item">
                            <button className={`nav-link py-3 ${activeTab === 'movement' ? 'active' : 'text-dark'}`} onClick={() => setActiveTab('movement')}>
                                <i className="bi bi-arrow-left-right me-2"></i> Inventory Movement
                            </button>
                        </li>
                    </ul>
                </div>

                <div className="bg-white p-4 rounded shadow-sm border">
                    {activeTab === 'users' && <UserManagement user={user} setUser={setUser} />}
                    {activeTab === 'inventory' && <Inventory user={user} />}
                    {activeTab === 'movement' && <InboundOutbound user={user} />}
                </div>
            </div>
        </div>
    );
};

export default Home;
