export const COLUMN_MIN_WIDTH = 200;

export class DataScriptColumnModel {
	columns: any[];
	constructor() {
		this.columns = [{
			label: 'Action',
			type: 'action',
			width: 80
		}, {
			label: 'Name',
			property: 'name',
			type: 'text'
		}, {
			label: 'Provider',
			property: 'provider.name',
			type: 'object'
		}, {
			label: 'Description',
			property: 'description',
			type: 'text'
		}, {
			label: 'Date Create',
			property: 'dateCreated',
			type: 'date',
			width: 80
		}, {
			label: 'Last Modified',
			property: 'lastModified',
			type: 'date',
			width: 80
		}];
	}
}

export class DataScriptModel {
	name: String;
	provider?: {
		id: number,
		name: String
	};
	description: String;
	mode: ModeType;
	view?: {
		id: number,
		name: String
	};
	dateCreated?: Date;
	lastModified?: Date;
}

export const Flatten = filter => {
	const filters = filter.filters;
	if (filters) {
		return filters.reduce((acc, curr) => acc.concat(curr.filters ? Flatten(curr) : [curr]), []);
	}
	return [];
};

export enum ModalType {
	VIEW,
	CREATE,
	EDIT
};

export enum ModeType {
	IMPORT,
	EXPORT
}