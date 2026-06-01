import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { format } from 'date-fns';
import { Clock, MessageSquare, MoreHorizontal } from 'lucide-react';
import { Button } from '../ui/Button';

interface User {
  id: string;
  fullName: string;
  avatarUrl: string | null;
}

interface Label {
  id: string;
  name: string;
  color: string;
}

export interface Task {
  id: string;
  taskKey: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  dueDate: string | null;
  assignee: User | null;
  labels: Label[] | null;
  columnId: string;
}

interface TaskCardProps {
  task: Task;
  onClick: () => void;
}

export default function TaskCard({ task, onClick }: TaskCardProps) {
  const {
    setNodeRef,
    attributes,
    listeners,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: task.id,
    data: {
      type: 'Task',
      task,
    },
  });

  const style = {
    transition,
    transform: CSS.Transform.toString(transform),
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL': return 'text-red-500 bg-red-50 dark:bg-red-950/30';
      case 'HIGH': return 'text-orange-500 bg-orange-50 dark:bg-orange-950/30';
      case 'MEDIUM': return 'text-blue-500 bg-blue-50 dark:bg-blue-950/30';
      case 'LOW': return 'text-slate-500 bg-slate-50 dark:bg-slate-900/30';
      default: return 'text-slate-500 bg-slate-50 dark:bg-slate-900/30';
    }
  };

  if (isDragging) {
    return (
      <div
        ref={setNodeRef}
        style={style}
        className="opacity-50 border-2 border-primary bg-primary/5 rounded-xl h-32 w-full"
      />
    );
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={onClick}
      className="bg-surface rounded-xl p-4 shadow-sm border border-border cursor-grab active:cursor-grabbing hover:border-primary/50 transition-colors group relative"
    >
      <div className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity">
        <Button variant="ghost" size="icon" className="h-6 w-6">
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </div>

      <div className="flex flex-wrap gap-1.5 mb-3">
        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${getPriorityColor(task.priority)}`}>
          {task.priority}
        </span>
        {task.labels?.map((label) => (
          <span
            key={label.id}
            className="text-[10px] font-bold px-2 py-0.5 rounded-full text-white"
            style={{ backgroundColor: label.color }}
          >
            {label.name}
          </span>
        ))}
      </div>

      <h4 className="text-sm font-semibold mb-2 leading-tight text-foreground pr-6">
        {task.title}
      </h4>

      <div className="flex items-center justify-between mt-4">
        <div className="flex items-center gap-3 text-xs text-muted-foreground">
          {task.dueDate && (
            <div className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              <span>{format(new Date(task.dueDate), 'MMM d')}</span>
            </div>
          )}
          <div className="flex items-center gap-1">
            <MessageSquare className="h-3 w-3" />
            <span>0</span>
          </div>
        </div>

        {task.assignee && (
          <div
            className="h-6 w-6 rounded-full bg-primary/20 flex items-center justify-center text-[10px] font-bold text-primary"
            title={task.assignee.fullName}
          >
            {task.assignee.fullName.charAt(0)}
          </div>
        )}
      </div>
    </div>
  );
}
