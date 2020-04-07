import { GridColumnModel } from '../../../shared/model/data-list-grid.model';

export const taskListColumnsModel: Array<GridColumnModel> = [
	{
		label: 'Task',
		property: 'taskNumber',
		type: 'text',
		width: 180,
		locked: false,
		filterable: true,
		cellClass: 'is-grid-link',
		resizable: true
	},
	{
		label: 'Description',
		property: 'comment',
		type: 'text',
		width: 240,
		locked: false,
		filterable: true,
		resizable: true
	},
	{
		label: 'userSelectedCol0',
		property: 'userSelectedCol0',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true,
		resizable: true
	},
	{
		label: 'userSelectedCol1',
		property: 'userSelectedCol1',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true,
		resizable: true
	},
	{
		label: 'Updated',
		property: 'updatedTime',
		type: 'text',
		width: 120,
		filterable: false,
		locked: false,
		resizable: true
	},
	{
		label: 'Due Date',
		property: 'dueDate',
		type: 'date',
		width: 180,
		locked: false,
		filterable: true,
		resizable: true
	},
	{
		label: 'Status',
		property: 'status',
		type: 'text',
		width: 180,
		locked: false,
		cellClass: 'task-status',
		filterable: true,
		resizable: true
	},
	{
		label: 'userSelectedCol2',
		property: 'userSelectedCol2',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true
	},
	{
		label: 'userSelectedCol3',
		property: 'userSelectedCol3',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true,
		resizable: true
	},
	{
		label: 'userSelectedCol4',
		property: 'userSelectedCol4',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true,
		resizable: true
	},
	{
		label: 'Suc.',
		property: 'successors',
		type: 'number',
		width: 180,
		locked: false,
		resizable: true
	},
	{
		label: 'Score',
		property: 'score',
		type: 'number',
		width: 180,
		locked: false,
		resizable: true
	},
];
