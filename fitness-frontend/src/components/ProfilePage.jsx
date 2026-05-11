import React, { useState, useEffect } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Snackbar, Alert } from '@mui/material';
import { getUserProfile, updateUserProfile } from '../services/api';

const ProfilePage = () => {
    const [profile, setProfile] = useState({ height: '', weight: '', age: '' });
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
    const userId = localStorage.getItem('userId');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await getUserProfile(userId);
                setProfile({
                    height: res.data.height || '',
                    weight: res.data.weight || '',
                    age: res.data.age || ''
                });
            } catch (error) {
                console.error("Failed to fetch profile:", error);
            }
        };
        if (userId) fetchProfile();
    }, [userId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await updateUserProfile(userId, {
                height: parseFloat(profile.height),
                weight: parseFloat(profile.weight),
                age: parseInt(profile.age)
            });
            setSnackbar({ open: true, message: 'Profile updated successfully!', severity: 'success' });
        } catch (error) {
            console.error("Failed to update profile:", error);
            setSnackbar({ open: true, message: 'Failed to update profile.', severity: 'error' });
        }
    };

    const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', p: 2 }}>
            <Card sx={{ boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h5" gutterBottom color="primary">Athlete Profile</Typography>
                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                        Enter your details to receive personalized AI recommendations.
                    </Typography>
                    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField
                            label="Height (cm)"
                            type="number"
                            value={profile.height}
                            onChange={(e) => setProfile({ ...profile, height: e.target.value })}
                            fullWidth
                        />
                        <TextField
                            label="Weight (kg)"
                            type="number"
                            value={profile.weight}
                            onChange={(e) => setProfile({ ...profile, weight: e.target.value })}
                            fullWidth
                        />
                        <TextField
                            label="Age"
                            type="number"
                            value={profile.age}
                            onChange={(e) => setProfile({ ...profile, age: e.target.value })}
                            fullWidth
                        />
                        <Button type="submit" variant="contained" color="primary" sx={{ mt: 2 }}>
                            Save Profile
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

export default ProfilePage;
