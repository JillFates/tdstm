import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

/**
 * Defines the columns
 */
export class TaskNotesColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Date Created',
				property: 'dateCreated',
				type: 'text',
				width: 70
			},
			{
				label: 'Created By',
				property: 'createdBy',
				type: 'text',
				width: 80
			},
			{
				label: 'Notes',
				property: 'note',
				type: 'text',
				width: 200
			}
		];
	}
}