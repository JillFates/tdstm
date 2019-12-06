import {IGraphTask} from './graph-task.model';

export interface ITaskEvent {
	name?: string;
	task: IGraphTask
}
