import {IGraphTask} from '../../../../modules/taskManager/model/graph-task.model';
import {IDiagramContextMenuOption} from './legacy-diagram-context-menu.model';

export interface IDiagramLayoutModel {
	nodeDataArray: IGraphTask[];
	linksPath: ILinkPath[];
	currentUserId: string | number;
	ctxMenuOptions: IDiagramContextMenuOption[];
}

export interface ILinkPath {
	from: number | string;
	to: number | string;
}
