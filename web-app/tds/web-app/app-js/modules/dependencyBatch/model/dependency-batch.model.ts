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
				locked: true
			},
		];
	}
}