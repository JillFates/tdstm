import {GridColumnModel} from '../../../shared/model/data-list-grid.model';

export class ImportBatchRecordModel {
	id: number;
	status: string;
	errorCount: number;
	operation: 'Update'|'Add'|'Undetermined';
	sourceRow: number;
	name: string;
	type: string;
	depType: string;
	nameD: string;
	typeD: string;
}

export class ImportBatchRecordDetailColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 100,
				locked: true,
			},
			{
				label: 'Error Count',
				property: 'errorCount',
				type: 'number',
				width: 100,
				locked: true
			},
			{
				label: 'Import Operation',
				property: 'operation',
				type: 'text',
				width: 130,
				locked: true
			},
			{
				label: 'Source Row',
				property: 'sourceRow',
				type: 'number',
				width: 100,
				locked: true
			},
			{
				label: 'Name (P)',
				property: 'name',
				type: 'text',
				width: 130,
				locked: true
			},
			{
				label: 'Type (P)',
				property: 'type',
				type: 'text',
				width: 130
			},
			{
				label: 'Dep. Type',
				property: 'depType',
				type: 'text',
				width: 130
			},
			{
				label: 'Name (D)',
				property: 'nameD',
				type: 'text',
				width: 130,
			},
			{
				label: 'Type (D)',
				property: 'typeD',
				type: 'text',
				width: 130,
			},
		];
	}
}