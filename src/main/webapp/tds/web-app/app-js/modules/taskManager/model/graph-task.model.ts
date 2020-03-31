export interface IGraphNode {
	task?: IGraphTask;
	successors?: any[];
}

export interface IGraphTask {
	id?: number | string;
	key?: number | string;
	number?: number | string;
	label?: string;
	style?: string;
	color?: string;
	category?: string;
	fillcolor?: string;
	fontcolor?: string;
	fontsize?: string;
	status?: string;
	tooltip?: string;
	successors?: number[];
	name?: string;
	icon?: string;
	actFinish?: any;
	actualStart?: any;
	apiAction?: any;
	apiActionCompletedAt?: string;
	apiActionInvokedAt?: string;
	apiActionSettings?: any;
	asset?: any;
	assetEntity?: IAssetEntity;
	assignedTo?: string;
	attribute?: string;
	autoGenerated?: boolean;
	comment?: string;
	commentCode?: any;
	commentKey?: string;
	commentType?: string;
	constraintTime?: any;
	constraintType?: any;
	createdBy?: any;
	dateCreated?: string;
	dateResolved?: string;
	displayOption?: string;
	dueDate?: string;
	duration?: number;
	durationLocked?: boolean;
	durationScale?: IDurationScale | string;
	estFinish?: string;
	estStart?: string;
	hasAction?: boolean;
	hardAssigned?: number;
	instructionsLink?: any;
	criticalPath?: boolean;
	isPublished?: boolean;
	isAutomatic?: boolean;
	lastUpdated?: string;
	moveEvent?: IMoveEvent;
	mustVerify?: number;
	notes?: any[];
	percentageComplete?: number;
	priority?: number;
	project?: IProject;
	recipe?: string;
	resolution?: string;
	resolvedBy?: string;
	role?: string;
	score?: number;
	sendNotification?: boolean;
	slack?: any;
	statusUpdated?: any;
	startInitial?: number;
	taskBatch?: ITaskBatch;
	taskDependencies?: ITaskDependencies[];
	taskSpec?: number;
	predecessorIds?: number[];
	predecessorList?: number[];
	successorList?: number[];
	team?: string;
	assetType?: string;
}

interface IAssetEntity {
	id?: number;
}

interface IDurationScale {
	enumType?: string;
	name?: string;
}

interface ITaskBatch {
	id?: number;
}

interface ITaskDependencies {
	id?: number;
}

interface IProject {
	id?: number;
}

interface IMoveEvent {
	id?: number;
}

export interface IMoveEventTask {
	cycles?: number[][];
	sinks?: number[];
	starts?: number[];
	startDate?: string;
	tasks?: IGraphTask[];
}

export const enum TASK_OPTION_LABEL {
	HOLD = 'Hold',
	START = 'Start',
	DONE = 'Done',
	RESET = 'Reset',
	INVOKE = 'Invoke',
	ASSET_DETAILS = 'Asset Details',
	VIEW = 'View',
	EDIT = 'Edit',
	ASSIGN_TO_ME = 'Assign to me',
	NEIGHBORHOOD = 'Neighborhood'
}

export const enum TASK_TOOLTIP_FIELDS {
	STATUS = 'Status',
	ASSIGNED_TO = 'Assigned To',
	TEAM = 'Team',
	ASSET_CLASS = 'Asset Class',
	ASSET_NAME = 'Asset Name'
}

export interface ILinkPath {
	from: number | string;
	to: number | string;
}

export const enum ContainerComp {
	NEIGHBORHOOD = 'NeighborhoodComponent',
	ARCHITECTURE_GRAPH = 'ArchitectureGraph',
	TASK_TIMELINE = 'TaskTimeline'
}