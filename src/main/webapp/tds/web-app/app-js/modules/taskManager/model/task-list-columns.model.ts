import { GridColumnModel } from '../../../shared/model/data-list-grid.model';

export const taskListColumnsModel: Array<GridColumnModel> = [
	{
		label: '',
		property: 'actionColumn',
		type: 'action',
		width: 60,
		locked: false,
		resizable: false,
		sortable: false,
		headerClass: ['no-sort-header', 'action-column']
	},
	{
		label: 'Task',
		property: 'taskNumber',
		type: 'text',
		width: 180,
		locked: false,
		filterable: true
	},
	{
		label: 'Description',
		property: 'comment',
		type: 'text',
		width: 200,
		locked: false,
		filterable: true
	},
	{
		label: 'userSelectedCol0',
		property: 'userSelectedCol0',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true
	},
	{
		label: 'userSelectedCol1',
		property: 'userSelectedCol1',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true
	},
	{
		label: 'Updated',
		property: 'updatedTime',
		type: 'text',
		width: 180,
		filterable: false,
		locked: false
	},
	{
		label: 'Due Date',
		property: 'dueDate',
		type: 'text',
		width: 180,
		locked: false,
		filterable: true
	},
	{
		label: 'Status',
		property: 'status',
		type: 'text',
		width: 180,
		locked: false,
		cellClass: 'task-status',
		filterable: true
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
		filterable: true
	},
	{
		label: 'userSelectedCol4',
		property: 'userSelectedCol4',
		type: 'text',
		width: 180,
		locked: false,
		columnMenu: true,
		filterable: true
	},
	{
		label: 'Suc.',
		property: 'assetName',
		type: 'number',
		width: 180,
		locked: false
	},
	{
		label: 'Score',
		property: 'score',
		type: 'number',
		width: 180,
		locked: false
	},
];
