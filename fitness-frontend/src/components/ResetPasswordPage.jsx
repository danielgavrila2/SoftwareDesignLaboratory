import React, { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Snackbar, Alert } from '@mui/material';
import { resetPassword } from '../services/api';
import { useNavigate, useLocation } from 'react-router';

const ResetPasswordPage = () => {
    const [password, setPassword] = useState('');
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
    const navigate = useNavigate();
    const location = useLocation();

    // Get token from URL
    const query = new URLSearchParams(location.search);
    const token = query.get('token');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await resetPassword({ token, newPassword: password });
            setSnackbar({ open: true, message: 'Password reset successful! You can now log in.', severity: 'success' });
            setTimeout(() => navigate('/'), 2000);
        } catch (error) {
            console.error("Failed to reset password:", error);
            setSnackbar({ open: true, message: 'Failed to reset password. Token may be invalid or expired.', severity: 'error' });
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
                        Reset Password
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 4, color: 'text.secondary' }}>
                        Enter your new password below.
                    </Typography>
                    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField
                            label="New Password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            fullWidth
                            variant="outlined"
                            required
                        />
                        <Button type="submit" variant="contained" color="primary" size="large" sx={{ mt: 2 }}>
                            Reset Password
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

export default ResetPasswordPage;
