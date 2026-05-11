import React, { useState, useEffect } from 'react';
import { Box, Typography, Container, Paper, TextField, Button, Snackbar, Alert } from '@mui/material';
import { getStravaConfig, saveStravaConfig } from '../services/api';

const IntegrationsPage = () => {
    const [clientId, setClientId] = useState('');
    const [clientSecret, setClientSecret] = useState('');
    const [saving, setSaving] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });

    useEffect(() => {
        const fetchConfig = async () => {
            try {
                const response = await getStravaConfig();
                if (response.data) {
                    setClientId(response.data.clientId || '');
                    setClientSecret(response.data.clientSecret || '');
                }
            } catch (error) {
                // Ignore 404s as it means no config exists yet
                if (error.response && error.response.status !== 404) {
                    console.error("Failed to load config:", error);
                }
            }
        };
        fetchConfig();
    }, []);

    const handleSave = async () => {
        setSaving(true);
        try {
            await saveStravaConfig(clientId, clientSecret);
            setSnackbar({ open: true, message: 'Strava configuration saved successfully!', severity: 'success' });
        } catch (error) {
            console.error(error);
            setSnackbar({ open: true, message: 'Failed to save configuration.', severity: 'error' });
        } finally {
            setSaving(false);
        }
    };

    const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

    return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom color="primary">
                    Integrations
                </Typography>
                <Typography variant="body1" sx={{ mb: 4, color: 'text.secondary' }}>
                    Connect external fitness tracking applications to automatically sync your activities.
                </Typography>

                <Box sx={{ border: '1px solid rgba(255,255,255,0.1)', borderRadius: 2, p: 3, mb: 4 }}>
                    <Typography variant="h6" gutterBottom>
                        Strava Developer API
                    </Typography>
                    <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
                        To sync activities, you must create an API Application in your Strava Settings and provide the Client ID and Secret below.
                    </Typography>
                    
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField 
                            label="Strava Client ID" 
                            variant="outlined" 
                            fullWidth
                            value={clientId}
                            onChange={(e) => setClientId(e.target.value)}
                        />
                        <TextField 
                            label="Strava Client Secret" 
                            variant="outlined" 
                            type="password"
                            fullWidth
                            value={clientSecret}
                            onChange={(e) => setClientSecret(e.target.value)}
                        />
                        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <Button 
                                variant="contained" 
                                color="primary" 
                                onClick={handleSave}
                                disabled={saving || !clientId || !clientSecret}
                            >
                                {saving ? 'Saving...' : 'Save Configuration'}
                            </Button>
                        </Box>
                    </Box>
                </Box>
            </Paper>

            <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
};

export default IntegrationsPage;
