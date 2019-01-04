import {GridColumnModel} from '../../../model/data-list-grid.model';

export class BulkChangeEditColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor(dateFormat = '') {
		this.columns = [
			{
				label: 'Class',
				property: 'domain',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Field Name',
				property: 'field',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Action',
				property: 'action',
				type: 'text',
				width: 100,
				locked: false
			},
			{
				label: 'Value',
				property: 'value',
				type: 'text',
				width: 190,
				locked: false
			}
		]
	}
}