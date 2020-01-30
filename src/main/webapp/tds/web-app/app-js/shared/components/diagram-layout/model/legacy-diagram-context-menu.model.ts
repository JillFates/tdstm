import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';
import {IContextMenuIcon} from './legacy-context-menu-icon.model';

export interface IDiagramContextMenuModel {
	selectedNode?: IGraphTask;
	currentUser?: any;
	mousePt?: {x: string, y: string};
	options?: IDiagramContextMenuOption;
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

export interface IDiagramContextMenuOption {
	containerComp?: string;
	fields: IDiagramContextMenuField[]
}

export interface IDiagramContextMenuField {
	label?: string;
	event?: string;
	icon?: IContextMenuIcon;
	status?: string;
	permission?: string;
}

export const enum ContainerComp {
	NEIGHBORHOOD = 'NeighborhoodComponent',
	ARCHITECTURE_GRAPH = 'ArchitectureGraph',
	TASK_TIMELINE = 'TaskTimeline'
}
