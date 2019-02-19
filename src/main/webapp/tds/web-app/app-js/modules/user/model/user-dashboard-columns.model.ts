export class ApplicationColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'assetName',
				type: 'text',
				width: 100
			}, {
				label: 'Plan Status',
				property: 'planStatus',
				type: 'text',
				width: 100
			}, {
				label: 'Bundle',
				property: 'moveBundle.id',
				type: 'text',
				width: 100
			}, {
				label: 'Relation',
				property: 'relation',
				type: 'text',
				width: 100
			}
		];
	}
}

export class ActivePersonColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Project',
				property: 'projectName',
				type: 'text',
				width: 100
			}, {
				label: 'Name',
				property: 'personName',
				type: 'text',
				width: 100
			}, {
				label: 'Latest Activity',
				property: 'lastActivity',
				type: 'text',
				width: 100
			}
		];
	}
}

export class EventNewsColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Date',
				property: 'date',
				type: 'text',
				width: 100
			}, {
				label: 'Event',
				property: 'eventName',
				type: 'text',
				width: 100
			}, {
				label: 'News',
				property: 'eventNews',
				type: 'text',
				width: 100
			}
		];
	}
}

export class EventColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 100
			}, {
				label: 'Start Date',
				property: 'startDate',
				type: 'text',
				width: 100
			}, {
				label: 'Days',
				property: 'days',
				type: 'text',
				width: 100
			}, {
				label: 'Teams',
				property: 'teams',
				type: 'text',
				width: 100
			}
		];
	}
}
