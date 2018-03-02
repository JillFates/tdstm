import {GridColumnModel} from '../../../shared/model/data-list-grid.model';
import {EnumModel} from '../../../shared/model/enum.model';

export class ImportBatchRecordModel {
	id: number;
	importBatch: any;
	status: EnumModel;
	errorCount: number;
	operation: string;
	sourceRowId: number;
	ignored: number;
	lastUpdated: Date;
	warn: number;
	currentValues: any;
	// name: string;
	// type: string;
	// depType: string;
	// nameD: string;
	// typeD: string;
}

export class ImportBatchRecordDetailColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Status',
				property: 'status.label',
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
				property: 'sourceRowId',
				type: 'number',
				width: 100,
				locked: true
			},
			// {
			// 	label: 'Name (P)',
			// 	property: 'name',
			// 	type: 'text',
			// 	width: 130,
			// },
			// {
			// 	label: 'Type (P)',
			// 	property: 'type',
			// 	type: 'text',
			// 	width: 130
			// },
			// {
			// 	label: 'Dep. Type',
			// 	property: 'depType',
			// 	type: 'text',
			// 	width: 130
			// },
			// {
			// 	label: 'Name (D)',
			// 	property: 'nameD',
			// 	type: 'text',
			// 	width: 130,
			// },
			// {
			// 	label: 'Type (D)',
			// 	property: 'typeD',
			// 	type: 'text',
			// 	width: 130,
			// },
		];
	}
}