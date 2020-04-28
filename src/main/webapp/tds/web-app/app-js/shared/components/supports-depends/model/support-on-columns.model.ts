import {GridColumnModel} from '../../../model/data-list-grid.model';

/**
 * Defines the columns.
 */
export class SupportOnColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Frequency',
				property: 'dataFlowFreq',
				type: 'text',
				filterable: true,
				width: 90
			},
			{
				label: 'Class',
				property: 'assetClass.text',
				type: 'text',
				filterable: true,
				width: 120
			},
			{
				label: 'Name',
				property: 'assetDepend.text',
				type: 'text',
				filterable: true,
				width: 235
			},
			{
				label: 'Bundle',
				property: 'assetDepend.moveBundle.name',
				type: 'text',
				filterable: true,
				width: 195
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				filterable: true,
				width: 120
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				filterable: true,
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
