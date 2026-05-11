import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Snackbar, Alert } from '@mui/material';
import { forgotPassword } from '../services/api';
import { useNavigate } from 'react-router';

const ForgotPasswordPage = () => {
    const [email, setEmail] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await forgotPassword(email);
            setSnackbar({ open: true, message: 'Password reset email sent! Please check your inbox.', severity: 'success' });
        } catch (error) {
            console.error("Failed to request password reset:", error);
            setSnackbar({ open: true, message: 'Failed to request password reset.', severity: 'error' });
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
                        Forgot Password
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 4, color: 'text.secondary' }}>
                        Enter your email address and we'll send you a link to reset your password.
                    </Typography>
                    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField
                            label="Email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            fullWidth
                            variant="outlined"
                            required
                        />
                        <Button type="submit" variant="contained" color="primary" size="large" sx={{ mt: 2 }}>
                            Send Reset Link
                        </Button>
                        <Button variant="text" color="secondary" onClick={() => navigate('/')}>
                            Back to Sign In
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

export default ForgotPasswordPage;
