import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import { useOrgStore } from '../store/orgStore';
import { useNavigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import toast from 'react-hot-toast';
import { Settings, UserPlus, Trash2, ArrowLeft, Shield, Users } from 'lucide-react';

interface OrgMember {
  id: string;
  user: { id: string; firstName: string; email: string; avatarUrl: string | null };
  role: string;
  joinedAt: string;
}

export default function OrganizationSettingsPage() {
  const { currentOrg, setCurrentOrg } = useOrgStore();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [newMemberEmail, setNewMemberEmail] = useState('');
  const [orgName, setOrgName] = useState(currentOrg?.name ?? '');

  const { data: members, isLoading } = useQuery<OrgMember[]>({
    queryKey: ['org-members', currentOrg?.id],
    queryFn: async () => {
      const res = await api.get(`/organizations/${currentOrg?.id}/members`);
      return res.data;
    },
    enabled: !!currentOrg?.id,
  });

  const updateOrgMutation = useMutation({
    mutationFn: async (name: string) => {
      const res = await api.put(`/organizations/${currentOrg?.id}`, { name });
      return res.data;
    },
    onSuccess: (data) => {
      setCurrentOrg(data);
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      toast.success('Organization updated!');
    },
    onError: () => toast.error('Failed to update organization'),
  });

  const addMemberMutation = useMutation({
    mutationFn: async (email: string) => {
      const res = await api.post(`/organizations/${currentOrg?.id}/members`, { email, role: 'MEMBER' });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['org-members', currentOrg?.id] });
      setNewMemberEmail('');
      toast.success('Member invited!');
    },
    onError: () => toast.error('Failed to add member. Make sure the email is registered.'),
  });

  const removeMemberMutation = useMutation({
    mutationFn: async (userId: string) => {
      await api.delete(`/organizations/${currentOrg?.id}/members/${userId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['org-members', currentOrg?.id] });
      toast.success('Member removed');
    },
    onError: () => toast.error('Failed to remove member'),
  });

  if (!currentOrg) {
    return (
      <div className="flex flex-col items-center justify-center py-20">
        <p className="text-muted-foreground mb-4">No organization selected</p>
        <Button onClick={() => navigate('/orgs')}>Select Organization</Button>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto py-8 space-y-8">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Settings className="h-7 w-7 text-primary" />
            Organization Settings
          </h1>
          <p className="text-muted-foreground mt-1">Manage {currentOrg.name}</p>
        </div>
      </div>

      {/* General Settings */}
      <div className="bg-surface rounded-xl border border-border p-6">
        <h2 className="text-lg font-semibold mb-4">General</h2>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="orgName">Organization Name</Label>
            <div className="flex gap-3">
              <Input
                id="orgName"
                value={orgName}
                onChange={e => setOrgName(e.target.value)}
                className="flex-1"
              />
              <Button
                onClick={() => updateOrgMutation.mutate(orgName)}
                disabled={updateOrgMutation.isPending || orgName === currentOrg.name || !orgName.trim()}
              >
                {updateOrgMutation.isPending ? 'Saving...' : 'Save'}
              </Button>
            </div>
          </div>
          <div className="space-y-1">
            <Label>Slug</Label>
            <p className="text-sm font-mono bg-muted px-3 py-2 rounded-md text-muted-foreground">/{currentOrg.slug}</p>
          </div>
        </div>
      </div>

      {/* Members */}
      <div className="bg-surface rounded-xl border border-border p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Users className="h-5 w-5 text-muted-foreground" /> Members
          </h2>
        </div>

        {/* Invite */}
        <div className="flex gap-3 mb-6">
          <Input
            value={newMemberEmail}
            onChange={e => setNewMemberEmail(e.target.value)}
            placeholder="Invite by email address"
            className="flex-1"
            onKeyDown={e => e.key === 'Enter' && newMemberEmail && addMemberMutation.mutate(newMemberEmail)}
          />
          <Button
            onClick={() => addMemberMutation.mutate(newMemberEmail)}
            disabled={addMemberMutation.isPending || !newMemberEmail.trim()}
          >
            <UserPlus className="mr-2 h-4 w-4" />
            Invite
          </Button>
        </div>

        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading members...</p>
        ) : (
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
                    disabled={removeMemberMutation.isPending}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
