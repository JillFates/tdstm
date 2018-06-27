import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

export class TagListColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'Name',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Description',
				property: 'Description',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Color',
				property: 'Color',
				type: 'text',
				width: 80,
				locked: false
			},
			{
				label: 'Assets',
				property: 'Assets',
				type: 'number',
				width: 130,
				locked: false,
				cellStyle: {'text-align': 'center'}
			},
			{
				label: 'Dependencies',
				property: 'Dependencies',
				type: 'number',
				width: 130,
				locked: false
			},
			{
				label: 'Tasks',
				property: 'Tasks',
				type: 'number',
				width: 130,
				locked: false
			},
			{
				label: 'Date Created',
				property: 'DateCreated',
				type: 'date',
				format: '{0:d}',
				width: 160,
				locked: false
			},
			{
				label: 'Last Modified',
				property: 'LastModified',
				type: 'date',
				format: '{0:d}',
				width: 160,
				locked: false
			},
		]
	}
}