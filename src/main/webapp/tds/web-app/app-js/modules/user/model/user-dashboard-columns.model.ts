export class ApplicationColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 100
			}, {
				label: 'Plan Status',
				property: 'planStatus',
				type: 'text',
				width: 100
			}, {
				label: 'Bundle',
				property: 'moveBundle',
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
				property: 'event',
				type: 'text',
				width: 100
			}, {
				label: 'News',
				property: 'news',
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

export class TaskColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			/* For More options button
			{
				label: '',
				property: 'action',
				type: 'text',
				width: 20
			},*/ {
				label: 'Task',
				property: 'task',
				type: 'text',
				width: 295
			}, {
				label: 'Related',
				property: 'related',
				type: 'text',
				width: 295
			}, {
				label: 'Due/Est Finish',
				property: 'dueEstFinish',
				type: 'text',
				width: 140
			}, {
				label: 'Status',
				property: 'status',
				type: 'text',
				width: 80
			}
		];
	}
}