import {GridColumnModel} from '../../../../../shared/model/data-list-grid.model';

export class BulkChangeEditColumnsModel {

	public columns: Array<GridColumnModel>;

	constructor(dateFormat = '') {
		this.columns = [
			{
				label: 'Class',
				property: 'domain',
				type: 'text',
				width: 110,
				locked: false
			},
			{
				label: 'Field Name',
				property: 'field',
				type: 'text',
				width: 110,
				locked: false
			},
			{
				label: 'Action',
				property: 'action',
				type: 'text',
				width: 110,
				locked: false
			},
			{
				label: 'Value',
				property: 'value',
				type: 'text',
				width: 180,
				locked: false
			}
		]
	}
}