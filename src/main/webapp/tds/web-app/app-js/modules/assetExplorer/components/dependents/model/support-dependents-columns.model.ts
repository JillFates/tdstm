import {GridColumnModel} from '../../../../../shared/model/data-list-grid.model';

export class SupportDependentsColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Class',
				property: 'assetType',
				type: 'text',
				filterable: true,
				width: 180
			},
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				filterable: true,
				width: 280
			},
			{
				label: 'Bundle',
				property: 'moveBundle.name',
				type: 'text',
				filterable: true,
				width: 320
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				filterable: true,
				width: 150
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				filterable: true,
				width: 150
			}
		];
	}
}

export enum DependentType {
	SUPPORT = 'support',
	DEPENDENT = 'dependent'
}
