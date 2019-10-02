import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';

export interface ITaskContextMenuModel {
	selectedNode?: IGraphTask;
	currentUserId?: string | number;
	mousePt?: {x: string, y: string};
}

export interface IHideBtn {
	pending?: boolean;
	started?: boolean;
	completed?: boolean;
	ready?: boolean;
	reset?: boolean;
	invoke?: boolean;
}

export class HideBtn implements IHideBtn {
	constructor(
		public pending?: boolean,
		public started?: boolean,
		public completed?: boolean,
		public ready?: boolean,
		public reset?: boolean,
		public invoke?: boolean,
	) {}
}
