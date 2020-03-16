import { GridColumnModel } from '../../../shared/model/data-list-grid.model';

export const taskListColumnsModel: Array<GridColumnModel> = [
	{
		label: '',
		property: 'actionColumn',
		type: 'action',
		width: 30,
		locked: false,
		sortable: false,
		headerClass: ['no-sort-header']
	},
	{
		label: 'Task',
		property: 'taskNumber',
		type: 'text',
		width: 80,
		locked: false,
		filterable: true,
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
		width: 100,
		locked: false,
		cellClass: 'task-updated',
		sortable: false,
		resizable: true
	},
	{
		label: 'Due Date',
		property: 'dueDate',
		type: 'text',
		width: 100,
		locked: false,
		filterable: true,
		resizable: true
	},
	{
		label: 'Status',
		property: 'status',
		type: 'text',
		width: 80,
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
		filterable: true,
		resizable: true
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
		width: 80,
		locked: false,
		resizable: true
	},
	{
		label: 'Score',
		property: 'score',
		type: 'number',
		width: 80,
		locked: false,
		resizable: true
	},
];
