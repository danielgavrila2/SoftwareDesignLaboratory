import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Snackbar, Alert } from '@mui/material';
import { registerUser } from '../services/api';
import { useNavigate } from 'react-router';

const RegisterPage = () => {
    const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await registerUser(form);
            setSnackbar({ open: true, message: 'Registration successful! You can now log in.', severity: 'success' });
            setTimeout(() => navigate('/'), 2000); // Redirect to login after 2 seconds
        } catch (error) {
            console.error("Failed to register:", error);
            setSnackbar({ open: true, message: 'Registration failed. Please try again.', severity: 'error' });
        }
    };

    const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

    return (
        <Box
            sx={{
                height: "100vh",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                textAlign: "center",
                background: 'radial-gradient(circle at 50% 50%, #1e293b 0%, #0f172a 100%)',
            }}
        >
            <Card sx={{ p: 4, borderRadius: 4, background: 'rgba(30, 41, 59, 0.5)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.05)', maxWidth: 500, width: '100%' }}>
                <CardContent>
                    <Typography variant="h4" gutterBottom color="primary" sx={{ fontWeight: 700 }}>
                        Join Apollo
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 4, color: 'text.secondary' }}>
                        Create your account to start your fitness journey.
                    </Typography>
                    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField
                            label="First Name"
                            value={form.firstName}
                            onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                            fullWidth
                            variant="outlined"
                        />
                        <TextField
                            label="Last Name"
                            value={form.lastName}
                            onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                            fullWidth
                            variant="outlined"
                        />
                        <TextField
                            label="Email"
                            type="email"
                            value={form.email}
                            onChange={(e) => setForm({ ...form, email: e.target.value })}
                            fullWidth
                            variant="outlined"
                            required
                        />
                        <TextField
                            label="Password"
                            type="password"
                            value={form.password}
                            onChange={(e) => setForm({ ...form, password: e.target.value })}
                            fullWidth
                            variant="outlined"
                            required
                        />
                        <Button type="submit" variant="contained" color="primary" size="large" sx={{ mt: 2 }}>
                            Register
                        </Button>
                        <Button variant="text" color="secondary" onClick={() => navigate('/')}>
                            Already have an account? Sign In
                        </Button>
                    </Box>
                </CardContent>
            </Card>
            <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Box>
    );
};

export default RegisterPage;
