export interface ITaskHighlightQuery {
	eventId?: number;
	viewUnpublished?: number;
	assignedPersonId?: number;
	teams?: string;
	ownerSmeId?: string;
	environments?: string;
	taskText?: string;
	tagIds?: [];
	tagMatch?: string;
	criticalPathMode?: string;
	cyclicalPath?: number;
	withActions?: number;
	withTmdActions?: number;
}

export class TaskHighlightQueryModel implements ITaskHighlightQuery {
	constructor(
		public eventId?: number,
		public viewUnpublished?: number,
		public assignedPersonId?: number,
		public teams?: string,
		public ownerSmeId?: string,
		public environments?: string,
		public taskText?: string,
		public tagIds?: [],
		public tagMatch?: string,
		public criticalPathMode?: string,
		public cyclicalPath = 0,
		public withActions = 0,
		public withTmdActions = 0,
	) {}
}

export interface ITaskHighlightOption {
	persons?: IFilterOption[];
	teams?: IFilterOption[];
	ownersAndSmes?: IFilterOption[];
	environments?: IFilterOption[];
	tags?: any[];
	showCycles?: boolean;
	withActions?: boolean;
	withTmdActions?: boolean;
}

export interface IFilterOption {
	id?: number | string;
	name?: string;
}