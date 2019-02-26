import {GridColumnModel} from '../../../model/data-list-grid.model';

/**
 * Defines the columns.
 */
export class SupportOnColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 80,
				locked: true
			},
			{
				label: 'Frequency',
				property: 'dataFlowFreq',
				type: 'text',
				width: 90
			},
			{
				label: 'Class',
				property: 'assetClass',
				type: 'text',
				width: 120
			},
			{
				label: 'Name',
				property: 'assetName',
				type: 'text',
				width: 235
			},
			{
				label: 'Bundle',
				property: 'moveBundle',
				type: 'text',
				width: 195
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				width: 120
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 120
			}
		];
	}
}

export class DependencySupportModel {
	public id: number;
	public dataFlowFreq: string;
	public type: string;
	public status: string;
	public assetClass: any;
	public assetDepend: any;
	public dependencyType: string;
	public comment: string;
}