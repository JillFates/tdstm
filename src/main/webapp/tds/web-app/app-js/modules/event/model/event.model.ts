import {FilterType} from 'tds-component-library';

export class EventColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 'auto'
			}, {
				label: 'Estimated Start',
				property: 'estStartTime',
				filterType: FilterType.date,
				format: dateFormat,
				width: 'auto',
			}, {
				label: 'Estimated Completion',
				property: 'estCompletionTime',
				filterType: FilterType.date,
				format: dateFormat,
				width: 'auto',
			}, {
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 'auto',
			}, {
				label: 'Runbook Status',
				property: 'runbookStatus',
				filterType: FilterType.text,
				width: 'auto'
			}, {
				label: 'Bundles',
				property: 'moveBundlesString',
				filterType: FilterType.text,
				width: 'auto'
			}
		];
	}
}

export class EventModel {
	id: number;
	name: string;
	description?: string;
	tagIds?: number[];
	moveBundle?: number[];
	runbookStatus?: string;
	runbookBridge1?: string;
	runbookBridge2?: string;
	videolink?: string;
	estStartTime?: Date;
	estCompletionTime?: Date;
	apiActionBypass: boolean;
};

export enum EventRowType {
	Header,
	Percents,
	Tasks,
	PlannedStart,
	PlannedCompletion,
	ActualStart,
	ActualCompletion
}

export enum CatagoryRowType {
	Header,
	Percent,
	TaskCompleted,
	PlannedStart,
	PlannedCompletion,
	ActualStart,
	ActualCompletion
}

export interface CategoryTask {
	percComp: string;
	totalTasks: string;
	completedTasks: string;
	category: string;
	minActStart: string;
	maxActFinish: string;
	minEstStart: string;
	maxEstFinish: string;
}

export class EventPlanStatus {
	dialIndicator: number;
	cssClass: string;
	dayTime: string;
	clockMode: string;
	description: string;
	eventTitle: string;
	startDate: string;
	complDate: string;
	status: string;

	constructor() {
		this.dialIndicator = 0;
		this.cssClass = '';
		this.dayTime = '';
		this.clockMode = 'none';
		this.description = '';
		this.eventTitle = '';
		this.status = '';
	}
}

export class TaskSummaryStep {
	columnNumber: number;
	status: {
		value: string;
		classes: string;
	};
	tasks: string;
	plannedStart: string;
	plannedCompletion: string;
	actualStart: string;
	actualCompletion: string;
}

export interface TaskCategoryCell {
	text: string;
	classes: string;
	compose?: any;
}
