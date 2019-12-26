export interface ITaskHighlightQuery {
	persons?: string;
	teams?: string;
	ownerAndSmes?: string;
	environments?: string;
	text?: string;
	tag?: string;
}

export interface ITaskHighlightOption {
	persons?: IFilterOption[];
	teams?: IFilterOption[];
	ownerAndSmes?: IFilterOption[];
	environments?: IFilterOption[];
}

export interface IFilterOption {
	id?: number | string;
	name?: string;
}