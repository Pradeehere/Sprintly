import { useQuery } from '@tanstack/react-query';
import { api } from '../api/axios';
import { useNavigate } from 'react-router-dom';
import { useOrgStore } from '../store/orgStore';
import { Button } from '../components/ui/Button';
import {
  LayoutDashboard, FolderKanban, CheckSquare, Clock,
  TrendingUp, ArrowRight, Plus, AlertCircle
} from 'lucide-react';

interface DashboardData {
  totalOrganizations: number;
  totalProjects: number;
  totalTasks: number;
  tasksAssignedToMe: number;
  tasksTodo: number;
  tasksInProgress: number;
  tasksDone: number;
  recentProjects: Array<{
    id: string;
    name: string;
    key: string;
    description: string;
    status: string;
    organizationId: string;
  }>;
}

function StatCard({
  label,
  value,
  icon: Icon,
  color,
  subtext,
}: {
  label: string;
  value: number;
  icon: React.ElementType;
  color: string;
  subtext?: string;
}) {
  return (
    <div className="bg-surface rounded-xl border border-border p-6 hover:shadow-md transition-shadow">
      <div className="flex items-center justify-between mb-4">
        <span className="text-sm font-medium text-muted-foreground">{label}</span>
        <div className={`h-10 w-10 rounded-lg ${color} flex items-center justify-center`}>
          <Icon className="h-5 w-5 text-white" />
        </div>
      </div>
      <p className="text-3xl font-bold">{value}</p>
      {subtext && <p className="text-xs text-muted-foreground mt-1">{subtext}</p>}
    </div>
  );
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const { currentOrg } = useOrgStore();

  const { data, isLoading } = useQuery<{ data: DashboardData }>({
    queryKey: ['dashboard'],
    queryFn: async () => {
      const res = await api.get('/dashboard');
      return res.data;
    },
  });

  const dashboard = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="flex flex-col items-center gap-3">
          <LayoutDashboard className="h-10 w-10 text-muted-foreground/40 animate-pulse" />
          <p className="text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  const completion = dashboard && dashboard.totalTasks > 0
    ? Math.round((dashboard.tasksDone / dashboard.totalTasks) * 100)
    : 0;

  return (
    <div className="py-6 space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground mt-1">Your project overview at a glance.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Organizations"
          value={dashboard?.totalOrganizations ?? 0}
          icon={AlertCircle}
          color="bg-purple-500"
        />
        <StatCard
          label="Total Projects"
          value={dashboard?.totalProjects ?? 0}
          icon={FolderKanban}
          color="bg-blue-500"
        />
        <StatCard
          label="Total Tasks"
          value={dashboard?.totalTasks ?? 0}
          icon={CheckSquare}
          color="bg-emerald-500"
          subtext={`${completion}% completed`}
        />
        <StatCard
          label="Assigned to Me"
          value={dashboard?.tasksAssignedToMe ?? 0}
          icon={TrendingUp}
          color="bg-orange-500"
        />
      </div>

      {/* Task Status Breakdown */}
      <div className="bg-surface rounded-xl border border-border p-6">
        <h2 className="text-lg font-semibold mb-4">Task Status Breakdown</h2>
        <div className="grid grid-cols-3 gap-4">
          <div className="text-center p-4 bg-blue-50 dark:bg-blue-950/20 rounded-xl border border-blue-200 dark:border-blue-800">
            <p className="text-2xl font-bold text-blue-600">{dashboard?.tasksTodo ?? 0}</p>
            <p className="text-xs font-medium text-blue-500 mt-1 uppercase tracking-wider">To Do</p>
          </div>
          <div className="text-center p-4 bg-orange-50 dark:bg-orange-950/20 rounded-xl border border-orange-200 dark:border-orange-800">
            <p className="text-2xl font-bold text-orange-600">{dashboard?.tasksInProgress ?? 0}</p>
            <p className="text-xs font-medium text-orange-500 mt-1 uppercase tracking-wider">In Progress</p>
          </div>
          <div className="text-center p-4 bg-emerald-50 dark:bg-emerald-950/20 rounded-xl border border-emerald-200 dark:border-emerald-800">
            <p className="text-2xl font-bold text-emerald-600">{dashboard?.tasksDone ?? 0}</p>
            <p className="text-xs font-medium text-emerald-500 mt-1 uppercase tracking-wider">Done</p>
          </div>
        </div>

        {dashboard && dashboard.totalTasks > 0 && (
          <div className="mt-4">
            <div className="flex items-center justify-between text-xs text-muted-foreground mb-1.5">
              <span>Overall Progress</span>
              <span className="font-semibold text-foreground">{completion}%</span>
            </div>
            <div className="h-2 bg-muted rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-emerald-400 to-emerald-600 rounded-full transition-all duration-700"
                style={{ width: `${completion}%` }}
              />
            </div>
          </div>
        )}
      </div>

      {/* Recent Projects */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Clock className="h-5 w-5 text-muted-foreground" />
            Recent Projects
          </h2>
          {currentOrg && (
            <Button variant="ghost" size="sm" onClick={() => navigate(`/orgs/${currentOrg.slug}/projects`)}>
              View all <ArrowRight className="ml-1 h-4 w-4" />
            </Button>
          )}
        </div>

        {!dashboard?.recentProjects?.length ? (
          <div className="text-center py-12 bg-surface rounded-xl border border-border border-dashed">
            <FolderKanban className="mx-auto h-12 w-12 text-muted-foreground/40 mb-3" />
            <p className="font-medium">No projects yet</p>
            <p className="text-sm text-muted-foreground mb-4">Create an organization and project to get started.</p>
            <Button onClick={() => navigate('/orgs')}>
              <Plus className="mr-2 h-4 w-4" /> Get Started
            </Button>
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {dashboard.recentProjects.map(project => (
              <div
                key={project.id}
                onClick={() => navigate(`/projects/${project.id}/board`)}
                className="group cursor-pointer bg-surface rounded-xl border border-border p-5 hover:border-primary/50 hover:shadow-md transition-all"
              >
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-semibold group-hover:text-primary transition-colors">{project.name}</h3>
                  <span className="text-xs font-mono bg-muted px-2 py-0.5 rounded text-muted-foreground">
                    {project.key}
                  </span>
                </div>
                <p className="text-sm text-muted-foreground line-clamp-2 mb-4">
                  {project.description || 'No description'}
                </p>
                <div className="flex items-center justify-between text-xs">
                  <span className={`flex items-center gap-1 font-medium ${project.status === 'ACTIVE' ? 'text-emerald-600' : 'text-amber-600'}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${project.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                    {project.status}
                  </span>
                  <span className="text-muted-foreground group-hover:text-primary transition-colors flex items-center gap-1">
                    View Board <ArrowRight className="h-3 w-3" />
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
