import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import toast from 'react-hot-toast';
import { Settings, UserPlus, Trash2, ArrowLeft, Shield } from 'lucide-react';

interface Project {
  id: string;
  name: string;
  description: string;
  key: string;
  status: string;
}

interface ProjectMember {
  id: string;
  user: { id: string; firstName: string; email: string; avatarUrl: string | null };
  role: string;
}

export default function ProjectSettingsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [newMemberEmail, setNewMemberEmail] = useState('');

  const { data: project, isLoading: projectLoading } = useQuery<Project>({
    queryKey: ['project', projectId],
    queryFn: async () => {
      const res = await api.get(`/projects/${projectId}`);
      return res.data;
    },
    enabled: !!projectId,
  });

  const [projectName, setProjectName] = useState(project?.name ?? '');
  const [projectDesc, setProjectDesc] = useState(project?.description ?? '');

  const { data: members } = useQuery<ProjectMember[]>({
    queryKey: ['project-members', projectId],
    queryFn: async () => {
      const res = await api.get(`/projects/${projectId}/members`);
      return res.data;
    },
    enabled: !!projectId,
  });

  const updateProjectMutation = useMutation({
    mutationFn: async (data: { name: string; description: string }) => {
      const res = await api.put(`/projects/${projectId}`, data);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project', projectId] });
      toast.success('Project updated!');
    },
    onError: () => toast.error('Failed to update project'),
  });

  const addMemberMutation = useMutation({
    mutationFn: async (email: string) => {
      await api.post(`/projects/${projectId}/members`, { email, role: 'DEVELOPER' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', projectId] });
      setNewMemberEmail('');
      toast.success('Member added!');
    },
    onError: () => toast.error('Failed to add member'),
  });

  const removeMemberMutation = useMutation({
    mutationFn: async (userId: string) => {
      await api.delete(`/projects/${projectId}/members/${userId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project-members', projectId] });
      toast.success('Member removed');
    },
    onError: () => toast.error('Failed to remove member'),
  });

  const deleteProjectMutation = useMutation({
    mutationFn: async () => {
      await api.delete(`/projects/${projectId}`);
    },
    onSuccess: () => {
      toast.success('Project deleted');
      navigate(-1);
    },
    onError: () => toast.error('Failed to delete project'),
  });

  if (projectLoading) return <div className="flex justify-center py-12 text-muted-foreground">Loading...</div>;

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-8">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Settings className="h-7 w-7 text-primary" />
            Project Settings
          </h1>
          <p className="text-muted-foreground mt-1">{project?.name}</p>
        </div>
      </div>

      {/* General */}
      <div className="bg-surface rounded-xl border border-border p-6 space-y-4">
        <h2 className="text-lg font-semibold">General</h2>
        <div className="space-y-2">
          <Label htmlFor="projectName">Project Name</Label>
          <Input
            id="projectName"
            defaultValue={project?.name}
            onChange={e => setProjectName(e.target.value)}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="projectDesc">Description</Label>
          <textarea
            id="projectDesc"
            className="w-full bg-muted/30 border border-border rounded-xl p-3 text-sm resize-none outline-none focus:border-primary"
            rows={3}
            defaultValue={project?.description}
            onChange={e => setProjectDesc(e.target.value)}
          />
        </div>
        <Button
          onClick={() => updateProjectMutation.mutate({ name: projectName || project?.name || '', description: projectDesc || project?.description || '' })}
          disabled={updateProjectMutation.isPending}
        >
          {updateProjectMutation.isPending ? 'Saving...' : 'Save Changes'}
        </Button>
      </div>

      {/* Members */}
      <div className="bg-surface rounded-xl border border-border p-6">
        <h2 className="text-lg font-semibold mb-4">Members</h2>
        <div className="flex gap-3 mb-6">
          <Input
            value={newMemberEmail}
            onChange={e => setNewMemberEmail(e.target.value)}
            placeholder="Add member by email"
            className="flex-1"
          />
          <Button
            onClick={() => addMemberMutation.mutate(newMemberEmail)}
            disabled={addMemberMutation.isPending || !newMemberEmail.trim()}
          >
            <UserPlus className="mr-2 h-4 w-4" /> Add
          </Button>
        </div>
        <div className="space-y-3">
          {members?.map(member => (
            <div key={member.id} className="flex items-center justify-between py-3 border-b border-border last:border-0">
              <div className="flex items-center gap-3">
                <div className="h-9 w-9 rounded-full bg-primary/20 flex items-center justify-center font-bold text-primary text-sm">
                  {member.user.firstName.charAt(0)}
                </div>
                <div>
                  <p className="text-sm font-medium">{member.user.firstName}</p>
                  <p className="text-xs text-muted-foreground">{member.user.email}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className="flex items-center gap-1 text-xs font-medium bg-muted px-2 py-1 rounded-full">
                  <Shield className="h-3 w-3" /> {member.role}
                </span>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-red-500 hover:text-red-600 hover:bg-red-50"
                  onClick={() => removeMemberMutation.mutate(member.user.id)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Danger Zone */}
      <div className="bg-surface rounded-xl border border-red-200 p-6">
        <h2 className="text-lg font-semibold text-red-600 mb-2">Danger Zone</h2>
        <p className="text-sm text-muted-foreground mb-4">Deleting a project is permanent and cannot be undone.</p>
        <Button
          variant="destructive"
          onClick={() => {
            if (confirm(`Are you sure you want to delete "${project?.name}"?`)) {
              deleteProjectMutation.mutate();
            }
          }}
          disabled={deleteProjectMutation.isPending}
        >
          <Trash2 className="mr-2 h-4 w-4" />
          {deleteProjectMutation.isPending ? 'Deleting...' : 'Delete Project'}
        </Button>
      </div>
    </div>
  );
}
