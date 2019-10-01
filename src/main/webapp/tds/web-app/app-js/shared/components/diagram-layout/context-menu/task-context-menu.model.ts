import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';

export interface ITaskContextMenuModel {
	selectedNode?: IGraphTask;
	currentUserId?: string | number;
	mousePt?: {x: string, y: string};
}

export interface IHideBtn {
	hold: boolean;
	start: boolean;
	done: boolean;
	reset: boolean;
	invoke: boolean;
}
