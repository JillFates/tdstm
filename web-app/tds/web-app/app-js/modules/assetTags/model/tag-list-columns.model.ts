import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

export class TagListColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Color',
				property: 'color',
				type: 'text',
				width: 80,
				locked: false
			},
			{
				label: 'Assets',
				property: 'assets',
				type: 'number',
				width: 130,
				locked: false,
				cellStyle: {'text-align': 'center'}
			},
			{
				label: 'Dependencies',
				property: 'dependencies',
				type: 'number',
				width: 130,
				locked: false
			},
			{
				label: 'Tasks',
				property: 'tasks',
				type: 'number',
				width: 130,
				locked: false
			},
			{
				label: 'Date Created',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 160,
				locked: false
			},
			{
				label: 'Last Modified',
				property: 'lastModified',
				type: 'date',
				format: '{0:d}',
				width: 160,
				locked: false
			},
		]
	}
}