import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/axios';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Label } from '../components/ui/Label';
import toast from 'react-hot-toast';
import {
  X, Calendar, User, Tag, Flag, MessageSquare,
  Trash2, Send, ChevronDown
} from 'lucide-react';
import type { Task } from '../components/kanban/TaskCard';
import { format } from 'date-fns';

interface Comment {
  id: string;
  content: string;
  author: { id: string; firstName: string; avatarUrl: string | null };
  createdAt: string;
}

interface TaskDetailPanelProps {
  task: Task;
  onClose: () => void;
}

const PRIORITIES = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
const STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

const priorityColors: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-700 border-red-200',
  HIGH: 'bg-orange-100 text-orange-700 border-orange-200',
  MEDIUM: 'bg-blue-100 text-blue-700 border-blue-200',
  LOW: 'bg-slate-100 text-slate-600 border-slate-200',
};

export default function TaskDetailPanel({ task, onClose }: TaskDetailPanelProps) {
  const queryClient = useQueryClient();
  const [commentText, setCommentText] = useState('');
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [editTitle, setEditTitle] = useState(task.title);

  const { data: comments, isLoading: commentsLoading } = useQuery<Comment[]>({
    queryKey: ['task-comments', task.id],
    queryFn: async () => {
      const res = await api.get(`/tasks/${task.id}/comments`);
      return res.data;
    },
  });

  const updateTaskMutation = useMutation({
    mutationFn: async (updates: Partial<Task>) => {
      const res = await api.put(`/tasks/${task.id}`, updates);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board-tasks'] });
      toast.success('Task updated');
    },
    onError: () => toast.error('Failed to update task'),
  });

  const deleteTaskMutation = useMutation({
    mutationFn: async () => {
      await api.delete(`/tasks/${task.id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board-tasks'] });
      toast.success('Task deleted');
      onClose();
    },
    onError: () => toast.error('Failed to delete task'),
  });

  const addCommentMutation = useMutation({
    mutationFn: async (content: string) => {
      const res = await api.post(`/tasks/${task.id}/comments`, { content });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task-comments', task.id] });
      setCommentText('');
      toast.success('Comment added');
    },
    onError: () => toast.error('Failed to add comment'),
  });

  const handleTitleSave = () => {
    if (editTitle.trim() && editTitle !== task.title) {
      updateTaskMutation.mutate({ title: editTitle });
    }
    setIsEditingTitle(false);
  };

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-start justify-end z-50">
      <div className="h-full w-full max-w-2xl bg-surface border-l border-border shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-border flex-shrink-0">
          <div className="flex items-center gap-3">
            <span className={`text-xs font-bold px-2 py-1 rounded border ${priorityColors[task.priority] || priorityColors.LOW}`}>
              {task.priority}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-red-500 hover:text-red-600 hover:bg-red-50"
              onClick={() => deleteTaskMutation.mutate()}
              disabled={deleteTaskMutation.isPending}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" className="h-8 w-8" onClick={onClose}>
              <X className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* Scrollable content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {/* Title */}
          <div>
            {isEditingTitle ? (
              <input
                autoFocus
                className="w-full text-2xl font-bold bg-transparent border-b-2 border-primary outline-none py-1"
                value={editTitle}
                onChange={e => setEditTitle(e.target.value)}
                onBlur={handleTitleSave}
                onKeyDown={e => e.key === 'Enter' && handleTitleSave()}
              />
            ) : (
              <h2
                className="text-2xl font-bold cursor-pointer hover:text-primary transition-colors"
                onClick={() => setIsEditingTitle(true)}
              >
                {task.title}
              </h2>
            )}
          </div>

          {/* Meta grid */}
          <div className="grid grid-cols-2 gap-4 p-4 bg-muted/50 rounded-xl border border-border">
            {/* Status */}
            <div className="flex flex-col gap-1.5">
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <ChevronDown className="h-3 w-3" /> Status
              </span>
              <select
                className="bg-surface border border-border rounded-md px-3 py-1.5 text-sm font-medium"
                value={task.status}
                onChange={e => updateTaskMutation.mutate({ status: e.target.value as Task['status'] })}
              >
                {STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
              </select>
            </div>

            {/* Priority */}
            <div className="flex flex-col gap-1.5">
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <Flag className="h-3 w-3" /> Priority
              </span>
              <select
                className="bg-surface border border-border rounded-md px-3 py-1.5 text-sm font-medium"
                value={task.priority}
                onChange={e => updateTaskMutation.mutate({ priority: e.target.value as Task['priority'] })}
              >
                {PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>

            {/* Assignee */}
            <div className="flex flex-col gap-1.5">
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <User className="h-3 w-3" /> Assignee
              </span>
              {task.assignee ? (
                <div className="flex items-center gap-2 text-sm">
                  <div className="h-6 w-6 rounded-full bg-primary/20 flex items-center justify-center text-[10px] font-bold text-primary">
                    {task.assignee.fullName.charAt(0)}
                  </div>
                  <span>{task.assignee.fullName}</span>
                </div>
              ) : (
                <span className="text-sm text-muted-foreground">Unassigned</span>
              )}
            </div>

            {/* Due Date */}
            <div className="flex flex-col gap-1.5">
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
                <Calendar className="h-3 w-3" /> Due Date
              </span>
              <input
                type="date"
                className="bg-surface border border-border rounded-md px-3 py-1.5 text-sm"
                defaultValue={task.dueDate ?? ''}
                onBlur={e => {
                  if (e.target.value !== task.dueDate) {
                    updateTaskMutation.mutate({ dueDate: e.target.value || null });
                  }
                }}
              />
            </div>
          </div>

          {/* Labels */}
          {task.labels && task.labels.length > 0 && (
            <div>
              <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5 mb-2">
                <Tag className="h-3 w-3" /> Labels
              </span>
              <div className="flex flex-wrap gap-2">
                {task.labels.map(label => (
                  <span
                    key={label.id}
                    className="text-xs font-medium px-2.5 py-1 rounded-full text-white"
                    style={{ backgroundColor: label.color }}
                  >
                    {label.name}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Description */}
          <div>
            <Label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2 block">
              Description
            </Label>
            <textarea
              className="w-full min-h-[100px] bg-muted/30 border border-border rounded-xl p-3 text-sm resize-none outline-none focus:border-primary transition-colors"
              defaultValue={task.description || ''}
              placeholder="Add a description..."
              onBlur={e => {
                if (e.target.value !== task.description) {
                  updateTaskMutation.mutate({ description: e.target.value });
                }
              }}
            />
          </div>

          {/* Comments */}
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5 mb-4">
              <MessageSquare className="h-3 w-3" /> Comments ({comments?.length ?? 0})
            </span>

            <div className="space-y-3 mb-4">
              {commentsLoading ? (
                <p className="text-sm text-muted-foreground">Loading comments...</p>
              ) : comments?.length === 0 ? (
                <p className="text-sm text-muted-foreground italic">No comments yet. Be the first to comment!</p>
              ) : (
                comments?.map(comment => (
                  <div key={comment.id} className="flex gap-3">
                    <div className="h-8 w-8 flex-shrink-0 rounded-full bg-primary/20 flex items-center justify-center text-xs font-bold text-primary">
                      {comment.author.firstName.charAt(0)}
                    </div>
                    <div className="flex-1 bg-muted/50 rounded-xl px-4 py-3">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-sm font-semibold">{comment.author.firstName}</span>
                        <span className="text-xs text-muted-foreground">
                          {format(new Date(comment.createdAt), 'MMM d, h:mm a')}
                        </span>
                      </div>
                      <p className="text-sm text-foreground">{comment.content}</p>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Add Comment */}
            <div className="flex gap-3">
              <div className="h-8 w-8 flex-shrink-0 rounded-full bg-primary/20 flex items-center justify-center text-xs font-bold text-primary">
                Y
              </div>
              <div className="flex-1 flex gap-2">
                <Input
                  value={commentText}
                  onChange={e => setCommentText(e.target.value)}
                  placeholder="Add a comment..."
                  className="flex-1"
                  onKeyDown={e => {
                    if (e.key === 'Enter' && commentText.trim()) {
                      addCommentMutation.mutate(commentText);
                    }
                  }}
                />
                <Button
                  size="icon"
                  className="h-10 w-10"
                  onClick={() => commentText.trim() && addCommentMutation.mutate(commentText)}
                  disabled={addCommentMutation.isPending || !commentText.trim()}
                >
                  <Send className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-border flex-shrink-0 bg-muted/20">
          <p className="text-xs text-muted-foreground text-center">
            Click title to edit · Press Enter in comment to submit
          </p>
        </div>
      </div>
    </div>
  );
}
