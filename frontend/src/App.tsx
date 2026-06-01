import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OrganizationsPage from './pages/OrganizationsPage';
import OrganizationSettingsPage from './pages/OrganizationSettingsPage';
import ProjectsPage from './pages/ProjectsPage';
import ProjectSettingsPage from './pages/ProjectSettingsPage';
import BoardPage from './pages/BoardPage';
import DashboardPage from './pages/DashboardPage';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/AppLayout';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        path: '/',
        element: <AppLayout />,
        children: [
          {
            index: true,
            element: <Navigate to="/dashboard" replace />,
          },
          {
            path: '/dashboard',
            element: <DashboardPage />,
          },
          {
            path: '/orgs',
            element: <OrganizationsPage />,
          },
          {
            path: '/orgs/:orgSlug/projects',
            element: <ProjectsPage />,
          },
          {
            path: '/orgs/:orgSlug/settings',
            element: <OrganizationSettingsPage />,
          },
          {
            path: '/projects/:projectId/board',
            element: <BoardPage />,
          },
          {
            path: '/projects/:projectId/settings',
            element: <ProjectSettingsPage />,
          },
        ]
      }
    ],
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
