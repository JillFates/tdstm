export const COLUMN_MIN_WIDTH = 360;

export class DataScriptColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 120
			}, {
				label: 'Name',
				property: 'name',
				type: 'text'
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text'
			}, {
				label: 'Description',
				property: 'description',
				type: 'text'
			}, {
				label: 'Date Create',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 170
			}, {
				label: 'Last Modified',
				property: 'lastUpdated',
				type: 'date',
				format: '{0:d}',
				width: 170
			}
		];
	}
}

export class DataScriptModel {
	id?: number;
	name: string;
	description: string;
	mode: DataScriptMode;
	etlSourceCode?: string;
	provider?: {
		id?: number,
		name: string
	};
	dateCreated?: Date;
	lastUpdated?: Date;
}

export const Flatten = filter => {
	const filters = filter.filters;
	if (filters) {
		return filters.reduce((acc, curr) => acc.concat(curr.filters ? Flatten(curr) : [curr]), []);
	}
	return [];
};

export enum ActionType {
	VIEW,
	CREATE,
	EDIT
};

export enum DataScriptMode {
	IMPORT,
	EXPORT
}