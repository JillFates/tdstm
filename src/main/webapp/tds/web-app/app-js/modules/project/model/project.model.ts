import {ModalType} from '../../../shared/model/constants';

export class ProjectModel {
	public name: string;
	public description: string;
	public fromId: number;
	public toId: number;
	public startTime: string;
	public completionTime: string;
	public projectManagerId: number;
	public moveManagerId: number;
	public moveEventId: number;
	public operationalOrder: number;
	public workflowCode: string;
	public useForPlanning: boolean;
}

export class ProjectColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Project Code',
				property: 'projectCode',
				type: 'text',
				width: '*'
			},
			{
				label: 'Name',
				property: 'projectName',
				type: 'text',
				width: '*'
			},
			{
				label: 'Start Date',
				property: 'startDate',
				type: 'text',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Completion Date',
				property: 'completionDate',
				type: 'text',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Comment',
				property: 'description',
				type: 'text',
				width: '*'
			}
		];
	}
}