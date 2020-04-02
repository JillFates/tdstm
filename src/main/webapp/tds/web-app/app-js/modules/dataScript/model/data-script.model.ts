import {FilterType} from 'tds-component-library';

export const COLUMN_MIN_WIDTH = 360;

export class DataScriptColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 160
			},
			{
				label: 'Provider',
				property: 'provider.name',
				filterType: FilterType.text,
				width: 150
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text
			},
			{
				label: 'Mode',
				property: 'modeFormat',
				filterType: FilterType.text,
				width: 80
			},
			{
				label: 'Date Created',
				property: 'dateCreated',
				filterType: FilterType.date,
				format: dateFormat,
				width: 120
			},
			{
				label: 'Last Updated',
				property: 'lastUpdated',
				filterType: FilterType.date,
				format: dateFormat,
				width: 120
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
	isAutoProcess?: boolean;
	useWithAssetActions?: boolean;
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
