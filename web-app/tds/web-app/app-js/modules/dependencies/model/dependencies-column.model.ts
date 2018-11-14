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
				width: 70
			},
			{
				label: 'Asset Type',
				property: 'dependentType',
				type: 'text',
				width: 70
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
				width: 70
			},
			{
				label: 'Dep Asset Type',
				property: 'dependentType',
				type: 'text',
				width: 70
			},
			{
				label: 'Dep Asset Bundle',
				property: 'assetBundle',
				type: 'text',
				width: 70
			},
			{
				label: 'MultiField1',
				property: 'multiField1',
				type: 'text',
				width: 70
			},
			{
				label: 'MultiField2',
				property: 'multiField2',
				type: 'text',
				width: 70
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 70
			}
		];
	}
}
