import { useMemo, useState } from 'react';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragStartEvent,
  type DragOverEvent,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import TaskCard, { type Task } from './TaskCard';
import { Button } from '../ui/Button';
import { Plus, MoreHorizontal } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../api/axios';
import toast from 'react-hot-toast';

interface Column {
  id: string;
  name: string;
  position: number;
}

interface KanbanBoardProps {
  columns: Column[];
  tasks: Task[];
  projectId: string;
  onTaskClick: (task: Task) => void;
}

// Internal component for the droppable column
function ColumnContainer({
  column,
  tasks,
  projectId,
  onTaskClick,
}: {
  column: Column;
  tasks: Task[];
  projectId: string;
  onTaskClick: (task: Task) => void;
}) {
  const queryClient = useQueryClient();
  const [isAdding, setIsAdding] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState('');

  const createTaskMutation = useMutation({
    mutationFn: async ({ title, columnId }: { title: string, columnId: string }) => {
      await api.post(`/projects/${projectId}/tasks`, { title, columnId });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board-tasks', projectId] });
      setIsAdding(false);
      setNewTaskTitle('');
    }
  });

  const { setNodeRef, attributes, listeners, transform, transition, isDragging } =
    useSortable({
      id: column.id,
      data: {
        type: 'Column',
        column,
      },
    });

  const style = {
    transition,
    transform: CSS.Transform.toString(transform),
  };

  if (isDragging) {
    return (
      <div
        ref={setNodeRef}
        style={style}
        className="opacity-50 border-2 border-primary bg-primary/5 rounded-2xl w-80 h-[500px] flex-shrink-0"
      />
    );
  }

  const taskIds = useMemo(() => tasks.map((t) => t.id), [tasks]);

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="bg-muted/50 rounded-2xl w-80 flex-shrink-0 flex flex-col h-full max-h-full overflow-hidden border border-border"
    >
      {/* Column Header */}
      <div
        {...attributes}
        {...listeners}
        className="p-4 flex items-center justify-between cursor-grab active:cursor-grabbing"
      >
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-foreground">{column.name}</h3>
          <span className="flex items-center justify-center bg-surface text-muted-foreground text-xs font-medium w-5 h-5 rounded-full border border-border">
            {tasks.length}
          </span>
        </div>
        <Button variant="ghost" size="icon" className="h-8 w-8 text-muted-foreground hover:text-foreground">
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </div>

      {/* Tasks Container */}
      <div className="p-3 flex-1 overflow-y-auto flex flex-col gap-3">
        <SortableContext items={taskIds} strategy={verticalListSortingStrategy}>
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} onClick={() => onTaskClick(task)} />
          ))}
        </SortableContext>
      </div>
      
      {isAdding ? (
        <div className="p-3 pt-0">
          <input
            autoFocus
            type="text"
            placeholder="What needs to be done?"
            className="w-full text-sm rounded-md border border-border bg-background p-2 mb-2 focus:outline-none focus:ring-1 focus:ring-primary"
            value={newTaskTitle}
            onChange={(e) => setNewTaskTitle(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && newTaskTitle.trim()) {
                createTaskMutation.mutate({ title: newTaskTitle, columnId: column.id });
              } else if (e.key === 'Escape') {
                setIsAdding(false);
                setNewTaskTitle('');
              }
            }}
          />
          <div className="flex items-center gap-2">
            <Button size="sm" onClick={() => {
              if (newTaskTitle.trim()) {
                createTaskMutation.mutate({ title: newTaskTitle, columnId: column.id });
              }
            }}>Add</Button>
            <Button size="sm" variant="ghost" onClick={() => {
              setIsAdding(false);
              setNewTaskTitle('');
            }}>Cancel</Button>
          </div>
        </div>
      ) : (
        <div className="p-3 pt-0">
          <Button variant="ghost" className="w-full justify-start text-muted-foreground hover:text-foreground" onClick={() => setIsAdding(true)}>
            <Plus className="mr-2 h-4 w-4" /> Add Task
          </Button>
        </div>
      )}
    </div>
  );
}

export default function KanbanBoard({ columns, tasks: initialTasks, projectId, onTaskClick }: KanbanBoardProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);
  const [activeTask, setActiveTask] = useState<Task | null>(null);
  const queryClient = useQueryClient();

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 3, // 3px movement before drag starts
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const moveTaskMutation = useMutation({
    mutationFn: async ({ taskId, targetColumnId, position }: { taskId: string, targetColumnId: string, position: number }) => {
      await api.put(`/tasks/${taskId}/move`, { targetColumnId, position });
    },
    onError: () => {
      toast.error('Failed to move task');
      queryClient.invalidateQueries({ queryKey: ['board-tasks', projectId] });
    }
  });

  const onDragStart = (event: DragStartEvent) => {
    if (event.active.data.current?.type === 'Task') {
      setActiveTask(event.active.data.current.task);
    }
  };

  const onDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    if (!over) return;

    const activeId = active.id;
    const overId = over.id;

    if (activeId === overId) return;

    const isActiveTask = active.data.current?.type === 'Task';
    const isOverTask = over.data.current?.type === 'Task';
    const isOverColumn = over.data.current?.type === 'Column';

    if (!isActiveTask) return;

    // Dropping a Task over another Task
    if (isActiveTask && isOverTask) {
      setTasks((tasks) => {
        const activeIndex = tasks.findIndex((t) => t.id === activeId);
        const overIndex = tasks.findIndex((t) => t.id === overId);

        if (tasks[activeIndex].columnId !== tasks[overIndex].columnId) {
          const newTasks = [...tasks];
          newTasks[activeIndex].columnId = tasks[overIndex].columnId;
          return arrayMove(newTasks, activeIndex, overIndex);
        }

        return arrayMove(tasks, activeIndex, overIndex);
      });
    }

    // Dropping a Task over an empty Column
    if (isActiveTask && isOverColumn) {
      setTasks((tasks) => {
        const activeIndex = tasks.findIndex((t) => t.id === activeId);
        const newTasks = [...tasks];
        newTasks[activeIndex].columnId = overId as string;
        return arrayMove(newTasks, activeIndex, activeIndex);
      });
    }
  };

  const onDragEnd = (event: DragEndEvent) => {
    setActiveTask(null);
    const { active, over } = event;
    if (!over) return;

    const activeId = active.id;
    const overId = over.id;

    if (activeId === overId) return;

    const isActiveTask = active.data.current?.type === 'Task';
    if (isActiveTask) {
      const activeIndex = tasks.findIndex((t) => t.id === activeId);
      const activeTask = tasks[activeIndex];
      
      // Calculate new position
      const columnTasks = tasks.filter(t => t.columnId === activeTask.columnId);
      const newPosition = columnTasks.findIndex(t => t.id === activeId);

      // Call API
      moveTaskMutation.mutate({
        taskId: activeId as string,
        targetColumnId: activeTask.columnId,
        position: newPosition
      });
    }
  };

  return (
    <div className="flex h-full w-full overflow-x-auto p-4 gap-6 items-start">
      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={onDragStart}
        onDragOver={onDragOver}
        onDragEnd={onDragEnd}
      >
        {columns.map((column) => (
          <ColumnContainer
            key={column.id}
            column={column}
            projectId={projectId}
            tasks={tasks.filter((task) => task.columnId === column.id)}
            onTaskClick={onTaskClick}
          />
        ))}

        <DragOverlay>
          {activeTask ? <TaskCard task={activeTask} onClick={() => {}} /> : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
