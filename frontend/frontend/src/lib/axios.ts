import axios from 'axios';

// Use relative path through Vite proxy in dev, or full URL with CORS if VITE_API_URL is set
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});
