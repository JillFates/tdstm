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
				label: 'Id',
				property: 'id',
				type: 'text',
				width: 100,
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
				format: '{0:d}',
				width: 170,
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
				width: 100,
				locked: false
			},
			{
				label: 'Provider',
				property: 'provider',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Datascript',
				property: 'datascript',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'File Name',
				property: 'filename',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Records',
				property: 'records',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Errors',
				property: 'errors',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Pending',
				property: 'pending',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Processed',
				property: 'processed',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Processed',
				property: 'ignored',
				type: 'text',
				width: 100,
				locked: false
			},
		];
	}
}