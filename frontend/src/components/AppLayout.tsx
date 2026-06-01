import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useOrgStore } from '../store/orgStore';
import {
  LogOut, Settings, LayoutDashboard, FolderKanban, Building2, ChevronRight
} from 'lucide-react';
import { Button } from './ui/Button';
import NotificationsPanel from './NotificationsPanel';

export default function AppLayout() {
  const { user, logout } = useAuthStore();
  const { currentOrg } = useOrgStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) =>
    location.pathname.startsWith(path)
      ? 'bg-primary/10 text-primary font-semibold'
      : 'text-foreground/70 hover:bg-muted hover:text-foreground';

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      {/* Sidebar */}
      <aside className="w-64 flex-shrink-0 border-r border-border bg-surface flex flex-col">
        {/* Logo */}
        <div className="flex h-16 items-center px-6 border-b border-border">
          <Link to="/dashboard" className="flex items-center gap-2.5 text-primary font-bold text-xl">
            <div className="w-8 h-8 rounded-lg bg-primary text-white flex items-center justify-center font-black">S</div>
            Sprintly
          </Link>
        </div>

        {/* Org Switcher */}
        <div className="p-3 border-b border-border">
          <div className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-2 mb-1.5">Workspace</div>
          {currentOrg ? (
            <button
              onClick={() => navigate('/orgs')}
              className="w-full flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-muted transition-colors group"
            >
              <div className="h-7 w-7 rounded-md bg-primary/20 flex items-center justify-center text-primary font-bold text-sm flex-shrink-0">
                {currentOrg.name.charAt(0).toUpperCase()}
              </div>
              <span className="font-medium text-sm truncate flex-1 text-left">{currentOrg.name}</span>
              <ChevronRight className="h-4 w-4 text-muted-foreground group-hover:text-foreground flex-shrink-0" />
            </button>
          ) : (
            <Button variant="outline" className="w-full justify-start text-sm h-9" onClick={() => navigate('/orgs')}>
              <Building2 className="mr-2 h-4 w-4" /> Select Workspace
            </Button>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto py-3 px-3">
          <div className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-2 mb-1.5">Main</div>
          <ul className="space-y-0.5">
            <li>
              <Link
                to="/dashboard"
                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${isActive('/dashboard')}`}
              >
                <LayoutDashboard className="h-4 w-4" />
                Dashboard
              </Link>
            </li>
            <li>
              <Link
                to="/orgs"
                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${isActive('/orgs')}`}
              >
                <Building2 className="h-4 w-4" />
                Organizations
              </Link>
            </li>
            {currentOrg && (
              <>
                <div className="text-[10px] font-bold text-muted-foreground uppercase tracking-widest px-2 mb-1.5 mt-4">
                  {currentOrg.name}
                </div>
                <li>
                  <Link
                    to={`/orgs/${currentOrg.slug}/projects`}
                    className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${isActive(`/orgs/${currentOrg.slug}/projects`)}`}
                  >
                    <FolderKanban className="h-4 w-4" />
                    Projects
                  </Link>
                </li>
                <li>
                  <Link
                    to={`/orgs/${currentOrg.slug}/settings`}
                    className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors ${isActive(`/orgs/${currentOrg.slug}/settings`)}`}
                  >
                    <Settings className="h-4 w-4" />
                    Org Settings
                  </Link>
                </li>
              </>
            )}
          </ul>
        </nav>

        {/* User Footer */}
        <div className="p-3 border-t border-border">
          <div className="flex items-center gap-3 px-2 py-2 mb-1">
            <div className="h-8 w-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-sm flex-shrink-0">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">{user?.fullName}</p>
              <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
            </div>
          </div>
          <Button
            variant="ghost"
            className="w-full justify-start text-red-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30 h-9"
            onClick={handleLogout}
          >
            <LogOut className="mr-2 h-4 w-4" />
            Sign out
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="h-16 flex-shrink-0 border-b border-border bg-surface flex items-center justify-between px-8">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            {currentOrg && (
              <>
                <span>{currentOrg.name}</span>
                <ChevronRight className="h-3.5 w-3.5" />
              </>
            )}
            <span className="text-foreground font-medium">
              {location.pathname.includes('/board') ? 'Board' :
               location.pathname.includes('/settings') ? 'Settings' :
               location.pathname.includes('/projects') ? 'Projects' :
               location.pathname.includes('/orgs') ? 'Organizations' : 'Dashboard'}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <NotificationsPanel />
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-auto p-8 bg-background">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
}
