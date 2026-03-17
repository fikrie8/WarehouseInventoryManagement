import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginOrRegister from './LoginOrRegister';
import Home from './Home';
import ProtectedRoute from './ProtectedRoute';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginOrRegister />} />
        <Route 
              path="/home" 
              element={
                <ProtectedRoute>
                    <Home />
                </ProtectedRoute>
              } 
        />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
