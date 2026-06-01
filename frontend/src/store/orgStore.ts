import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface Organization {
  id: string;
  name: string;
  slug: string;
  logoUrl: string | null;
}

interface OrgState {
  currentOrg: Organization | null;
  setCurrentOrg: (org: Organization | null) => void;
}

export const useOrgStore = create<OrgState>()(
  persist(
    (set) => ({
      currentOrg: null,
      setCurrentOrg: (org) => set({ currentOrg: org }),
    }),
    {
      name: 'sprintly-org',
    }
  )
);
