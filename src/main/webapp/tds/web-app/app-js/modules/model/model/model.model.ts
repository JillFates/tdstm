import {FilterType} from 'tds-component-library';

export class ModelColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'modelName',
				filterType: FilterType.text,
				width: 246,
			},
			{
				label: 'Manufacturer',
				property: 'manufacturer',
				filterType: FilterType.text,
				width: 'auto',
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 'auto',
			},
			{
				label: 'Asset Type',
				property: 'assetType',
				filterType: FilterType.text,
				width: 'auto',
			},
			{
				label: 'Last Modified',
				property: 'lastModified',
				filterType: FilterType.date,
				format: dateFormat,
				width: 120
			},
			{
				label: 'No Of Connectors',
				property: 'connectors',
				filterType: FilterType.number,
				width: 'auto',
			},
			{
				label: 'Assets',
				property: 'assetsCount',
				filterType: FilterType.number,
				width: 'auto',
			},
			{
				label: 'Version',
				property: 'sourceTDSVersion',
				filterType: FilterType.number,
				width: 'auto',
			},
			{
				label: 'Source TDS',
				property: 'sourceTDS',
				filterType: FilterType.number,
				width: 'auto',
			},
			{
				label: 'Model Status',
				property: 'modelStatus',
				filterType: FilterType.text,
				width: 'auto',
			},
		];
	}
}

export class ModelModel {
	id?: number;
	modelName: string;
	manufacturer?: string;
	description?: string;
	assetType?: string;
	lastModified?: Date;
	connectors?: number;
	assetsCount?: number;
	sourceTDSVersion?: number;
	sourceTDS?: number;
	modelStatus?: string;
	height?: number;
	weight?: number;
	depth?: number;
	width?: number;
	layoutStyle?: string;
	modelFamily?: string;
	endOfLifeStatus?: string;
	cpuType?: string;
	cpuCount?: number;
	memorySize?: number;
	storageSize?: number;
	sourceURL?: string;
	productLine?: string;
	endOfLifeDate?: Date;
	powerUse?: number;
	powerNameplate?: number;
	powerDesign?: number;
	roomObject?: boolean;
	validatedBy?: string;
	usize = 1;
	updatedBy?: string;
	createdBy?: string;
}
