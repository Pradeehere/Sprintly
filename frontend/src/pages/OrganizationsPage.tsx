import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import { useOrgStore } from '../store/orgStore';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import toast from 'react-hot-toast';
import { Building2, Plus, ArrowRight } from 'lucide-react';

interface Organization {
  id: string;
  name: string;
  slug: string;
  logoUrl: string | null;
}

export default function OrganizationsPage() {
  const [isCreating, setIsCreating] = useState(false);
  const [newOrgName, setNewOrgName] = useState('');
  const queryClient = useQueryClient();
  const setCurrentOrg = useOrgStore((state) => state.setCurrentOrg);
  const navigate = useNavigate();

  const { data: organizations, isLoading } = useQuery<Organization[]>({
    queryKey: ['organizations'],
    queryFn: async () => {
      const response = await api.get('/organizations');
      return response.data;
    },
  });

  const createMutation = useMutation({
    mutationFn: async (name: string) => {
      const response = await api.post('/organizations', { name });
      return response.data;
    },
    onSuccess: (newOrg) => {
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      setCurrentOrg(newOrg);
      toast.success('Organization created successfully!');
      setIsCreating(false);
      setNewOrgName('');
      navigate(`/orgs/${newOrg.slug}/projects`);
    },
    onError: () => {
      toast.error('Failed to create organization');
    },
  });

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOrgName.trim()) return;
    createMutation.mutate(newOrgName);
  };

  const handleSelectOrg = (org: Organization) => {
    setCurrentOrg(org);
    navigate(`/orgs/${org.slug}/projects`);
  };

  if (isLoading) {
    return <div className="flex justify-center py-12 text-muted-foreground">Loading organizations...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Organizations</h1>
          <p className="text-muted-foreground mt-1">Manage your workspaces and teams.</p>
        </div>
        {!isCreating && (
          <Button onClick={() => setIsCreating(true)}>
            <Plus className="mr-2 h-4 w-4" /> New Organization
          </Button>
        )}
      </div>

      {isCreating && (
        <div className="mb-8 p-6 bg-surface rounded-xl border border-border shadow-sm">
          <h2 className="text-xl font-semibold mb-4">Create New Organization</h2>
          <form onSubmit={handleCreate} className="flex gap-4 items-end">
            <div className="flex-1 space-y-2">
              <Label htmlFor="orgName">Organization Name</Label>
              <Input
                id="orgName"
                value={newOrgName}
                onChange={(e) => setNewOrgName(e.target.value)}
                placeholder="e.g. Acme Corp"
                autoFocus
              />
            </div>
            <Button type="button" variant="ghost" onClick={() => setIsCreating(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={createMutation.isPending || !newOrgName.trim()}>
              {createMutation.isPending ? 'Creating...' : 'Create'}
            </Button>
          </form>
        </div>
      )}

      {organizations?.length === 0 && !isCreating ? (
        <div className="text-center py-12 bg-surface rounded-xl border border-border border-dashed">
          <Building2 className="mx-auto h-12 w-12 text-muted-foreground/50 mb-4" />
          <h3 className="text-lg font-medium">No organizations found</h3>
          <p className="text-sm text-muted-foreground mb-4">Get started by creating a new organization.</p>
          <Button onClick={() => setIsCreating(true)}>Create Organization</Button>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {organizations?.map((org) => (
            <div
              key={org.id}
              className="group flex flex-col justify-between p-6 bg-surface rounded-xl border border-border hover:border-primary/50 hover:shadow-md transition-all cursor-pointer"
              onClick={() => handleSelectOrg(org)}
            >
              <div className="flex items-center gap-4 mb-4">
                <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-bold text-xl">
                  {org.name.charAt(0)}
                </div>
                <div>
                  <h3 className="font-semibold text-lg">{org.name}</h3>
                  <p className="text-xs text-muted-foreground">/{org.slug}</p>
                </div>
              </div>
              <div className="flex items-center text-sm text-primary font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                Go to projects <ArrowRight className="ml-1 h-4 w-4" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
