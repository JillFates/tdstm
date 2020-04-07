import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

export class TagListColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				filterable: true,
				property: 'name',
				type: 'text',
				width: 170,
				locked: false
			},
			{
				label: 'Description',
				filterable: true,
				property: 'description',
				type: 'text',
				width: 380,
				locked: false
			},
			{
				label: 'Color',
				filterable: false,
				property: 'color',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Assets',
				filterable: true,
				property: 'assets',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: {'text-align': 'center'}
			},
			// Disable these two when data available on API.
			/*{
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
			},*/
			{
				label: 'Date Created',
				filterable: true,
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
				width: 160,
				locked: false
			},
			{
				label: 'Last Updated',
				filterable: true,
				property: 'lastModified',
				type: 'date',
				format: dateFormat,
				width: 160,
				locked: false
			},
		]
	}
}
