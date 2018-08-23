import {GridColumnModel} from '../../../shared/model/data-list-grid.model';
import {EnumModel} from '../../../shared/model/enum.model';

export enum BATCH_RECORD_OPERATION {
	INSERT = 'INSERT',
	UPDATE = 'UPDATE',
	UNCHANGED = 'UNCHANGED',
	TBD = 'TBD'
}

export class ImportBatchRecordModel {
	id: number;
	importBatch: any;
	status: EnumModel;
	errorCount: number;
	errorList: Array<string>;
	operation: BATCH_RECORD_OPERATION;
	sourceRowId: number;
	lastUpdated: Date;
	warn: number;
	currentValues: any;
	init?: any;
}

export class ImportBatchRecordDetailColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Status',
				property: 'status.label',
				type: 'text',
				width: 130,
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
				property: 'sourceRowId',
				type: 'number',
				width: 100,
				locked: true
			}
		];
	}
}