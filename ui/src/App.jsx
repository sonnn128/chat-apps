import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import { connectWebSocket, disconnectWebSocket } from './websocket';
import jwt_decode from 'jwt-decode';

function App() {
  const [notifications, setNotifications] = useState([]);
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const user = token ? jwt_decode(token) : null;

  useEffect(() => {
    if (user?.sub) {
      connectWebSocket(user.sub, (message) => {
        setNotifications(prev => [`[${new Date().toLocaleTimeString()}] ${message}`, ...prev]);
      });
    }
    return () => disconnectWebSocket();
  }, [user?.sub]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
    window.location.reload();
  };
  
  const ProtectedRoute = ({ children }) => {
    return user ? children : <Navigate to="/login" />;
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '1200px', margin: 'auto' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #ccc', paddingBottom: '10px' }}>
        <nav><Link to="/" style={{ marginRight: '15px' }}>Dashboard</Link></nav>
        <div>
          {user ? (
            <>
              <span>Welcome, <b>{user.sub}</b>! (Roles: {user.roles?.join(', ') || 'N/A'})</span>
              <button onClick={handleLogout} style={{ marginLeft: '15px' }}>Logout</button>
            </>
          ) : <Link to="/login">Login</Link>}
        </div>
      </header>
      <main style={{ display: 'flex', marginTop: '20px', gap: '20px' }}>
        <div style={{ flex: 3 }}>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/" element={<ProtectedRoute><DashboardPage user={user} /></ProtectedRoute>} />
            </Routes>
        </div>
        {user && (
            <div style={{ flex: 1, borderLeft: '1px solid #ccc', paddingLeft: '20px' }}>
                <h3>Notifications</h3>
                <div style={{ height: '300px', overflowY: 'auto', border: '1px solid #eee', padding: '10px', display: 'flex', flexDirection: 'column-reverse' }}>
                    <div>
                        {notifications.length > 0 ? notifications.map((msg, i) => <div key={i}>{msg}</div>) : <p>Waiting for notifications...</p>}
                    </div>
                </div>
            </div>
        )}
      </main>
    </div>
  );
}
export default App;
