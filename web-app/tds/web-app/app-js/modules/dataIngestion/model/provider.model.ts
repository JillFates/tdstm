export class ProviderColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
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
				width: 80
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
