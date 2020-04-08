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
	cyclicalPath?: any;
	withActions?: any;
	withTmdActions?: any;
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