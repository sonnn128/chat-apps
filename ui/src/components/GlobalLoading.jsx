// src/stores/middlewares/authMiddleware.js (ví dụ)
import { createAsyncThunk } from '@reduxjs/toolkit';
import { showLoading, hideLoading } from '../slices/uiSlice'; // Import actions
// ... các imports khác

export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async (credentials, { dispatch, rejectWithValue }) => {
    dispatch(showLoading('Đang đăng nhập...')); // Hiển thị loading với message tùy chỉnh
    try {
      // Giả lập API call
      await new Promise(resolve => setTimeout(resolve, 1500));
      if (credentials.email === 'test@example.com' && credentials.password === 'password') {
        dispatch(hideLoading()); // Ẩn loading
        return { user: { id: 1, name: 'Test User' }, token: 'fake-jwt-token' };
      } else {
        throw new Error('Sai email hoặc mật khẩu');
      }
    } catch (error) {
      dispatch(hideLoading()); // Ẩn loading ngay cả khi có lỗi
      return rejectWithValue(error.message || 'Đăng nhập thất bại');
    }
  }
);

export const registerUser = createAsyncThunk(
  'auth/registerUser',
  async (userData, { dispatch, rejectWithValue }) => {
    dispatch(showLoading('Đang đăng ký...'));
    try {
      // Giả lập API call
      await new Promise(resolve => setTimeout(resolve, 2000));
      // ... logic đăng ký
      dispatch(hideLoading());
      return { message: 'Đăng ký thành công!' };
    } catch (error) {
      dispatch(hideLoading());
      return rejectWithValue(error.message || 'Đăng ký thất bại');
    }
  }
);

// Tương tự cho các async thunks khác