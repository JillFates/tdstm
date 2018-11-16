import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

/**
 * Defines the columns
 */
export class DependenciesColumnModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Asset',
				property: 'assetName',
				type: 'text',
				width: 100
			},
			{
				label: 'Asset Type',
				property: 'dependentType',
				type: 'text',
				width: 100
			},
			{
				label: 'Bundle',
				property: 'assetBundle',
				type: 'text',
				width: 70
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				width: 70
			},
			{
				label: 'Depends On',
				property: 'dependentName',
				type: 'text',
				width: 100
			},
			{
				label: 'Dep Asset Type',
				property: 'dependentType',
				type: 'text',
				width: 110
			},
			{
				label: 'Dep Asset Bundle',
				property: 'assetBundle',
				type: 'text',
				width: 120
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 70
			},
			{
				label: 'Comment',
				property: 'comment',
				type: 'text',
				width: 100
			},
			{
				label: 'Frequency',
				property: 'frequency',
				type: 'text',
				width: 100
			},
			{
				label: 'Direction',
				property: 'direction',
				type: 'text',
				width: 100
			},
			{
				label: 'C1',
				property: 'c1',
				type: 'text',
				width: 100
			},
			{
				label: 'C2',
				property: 'c2',
				type: 'text',
				width: 100
			},
			{
				label: 'C3',
				property: 'c3',
				type: 'text',
				width: 100
			},
			{
				label: 'C4',
				property: 'c4',
				type: 'text',
				width: 100
			}
		];
	}
}
