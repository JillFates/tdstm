import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

export class DependencyBatchModel {
	id: number;
	status: string;
	importedDate: Date;
	importedBy: string;
	domain: string;
	provider: string;
	datascript: string;
	filename: string;
	records: number;
	errors: number;
	pending: number;
	processed: number;
	ignored: number;
}

export class DependencyBatchColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 70,
				locked: true,
				cellStyle: {'text-align': 'center'}
			},
			{
				label: 'Id',
				property: 'id',
				type: 'number',
				width: 60,
				locked: false
			},
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Imported At',
				property: 'importedDate',
				type: 'date',
				format: '{0:yyyy/MM/dd HH:mm:ss}',
				width: 200,
				locked: false
			},
			{
				label: 'Imported By',
				property: 'importedBy',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Domain',
				property: 'domain',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'Provider',
				property: 'provider',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'Datascript',
				property: 'datascript',
				type: 'text',
				width: 130,
				locked: false
			},
			{
				label: 'File Name',
				property: 'filename',
				type: 'text',
				width: 150,
				locked: false
			},
			{
				label: 'Records',
				property: 'records',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Errors',
				property: 'errors',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Pending',
				property: 'pending',
				type: 'number',
				width: 80,
				locked: false
			},
			{
				label: 'Processed',
				property: 'processed',
				type: 'number',
				width: 100,
				locked: false
			},
			{
				label: 'Ignored',
				property: 'ignored',
				type: 'number',
				width: 80,
				locked: false
			},
		];
	}
}