import {FilterType} from 'tds-component-library';
import {Connector} from '../../../shared/components/connector/model/connector.model';
import {Aka, AkaChanges} from '../../../shared/components/aka/model/aka.model';

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
				width: 200,
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 200,
			},
			{
				label: 'Asset Type',
				property: 'assetType',
				filterType: FilterType.text,
				width: 200,
			},
			{
				label: 'Last Modified',
				property: 'lastModified',
				filterType: FilterType.date,
				format: dateFormat,
				width: 150
			},
			{
				label: 'No Of Connectors',
				property: 'connectors',
				filterType: FilterType.number,
				width: 170,
			},
			{
				label: 'Assets',
				property: 'assetsCount',
				filterType: FilterType.number,
				width: 100,
			},
			{
				label: 'Version',
				property: 'sourceTDSVersion',
				filterType: FilterType.number,
				width: 100,
			},
			{
				label: 'Source TDS',
				property: 'sourceTDS',
				filterType: FilterType.number,
				width: 140,
			},
			{
				label: 'Model Status',
				property: 'modelStatus',
				filterType: FilterType.text,
				width: 140,
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
	endOfLifeDate?: string;
	powerUse = 0;
	powerNameplate = 0;
	powerDesign = 0;
	roomObject?: boolean;
	validatedBy?: string;
	usize?: number;
	updatedBy?: string;
	createdBy?: string;
	modelConnectors: Connector[] = [];
	removedConnectors: number[] = [];
	connectorCount?: number;
	akaChanges?: AkaChanges;
	useImage = 0;
}
