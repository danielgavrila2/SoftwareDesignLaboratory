import { Box, Typography, Card, CardContent, Button, Snackbar, Alert } from '@mui/material';
import Grid from '@mui/material/Grid';
import React, { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router';
import { getActivities, getStravaConfig } from '../services/api';

const iconMap = {
  RUNNING: '🏃',
  CYCLING: '🚴',
  SWIMMING: '🏊',
  WALKING: '🚶',
  YOGA: '🧘',
  STRENGTH_TRAINING: '🏋️',
  HIIT: '⚡',
  ROWING: '🚣',
};

const ActivityList = () => {
  const [activities, setActivities] = useState([]);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });
  const navigate = useNavigate();
  const location = useLocation();

  const fetchActivities = async () => {
    try {
      const response = await getActivities();
      setActivities(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleConnectStrava = async () => {
    try {
      const configRes = await getStravaConfig();
      if (!configRes.data || !configRes.data.clientId) {
        setSnackbar({ open: true, message: 'Please set your Strava Client ID in Integrations first!', severity: 'warning' });
        return;
      }
      const CLIENT_ID = configRes.data.clientId; 
      const REDIRECT_URI = 'http://localhost:5173/strava-exchange';
      const SCOPE = 'activity:read_all';
      const authUrl = `http://www.strava.com/oauth/authorize?client_id=${CLIENT_ID}&response_type=code&redirect_uri=${REDIRECT_URI}&approval_prompt=force&scope=${SCOPE}`;
      window.location.href = authUrl;
    } catch (e) {
      setSnackbar({ open: true, message: 'Please configure Strava in the Integrations page first.', severity: 'warning' });
    }
  };

  const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

  useEffect(() => {
    fetchActivities();
    
    // Check if we just came back from a successful Strava sync
    if (location.state?.syncSuccess) {
      setSnackbar({ open: true, message: 'Successfully authenticated and synced with Strava!', severity: 'success' });
      // Clear the state so it doesn't show again on refresh
      window.history.replaceState({}, document.title)
    }
  }, [location]);

  return (
    <Box sx={{ p: 2 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'flex-end' }}>
        <Button 
          variant="contained" 
          color="secondary" 
          onClick={handleConnectStrava}
          sx={{ fontWeight: 'bold' }}
        >
          Connect Strava
        </Button>
      </Box>

      <Grid container spacing={2}>
        {activities.map((activity) => (
          <Grid item xs={12} sm={6} md={4} key={activity.id}>
              <Card sx={{cursor: 'pointer', height: '100%', transition: '0.3s', '&:hover': { transform: 'translateY(-4px)', boxShadow: 4 }}}
              onClick= {() => navigate(`/activities/${activity.id}`)}>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                      <Typography variant='h5'>{iconMap[activity.activityType] || iconMap[activity.type] || '🎯'}</Typography>
                      <Typography variant='h6' color="primary">{activity.type || activity.activityType}</Typography>
                    </Box>
                    <Typography>Duration: {activity.duration} mins</Typography>
                    <Typography>Calories: {activity.caloriesBurned} kcal</Typography>
                  </CardContent>
              </Card>
          </Grid>
        ))}
      </Grid>

      <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={handleCloseSnackbar}>
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  )
}

export default ActivityList