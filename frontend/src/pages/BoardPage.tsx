import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import KanbanBoard from '../components/kanban/KanbanBoard';
import type { Task } from '../components/kanban/TaskCard';
import { Button } from '../components/ui/Button';
import { Filter, Users, Layout, Activity } from 'lucide-react';
import TaskDetailPanel from '../components/TaskDetailPanel';
import { createWebSocketClient } from '../lib/websocket';

export default function BoardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!projectId) return;

    const client = createWebSocketClient();
    
    client.onConnect = () => {
      console.log('Connected to WebSocket for project:', projectId);
      client.subscribe(`/topic/projects/${projectId}/tasks`, (message) => {
        const updatedTask = JSON.parse(message.body);
        
        queryClient.setQueryData(['board-tasks', projectId], (oldTasks: Task[] | undefined) => {
          if (!oldTasks) return [];
          const taskExists = oldTasks.some(t => t.id === updatedTask.id);
          
          if (taskExists) {
            return oldTasks.map(t => t.id === updatedTask.id ? updatedTask : t);
          } else {
            return [...oldTasks, updatedTask];
          }
        });
      });
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [projectId, queryClient]);

  // Fetch project details
  const { data: project } = useQuery({
    queryKey: ['project', projectId],
    queryFn: async () => {
      const res = await api.get(`/projects/${projectId}`);
      return res.data;
    },
    enabled: !!projectId,
  });

  // Fetch columns
  const { data: columns, isLoading: isColumnsLoading } = useQuery({
    queryKey: ['board-columns', projectId],
    queryFn: async () => {
      // In a real app, you'd fetch the main board first, then its columns.
      // We assume default board is fetched or we just get columns by boardId
      // Let's assume we have an endpoint that gets columns by projectId (or we need to fetch board first)
      // Since backend doesn't have a direct /projects/{id}/columns, we fetch boards first:
      const boardsRes = await api.get(`/projects/${projectId}/boards`);
      const boardId = boardsRes.data[0]?.id;
      if (!boardId) return [];
      
      const res = await api.get(`/boards/${boardId}/columns`); // Note: Adjust endpoint if needed
      return res.data;
    },
    enabled: !!projectId,
  });

  // Fetch tasks
  const { data: tasks, isLoading: isTasksLoading } = useQuery({
    queryKey: ['board-tasks', projectId],
    queryFn: async () => {
      const res = await api.get(`/projects/${projectId}/tasks`);
      return res.data;
    },
    enabled: !!projectId,
  });

  if (isColumnsLoading || isTasksLoading) {
    return <div className="flex h-full items-center justify-center">Loading board...</div>;
  }

  return (
    <div className="flex flex-col h-[calc(100vh-4rem)] -m-8">
      {/* Board Header */}
      <div className="px-8 py-4 border-b border-border bg-surface flex items-center justify-between flex-shrink-0">
        <div className="flex items-center gap-4">
          <div className="h-10 w-10 bg-primary/10 rounded-lg flex items-center justify-center text-primary">
            <Layout className="h-5 w-5" />
          </div>
          <div>
            <h2 className="text-xl font-bold">{project?.name || 'Project Board'}</h2>
            <p className="text-xs text-muted-foreground">{project?.key} • Main Board</p>
          </div>
        </div>
        
        <div className="flex items-center gap-3">
          <div className="flex -space-x-2 mr-4">
            {/* Placeholder Avatars */}
            <div className="h-8 w-8 rounded-full bg-blue-500 border-2 border-surface" />
            <div className="h-8 w-8 rounded-full bg-green-500 border-2 border-surface" />
            <div className="h-8 w-8 rounded-full bg-purple-500 border-2 border-surface flex items-center justify-center text-[10px] font-bold text-white">+3</div>
          </div>
          
          <Button variant="outline" size="sm">
            <Users className="mr-2 h-4 w-4" /> Share
          </Button>
          <Button variant="outline" size="sm">
            <Filter className="mr-2 h-4 w-4" /> Filter
          </Button>
          <Button variant="outline" size="sm">
            <Activity className="mr-2 h-4 w-4" /> Activity
          </Button>
        </div>
      </div>

      {/* Board Content */}
      <div className="flex-1 overflow-hidden bg-background">
        <KanbanBoard 
          columns={columns || []} 
          tasks={tasks || []} 
          projectId={projectId as string}
          onTaskClick={(task) => setSelectedTask(task)} 
        />
      </div>

      {selectedTask && (
        <TaskDetailPanel task={selectedTask} onClose={() => setSelectedTask(null)} />
      )}
    </div>
  );
}
