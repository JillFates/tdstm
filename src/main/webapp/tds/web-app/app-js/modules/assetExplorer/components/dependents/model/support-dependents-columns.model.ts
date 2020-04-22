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
				width: 90
			},
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				filterable: true,
				width: 90
			},
			{
				label: 'Bundle',
				property: 'moveBundle.name',
				type: 'text',
				filterable: true,
				width: 90
			},
			{
				label: 'Type',
				property: 'type',
				type: 'text',
				filterable: true,
				width: 90
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				filterable: true,
				width: 90
			}
		];
	}
}

export enum DependentType {
	SUPPORT = 'support',
	DEPENDENT = 'dependent'
}
