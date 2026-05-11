import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router'
import { getActivityDetail, deleteActivity, updateActivity } from '../services/api';
import { Box, Card, CardContent, Divider, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from '@mui/material';

const ActivityDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [activity, setActivity] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [openEditDialog, setOpenEditDialog] = useState(false);
  const [editDuration, setEditDuration] = useState('');
  const [editCalories, setEditCalories] = useState('');

  const handleDelete = async () => {
    try {
      await deleteActivity(id);
      navigate('/activities');
    } catch (error) {
      console.error("Failed to delete activity:", error);
    }
  };

  const handleEditOpen = () => {
    setEditDuration(activity.duration);
    setEditCalories(activity.caloriesBurned);
    setOpenEditDialog(true);
  };

  const handleEditSave = async () => {
    try {
      await updateActivity(id, {
        duration: parseInt(editDuration),
        caloriesBurned: parseInt(editCalories),
        activityType: activity.activityType || activity.type
      });
      setOpenEditDialog(false);
      // Refresh data
      window.location.reload();
    } catch (error) {
      console.error("Failed to update activity:", error);
    }
  };

  useEffect(() => {
    const fetchActivityDetail = async () => {
      try {
        const response = await getActivityDetail(id);
        setActivity(response.data);
        setRecommendation(response.data.recommendation);
      } catch (error) {
        console.error(error);
      }
    }

    fetchActivityDetail();
  }, [id]);

  if (!activity) {
    return <Typography sx={{ p: 3 }}>Loading...</Typography>
  }
  return (
    <Box sx={{ maxWidth: 1000, mx: 'auto', p: 2 }}>
            <Card sx={{ mb: 3, background: 'rgba(30, 41, 59, 0.5)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.05)', boxShadow: 3 }}>
                <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Typography variant="h5" color="primary">Activity Details</Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button size="small" variant="outlined" color="primary" onClick={handleEditOpen}>Edit</Button>
                            <Button size="small" variant="outlined" color="error" onClick={() => setOpenDeleteDialog(true)}>Delete</Button>
                        </Box>
                    </Box>
                    <Typography><strong>Type:</strong> {activity.type || activity.activityType}</Typography>
                    <Typography><strong>Duration:</strong> {activity.duration} minutes</Typography>
                    <Typography><strong>Calories Burned:</strong> {activity.caloriesBurned}</Typography>
                    {activity.additionalMetrics?.distance_km && (
                       <Typography><strong>Distance:</strong> {activity.additionalMetrics.distance_km} km</Typography>
                    )}
                    {activity.additionalMetrics?.average_speed_kmh && (
                       <Typography><strong>Avg Speed:</strong> {activity.additionalMetrics.average_speed_kmh} km/h</Typography>
                    )}
                    <Typography><strong>Date:</strong> {activity.createdAt ? new Date(activity.createdAt).toLocaleString() : 'N/A'}</Typography>
                    
                    {activity.additionalMetrics?.start_lat && activity.additionalMetrics?.start_lng && (
                      <Box sx={{ mt: 3, mb: 1 }}>
                          <Typography variant="h6" gutterBottom color="primary">Start Location Map</Typography>
                          <Box sx={{ borderRadius: 2, overflow: 'hidden', height: 350, border: '1px solid rgba(255,255,255,0.1)' }}>
                              <iframe 
                                  width="100%" 
                                  height="100%" 
                                  frameBorder="0" 
                                  scrolling="no" 
                                  marginHeight="0" 
                                  marginWidth="0" 
                                  src={`https://www.openstreetmap.org/export/embed.html?bbox=${activity.additionalMetrics.start_lng-0.02},${activity.additionalMetrics.start_lat-0.02},${activity.additionalMetrics.start_lng+0.02},${activity.additionalMetrics.start_lat+0.02}&layer=mapnik&marker=${activity.additionalMetrics.start_lat},${activity.additionalMetrics.start_lng}`}
                                  style={{ border: 'none' }}
                              ></iframe>
                          </Box>
                      </Box>
                    )}
                </CardContent>
            </Card>

                <Card sx={{ background: 'rgba(30, 41, 59, 0.5)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.05)', boxShadow: 3 }}>
                    <CardContent>
                        <Typography variant="h5" gutterBottom color="secondary">AI Recommendation & Calendar</Typography>
                        
                        <Typography variant="h6" sx={{ mt: 2 }}>Analysis</Typography>
                        <Typography paragraph>{activity.recommendation}</Typography>
                        
                        <Divider sx={{ my: 2 }} />
                        
                        <Typography variant="h6">Improvements</Typography>
                        {activity?.improvements?.map((improvement, index) => (
                            <Typography key={index} paragraph>• {improvement}</Typography>
                        ))}
                        
                        <Divider sx={{ my: 2 }} />
                        
                        <Typography variant="h6">Suggestions</Typography>
                        {activity?.suggestions?.map((suggestion, index) => (
                            <Typography key={index} paragraph>• {suggestion}</Typography>
                        ))}
                        
                        <Divider sx={{ my: 2 }} />
                        
                        <Typography variant="h6">Safety Guidelines</Typography>
                        {activity?.safety?.map((safety, index) => (
                            <Typography key={index} paragraph>• {safety}</Typography>
                        ))}

                        {activity?.dailyPlan && activity.dailyPlan.length > 0 && (
                          <Box sx={{ mt: 4 }}>
                            <Divider sx={{ my: 3 }} />
                            <Typography variant="h5" gutterBottom color="primary">7-Day Training Calendar (Weather Adapted)</Typography>
                            <TableContainer component={Paper} elevation={2} sx={{ mt: 2 }}>
                              <Table>
                                <TableHead sx={{ backgroundColor: '#f0f4f8' }}>
                                  <TableRow>
                                    <TableCell><strong>Day</strong></TableCell>
                                    <TableCell><strong>Activity</strong></TableCell>
                                    <TableCell><strong>Duration (mins)</strong></TableCell>
                                    <TableCell><strong>Description</strong></TableCell>
                                  </TableRow>
                                </TableHead>
                                <TableBody>
                                  {activity.dailyPlan.map((plan, index) => (
                                    <TableRow key={index} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                      <TableCell component="th" scope="row">{plan.day}</TableCell>
                                      <TableCell>{plan.activity}</TableCell>
                                      <TableCell>{plan.durationMinutes}</TableCell>
                                      <TableCell>{plan.description}</TableCell>
                                    </TableRow>
                                  ))}
                                </TableBody>
                              </Table>
                            </TableContainer>
                          </Box>
                        )}
                    </CardContent>
                </Card>
            )}
            {/* Delete Confirmation Dialog */}
            <Dialog open={openDeleteDialog} onClose={() => setOpenDeleteDialog(false)}>
                <DialogTitle>Delete Activity</DialogTitle>
                <DialogContent>
                    <Typography>Are you sure you want to delete this activity? This action cannot be undone.</Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDeleteDialog(false)}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete</Button>
                </DialogActions>
            </Dialog>

            {/* Edit Dialog */}
            <Dialog open={openEditDialog} onClose={() => setOpenEditDialog(false)}>
                <DialogTitle>Edit Activity</DialogTitle>
                <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
                    <TextField
                        label="Duration (mins)"
                        type="number"
                        value={editDuration}
                        onChange={(e) => setEditDuration(e.target.value)}
                        fullWidth
                    />
                    <TextField
                        label="Calories Burned"
                        type="number"
                        value={editCalories}
                        onChange={(e) => setEditCalories(e.target.value)}
                        fullWidth
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenEditDialog(false)}>Cancel</Button>
                    <Button onClick={handleEditSave} color="primary">Save</Button>
                </DialogActions>
            </Dialog>
        </Box>
  )
}

export default ActivityDetail