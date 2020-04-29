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
				width: 160
			},
			{
				label: 'Asset Type',
				property: 'assetType',
				type: 'text',
				width: 120
			},
			{
				label: 'Asset Bundle',
				property: 'assetBundle',
				type: 'text',
				width: 160
			},
			{
				label: 'Asset Tags',
				property: 'tagsAsset',
				type: 'tags',
				width: 260
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				width: 120
			},
			{
				label: 'Depends On',
				property: 'dependentName',
				type: 'text',
				width: 160
			},
			{
				label: 'Dep Asset Type',
				property: 'dependentType',
				type: 'text',
				width: 160
			},
			{
				label: 'Dep Asset Bundle',
				property: 'dependentBundle',
				type: 'text',
				width: 180
			},
			{
				label: 'Dep Asset Tags',
				property: 'tagsDependent',
				type: 'tags',
				width: 220
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 100
			},
			{
				label: 'Comment',
				property: 'comment',
				type: 'text',
				width: 150
			},
			{
				label: 'Frequency',
				property: 'frequency',
				type: 'text',
				width: 120
			},
			{
				label: 'Direction',
				property: 'direction',
				type: 'text',
				width: 140
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
