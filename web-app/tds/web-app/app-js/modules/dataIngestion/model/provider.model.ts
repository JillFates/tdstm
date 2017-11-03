export class ProviderColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 80
			}, {
				label: 'Name',
				property: 'name',
				type: 'text'
			}, {
				label: 'Description',
				property: 'description',
				type: 'text'
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
	description: string;
	comment: string;
	dateCreated?: Date;
}
