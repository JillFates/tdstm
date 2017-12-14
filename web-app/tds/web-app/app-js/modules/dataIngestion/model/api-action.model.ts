export class APIActionColumnModel {
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
				locked: true
			}, {
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 200
			}, {
				label: 'Description',
				property: 'description',
				type: 'text'
			}, {
				label: 'Method',
				property: 'agentMethod',
				type: 'text',
				width: 200
			}, {
				label: 'Data',
				property: 'producesData',
				type: 'boolean',
				width: 100
			}, {
				label: 'Default Data Script',
				property: 'defaultDataScriptName',
				type: 'text'
			}, {
				label: 'Created',
				property: 'dateCreated',
				type: 'date',
				format: '{0:d}',
				width: 170
			}, {
				label: 'Last Modified',
				property: 'lastModified',
				type: 'date',
				format: '{0:d}',
				width: 170
			}
		];
	}
}

export class APIActionModel {
	id?: number;
	name: string;
	description: string;
	agentMethod?: string;
	provider?: {
		id?: number,
		name: string
	};
	defaultDataScriptName?: string;
	producesData?: number;
	dateCreated?: Date;
	lastModified?: Date;
}