import {GridColumnModel} from '../../../../../shared/model/data-list-grid.model';

export class BulkChangeEditColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor(dateFormat = '') {
		this.columns = [
			{
				label: 'Class',
				property: 'className',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Field Name',
				property: 'field',
				type: 'text',
				width: 200,
				locked: false
			},
			{
				label: 'Action',
				property: 'action',
				type: 'text',
				width: 80,
				locked: false
			},
			{
				label: 'Value',
				property: 'value',
				type: 'text',
				width: 130,
				locked: false
			}
		]
	}
}