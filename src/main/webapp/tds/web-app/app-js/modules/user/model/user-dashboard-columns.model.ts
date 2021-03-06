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
				width: 50
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
				cssClass: 'task',
				property: 'task',
				type: 'text',
				width: '*'
			}, {
				label: 'Related',
				cssClass: 'related',
				property: 'related',
				type: 'text',
				width: '*'
			}, {
				label: 'Due/Est Finish',
				cssClass: 'est-finish',
				property: 'dueEstFinish',
				width: '*',
				type: 'text',
			}, {
				label: 'Status',
				cssClass: 'status-column',
				property: 'status',
				type: 'text',
				width: 100
			}
		];
	}
}
