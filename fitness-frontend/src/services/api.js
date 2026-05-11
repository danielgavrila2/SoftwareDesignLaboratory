import axios from "axios";

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL:API_URL
});

api.interceptors.request.use((config) => {
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    if (userId) {
        config.headers['X-User-ID'] = userId;
    }
    return config;
}
);


export const getActivities = () => api.get('/activities');
export const addActivity = (activity) => api.post('/activities', activity);
export const getActivityDetail = (id) => api.get(`/recommendations/activity/${id}`);
export const syncStrava = () => api.post(`/activities/strava/sync`);
export const exchangeStravaToken = (code) => api.post(`/activities/strava/exchange?code=${code}`);
export const saveStravaConfig = (clientId, clientSecret) => api.post(`/activities/strava/config?clientId=${clientId}&clientSecret=${clientSecret}`);
export const getStravaConfig = () => api.get(`/activities/strava/config`);
export const deleteActivity = (id) => api.delete(`/activities/${id}`);
export const updateActivity = (id, activity) => api.put(`/activities/${id}`, activity);
export const getUserProfile = (userId) => api.get(`/users/${userId}`);
export const updateUserProfile = (userId, profile) => api.put(`/users/${userId}`, profile);
export const registerUser = (user) => api.post('/users/register', user);
export const forgotPassword = (email) => api.post(`/users/forgot-password?email=${email}`);
export const resetPassword = (data) => api.post('/users/reset-password', data);