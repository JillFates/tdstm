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
				filterable: true,
				property: 'name',
				type: 'text',
				width: 246,
				isActionable: true,
			}, {
				label: 'Description',
				filterable: true,
				property: 'description',
				type: 'text',
				width: 300,
			}, {
				label: 'Date Create',
				filterable: true,
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
