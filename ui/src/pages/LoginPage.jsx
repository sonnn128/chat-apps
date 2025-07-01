import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
const LoginPage = () => {
  const [username, setUsername] = useState('user');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('/api/auth/login', { username });
      localStorage.setItem('token', res.data.token);
      navigate('/');
      window.location.reload();
    } catch (err) { setError('Login failed!'); }
  };
  return (
    <form onSubmit={handleLogin}>
      <h2>Login</h2>
      <p>Enter '<b>admin</b>' or '<b>user</b>' to get different roles.</p>
      <input value={username} onChange={(e) => setUsername(e.target.value)} style={{ padding: '8px', marginRight: '10px' }}/>
      <button type="submit">Login</button>
      {error && <p style={{color: 'red'}}>{error}</p>}
    </form>
  );
};
export default LoginPage;
