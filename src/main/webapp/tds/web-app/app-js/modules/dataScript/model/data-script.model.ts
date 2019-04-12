export const COLUMN_MIN_WIDTH = 360;

export class DataScriptColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 70,
				locked: true
			}, {
				label: 'Name',
				property: 'name',
				type: 'text',
				locked: true
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 220
			}, {
				label: 'Description',
				property: 'description',
				type: 'text'
			}, {
				label: 'Mode',
				property: 'modeFormat',
				type: 'text',
				width: 200
			}, {
				label: 'Date Create',
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
				width: 170
			}, {
				label: 'Last Modified',
				property: 'lastUpdated',
				type: 'date',
				format: dateFormat,
				width: 170
			}
		];
	}
};

export class DataScriptModel {
	id?: number;
	name?: string;
	description?: string;
	mode?: DataScriptMode;
	etlSourceCode?: string;
	provider?: {
		id?: number,
		name: string
	};
	dateCreated?: Date;
	lastUpdated?: Date;
	sampleFilename?: string;
	originalSampleFilename?: string;
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
};

/**
 * Base Class for Reference Purposes
 * The Model is being retrieved from the Server see: WsDataScriptController.sampleData
 */
export class SampleDataModel {
	columns?: any[];
	data?: any[];
	gridHeight ?= 23;
	errors?: Array<string>;
	constructor(columns: any, data: any) {
		this.columns = columns;
		this.data = data;
	}
};