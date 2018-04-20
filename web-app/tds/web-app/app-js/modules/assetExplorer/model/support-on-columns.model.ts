import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

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
				width: 46,
				locked: true
			},
			{
				label: 'Frequency',
				property: 'dataFlowFreq',
				type: 'text',
				width: 100
			},
			{
				label: 'Class',
				property: 'assetClass',
				type: 'text',
				width: 130
			},
			{
				label: 'Name',
				property: 'assetName',
				type: 'text',
				width: 195
			},
			{
				label: 'Bundle',
				property: 'moveBundle',
				type: 'text',
				width: 195
			},
			{
				label: 'Type',
				property: 'dependencyType',
				type: 'text',
				width: 130
			},
			{
				label: 'Status',
				property: 'dependencyStatus',
				type: 'text',
				width: 130
			}
		];
	}
}

export class DependencySupportModel {
	public dataFlowFreq: string;
	public dependencyType: string;
	public dependencyStatus: string;
	public assetClass: any;
	public assetDepend: any;
}