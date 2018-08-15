import {GridColumnModel} from '../../../../../shared/model/data-list-grid.model';

/**
 * Defines the columns
 */
export class TaskCommentColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 70,
				locked: true
			},
			{
				label: 'Task #',
				property: 'commentInstance.taskNumber',
				type: 'text',
				width: 70
			},
			{
				label: 'Task/Comment',
				property: 'commentInstance.comment',
				type: 'text',
				width: 200
			},
			{
				label: 'Status',
				property: 'commentInstance.status',
				type: 'text',
				width: 105
			},
			{
				label: 'Category',
				property: 'commentInstance.category',
				type: 'text',
				width: 105
			},
			{
				label: 'Assigned To',
				property: 'assignedTo',
				type: 'text',
				width: 105
			}
		];
	}
}