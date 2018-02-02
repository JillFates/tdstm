export class ProviderColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 108,
				locked: true
			}, {
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 208,
			}, {
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 300,
			}, {
				label: 'Date Create',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 170
			}
		];
	}
}

export class ProviderModel {
	id?: number;
	name: string;
	description?: string;
	comment?: string;
	dateCreated?: Date;
}
