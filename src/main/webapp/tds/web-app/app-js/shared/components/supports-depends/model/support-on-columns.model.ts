import {GridColumnModel} from '../../../model/data-list-grid.model';
import {RecordState} from '../../../utils/data-grid-operations.helper';

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
				property: 'assetClassName',
				type: 'text',
				filterable: true,
				width: 120
			},
			{
				label: 'Name',
				property: 'assetName',
				type: 'text',
				filterable: true,
				width: 235
			},
			{
				label: 'Bundle',
				property: 'moveBundleName',
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
	public assetName?: string;
	public assetClassName?: string;
	public moveBundleName?: string;
	public assetDepend: any;
	public dependencyType: string;
	public comment: string;
	public recordState: RecordState;
}
