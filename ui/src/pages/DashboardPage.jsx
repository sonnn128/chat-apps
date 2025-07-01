import React, { useState } from 'react';
import axios from 'axios';
const DashboardPage = ({ user }) => {
    const [products, setProducts] = useState([]);
    const [message, setMessage] = useState('');
    const getProducts = async () => {
        try {
            const res = await axios.get('/api/products');
            setProducts(res.data);
            setMessage({ type: 'success', text: 'Products loaded successfully.' });
        } catch (err) { setMessage({ type: 'error', text: 'Failed to load products.' }); }
    };
    const sendNotification = async () => {
        if (!user) { setMessage({ type: 'error', text: 'Please log in to send notifications.' }); return; }
        try {
            await axios.post('/api/notifications/notify', { username: user.sub, message: "Hello from the client!" });
            setMessage({ type: 'success', text: 'Test notification sent! Check the notification panel.' });
        } catch(err) { setMessage({ type: 'error', text: 'Failed to send notification: ' + err.message }); }
    };
    const createProduct = async () => {
        try {
            const res = await axios.post('/api/products', { name: 'New Admin Product' });
            setMessage({ type: 'success', text: res.data.status });
        } catch (err) {
            setMessage({ type: 'error', text: `Failed to create product. Status: ${err.response.status}. You need ADMIN role.` });
        }
    };
    return (
        <div>
            <h2>Dashboard</h2>
            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                <button onClick={getProducts}>1. Get Products (Public)</button>
                <button onClick={createProduct}>2. Create Product (Admin Only)</button>
                <button onClick={sendNotification}>3. Send Test Notification</button>
            </div>
            {message && <p style={{ color: message.type === 'error' ? 'red' : 'green' }}><i>{message.text}</i></p>}
            <h3>Product List:</h3>
            <ul>{products.map(p => <li key={p.id}>{p.name}</li>)}</ul>
        </div>
    );
};
export default DashboardPage;
