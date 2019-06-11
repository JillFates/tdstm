import { GridColumnModel } from '../../../shared/model/data-list-grid.model';

export const taskListColumnsModel: Array<GridColumnModel> = [
	{
		label: 'Task',
		property: 'taskNumber',
		type: 'number',
		width: 70,
		locked: false
	},
	{
		label: 'Description',
		property: 'comment',
		type: 'text',
		width: 200,
		locked: false
	},
	{
		label: 'userSelectedCol0',
		property: 'userSelectedCol0',
		type: 'text',
		width: 130,
		locked: false,
		columnMenu: true
	},
	{
		label: 'userSelectedCol1',
		property: 'userSelectedCol1',
		type: 'text',
		width: 130,
		locked: false,
		columnMenu: true
	},
	{
		label: 'Updated',
		property: 'updatedTime',
		type: 'text',
		width: 80,
		locked: false,
		cellClass: 'task-updated'
	},
	{
		label: 'Due Date',
		property: 'dueDate',
		type: 'text',
		width: 80,
		locked: false
	},
	{
		label: 'Status',
		property: 'status',
		type: 'text',
		width: 80,
		locked: false,
		cellClass: 'task-status'
	},
	{
		label: 'userSelectedCol2',
		property: 'userSelectedCol2',
		type: 'text',
		width: 130,
		locked: false,
		columnMenu: true
	},
	{
		label: 'userSelectedCol3',
		property: 'userSelectedCol3',
		type: 'text',
		width: 130,
		locked: false,
		columnMenu: true
	},
	{
		label: 'userSelectedCol4',
		property: 'userSelectedCol4',
		type: 'text',
		width: 130,
		locked: false,
		columnMenu: true
	},
	{
		label: 'Suc.',
		property: 'assetName',
		type: 'text',
		width: 50,
		locked: false
	},
	{
		label: 'Score',
		property: 'score',
		type: 'number',
		width: 80,
		locked: false
	},
];
