export class ProviderColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 70,
				locked: true
			}, {
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 246,
			}, {
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 300,
			}, {
				label: 'Date Create',
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
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
