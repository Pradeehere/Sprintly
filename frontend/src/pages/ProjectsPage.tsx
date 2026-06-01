import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import { useOrgStore } from '../store/orgStore';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import toast from 'react-hot-toast';
import { Folder, Plus, Calendar, Settings } from 'lucide-react';
import { format } from 'date-fns';

interface Project {
  id: string;
  name: string;
  key: string;
  description: string;
  status: string;
  createdAt: string;
}

export default function ProjectsPage() {
  const [isCreating, setIsCreating] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [newProjectDesc, setNewProjectDesc] = useState('');
  
  const queryClient = useQueryClient();
  const { currentOrg } = useOrgStore();
  const navigate = useNavigate();

  const { data: projects, isLoading } = useQuery<Project[]>({
    queryKey: ['projects', currentOrg?.id],
    queryFn: async () => {
      if (!currentOrg) return [];
      const response = await api.get(`/organizations/${currentOrg.id}/projects`);
      return response.data;
    },
    enabled: !!currentOrg?.id,
  });

  const createMutation = useMutation({
    mutationFn: async (data: { name: string, description: string }) => {
      const response = await api.post(`/organizations/${currentOrg?.id}/projects`, data);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects', currentOrg?.id] });
      toast.success('Project created successfully!');
      setIsCreating(false);
      setNewProjectName('');
      setNewProjectDesc('');
    },
    onError: () => {
      toast.error('Failed to create project');
    },
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProjectName.trim()) return;
    createMutation.mutate({ name: newProjectName, description: newProjectDesc });
  };

  if (!currentOrg) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <h2 className="text-xl font-semibold mb-4">No organization selected</h2>
        <Button onClick={() => navigate('/orgs')}>Select Organization</Button>
      </div>
    );
  }

  return (
    <div className="py-6">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Projects</h1>
          <p className="text-muted-foreground mt-1">Projects in {currentOrg.name}</p>
        </div>
        {!isCreating && (
          <Button onClick={() => setIsCreating(true)}>
            <Plus className="mr-2 h-4 w-4" /> New Project
          </Button>
        )}
      </div>

      {isCreating && (
        <div className="mb-8 p-6 bg-surface rounded-xl border border-border shadow-sm max-w-2xl">
          <h2 className="text-xl font-semibold mb-4">Create New Project</h2>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="projectName">Project Name</Label>
              <Input
                id="projectName"
                value={newProjectName}
                onChange={(e) => setNewProjectName(e.target.value)}
                placeholder="e.g. Website Redesign"
                autoFocus
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="projectDesc">Description</Label>
              <Input
                id="projectDesc"
                value={newProjectDesc}
                onChange={(e) => setNewProjectDesc(e.target.value)}
                placeholder="Brief description of the project"
              />
            </div>
            <div className="flex justify-end gap-3 pt-4">
              <Button type="button" variant="ghost" onClick={() => setIsCreating(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={createMutation.isPending || !newProjectName.trim()}>
                {createMutation.isPending ? 'Creating...' : 'Create Project'}
              </Button>
            </div>
          </form>
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center py-12 text-muted-foreground">Loading projects...</div>
      ) : projects?.length === 0 && !isCreating ? (
        <div className="text-center py-12 bg-surface rounded-xl border border-border border-dashed">
          <Folder className="mx-auto h-12 w-12 text-muted-foreground/50 mb-4" />
          <h3 className="text-lg font-medium">No projects found</h3>
          <p className="text-sm text-muted-foreground mb-4">Create a project to start managing tasks.</p>
          <Button onClick={() => setIsCreating(true)}>Create Project</Button>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {projects?.map((project) => (
              <div
                key={project.id}
                className="flex flex-col p-5 bg-surface rounded-xl border border-border hover:border-primary/50 hover:shadow-md transition-all group relative"
              >
                <div
                  className="flex-1 cursor-pointer"
                  onClick={() => navigate(`/projects/${project.id}/board`)}
                >
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="font-semibold text-lg group-hover:text-primary transition-colors">{project.name}</h3>
                    <span className="px-2 py-1 bg-muted text-xs rounded-md font-mono text-muted-foreground">
                      {project.key}
                    </span>
                  </div>
                  <p className="text-sm text-muted-foreground mb-4 line-clamp-2 min-h-[40px]">
                    {project.description || 'No description provided.'}
                  </p>
                  <div className="mt-auto flex items-center justify-between text-xs text-muted-foreground border-t border-border pt-4">
                    <div className="flex items-center gap-1.5">
                      <Calendar className="h-3.5 w-3.5" />
                      {format(new Date(project.createdAt), 'MMM d, yyyy')}
                    </div>
                    <div className="flex items-center gap-1.5">
                      <div className={`w-2 h-2 rounded-full ${project.status === 'ACTIVE' ? 'bg-green-500' : 'bg-yellow-500'}`} />
                      {project.status}
                    </div>
                  </div>
                </div>
                <button
                  onClick={e => { e.stopPropagation(); navigate(`/projects/${project.id}/settings`); }}
                  className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity p-1.5 rounded-md hover:bg-muted text-muted-foreground"
                  title="Project Settings"
                >
                  <Settings className="h-4 w-4" />
                </button>
              </div>
          ))}
        </div>
      )}
    </div>
  );
}
