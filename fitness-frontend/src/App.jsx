import React from 'react';
import { Box, Button, Typography, ThemeProvider, createTheme, CssBaseline, Container, Paper } from "@mui/material";
import { useContext, useEffect, useState } from "react";
import { AuthContext } from "react-oauth2-code-pkce";
import { useDispatch } from "react-redux";
import { BrowserRouter as Router, Navigate, Route, Routes, useLocation, Link } from "react-router";
import { setCredentials } from "./store/authSlice";
import ActivityForm from "./components/ActivityForm";
import ActivityList from "./components/ActivityList";
import ActivityDetail from "./components/ActivityDetail";
import StravaExchange from "./components/StravaExchange";
import IntegrationsPage from "./components/IntegrationsPage";
import ProfilePage from "./components/ProfilePage";
import RegisterPage from "./components/RegisterPage";
import ForgotPasswordPage from "./components/ForgotPasswordPage";
import ResetPasswordPage from "./components/ResetPasswordPage";
import { syncStrava } from "./services/api";

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6366f1', // Indigo 500
    },
    secondary: {
      main: '#ec4899', // Pink 500
    },
    background: {
      default: '#0f172a', // Slate 900
      paper: 'rgba(30, 41, 59, 0.7)', // Slate 800 with opacity for glassmorphism
    },
  },
  typography: {
    fontFamily: '"Outfit", "Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h3: {
      fontWeight: 700,
      background: 'linear-gradient(45deg, #6366f1 30%, #ec4899 90%)',
      WebkitBackgroundClip: 'text',
      WebkitTextFillColor: 'transparent',
    },
    h4: {
      fontWeight: 600,
      letterSpacing: '0.5px',
    }
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
          padding: '10px 24px',
          boxShadow: '0 4px 14px 0 rgba(99, 102, 241, 0.39)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 6px 20px rgba(99, 102, 241, 0.5)',
          }
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backdropFilter: 'blur(12px)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          borderRadius: 16,
        }
      }
    }
  },
});

const ActvitiesPage = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper sx={{ p: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>Record New Activity</Typography>
        <ActivityForm onActivityAdded={() => window.location.reload()} />
      </Paper>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h4" gutterBottom sx={{ mb: 3 }}>Your Activities</Typography>
        <ActivityList />
      </Paper>
    </Container>
  );
}

function App() {
  const { token, tokenData, logIn, logOut, isAuthenticated } = useContext(AuthContext);
  const dispatch = useDispatch();
  const [authReady, setAuthReady] = useState(false);
  
  useEffect(() => {
    if (token) {
      dispatch(setCredentials({token, user: tokenData}));
      setAuthReady(true);
    }
  }, [token, tokenData, dispatch]);

  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/*" element={
            !token ? (
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
                <Box sx={{ p: 6, borderRadius: 4, background: 'rgba(30, 41, 59, 0.5)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255,255,255,0.05)', maxWidth: 600 }}>
                  <Typography variant="h3" gutterBottom>
                    Apollo Fitness
                  </Typography>
                  <Typography variant="h6" sx={{ mb: 4, color: 'text.secondary', fontWeight: 300 }}>
                    Unleash your potential with AI-driven training plans and advanced activity tracking.
                  </Typography>
                  <Button variant="contained" color="primary" size="large" onClick={() => logIn()} sx={{ mr: 2 }}>
                    Sign In to Start Training
                  </Button>
                  <Button variant="outlined" color="secondary" size="large" component={Link} to="/register" sx={{ mr: 2 }}>
                    Register
                  </Button>
                  <Button variant="text" color="inherit" component={Link} to="/forgot-password">
                    Forgot Password?
                  </Button>
                </Box>
              </Box>
            ) : (
              <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
                <Box component="header" sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.1)', background: 'rgba(15, 23, 42, 0.8)', backdropFilter: 'blur(8px)' }}>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: '#6366f1' }}>Apollo</Typography>
                  <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                    <Button color="inherit" component={Link} to="/activities">Activities</Button>
                    <Button color="inherit" component={Link} to="/profile">Profile</Button>
                    <Button color="inherit" component={Link} to="/integrations">Integrations</Button>
                    <Button variant="outlined" color="secondary" onClick={logOut}>
                      Logout
                    </Button>
                  </Box>
                </Box>
                <Routes>
                  <Route path="/activities" element={<ActvitiesPage />}/>
                  <Route path="/activities/:id" element={<ActivityDetail />}/>
                  <Route path="/profile" element={<ProfilePage />}/>
                  <Route path="/integrations" element={<IntegrationsPage />}/>
                  <Route path="/strava-exchange" element={<StravaExchange />}/>
                  <Route path="/" element={token ? <Navigate to="/activities" replace/> : <div>Welcome! Please Login.</div>} />
                </Routes>
              </Box>
            )
          } />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;