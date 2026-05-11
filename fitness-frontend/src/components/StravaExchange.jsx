import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { Box, Typography, CircularProgress, Paper } from '@mui/material';
import { exchangeStravaToken } from '../services/api';

const StravaExchange = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [status, setStatus] = useState('Authenticating with Strava...');
    const [error, setError] = useState(null);

    useEffect(() => {
        const urlParams = new URLSearchParams(location.search);
        const code = urlParams.get('code');
        const err = urlParams.get('error');

        if (err) {
            setError(`Strava authorization failed: ${err}`);
            setStatus('');
            return;
        }

        if (!code) {
            setError('No authorization code found in URL.');
            setStatus('');
            return;
        }

        const exchangeToken = async () => {
            try {
                setStatus('Syncing activities from Strava...');
                const response = await exchangeStravaToken(code);
                setStatus(response.data || 'Successfully synced!');
                
                // Redirect back to activities after a short delay so they see the success message
                setTimeout(() => {
                    navigate('/activities', { state: { syncSuccess: true } });
                }, 2000);
            } catch (err) {
                console.error(err);
                setError('Failed to sync with Strava. Please try again.');
                setStatus('');
            }
        };

        exchangeToken();
    }, [location, navigate]);

    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
            <Paper sx={{ p: 4, textAlign: 'center', maxWidth: 500, width: '100%' }}>
                <Typography variant="h5" gutterBottom color="primary">
                    Strava Integration
                </Typography>
                
                {status && (
                    <Box sx={{ mt: 3 }}>
                        <CircularProgress color="secondary" sx={{ mb: 2 }} />
                        <Typography>{status}</Typography>
                    </Box>
                )}

                {error && (
                    <Typography color="error" sx={{ mt: 2 }}>
                        {error}
                    </Typography>
                )}
            </Paper>
        </Box>
    );
};

export default StravaExchange;
