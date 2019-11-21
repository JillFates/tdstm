import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

/**
 * Defines the columns
 */
export class TaskSuccessorPredecessorColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Category',
				property: 'category',
				type: 'text',
				width: 60
			},
			{
				label: 'Task #',
				property: 'taskNumber',
				type: 'text',
				width: 50
			},
			{
				label: 'Task',
				property: 'desc',
				type: 'text',
				width: 150
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 75
			}
		];
	}
}
