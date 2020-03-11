import {FilterType} from 'tds-component-library';

export class ProjectModel {
	public id: number;
	public clientId: number;
	public projectName: string;
	public description: string;
	public startDate: Date;
	public completionDate: Date;
	public partners: any[];
	public projectLogo: any;
	public projectManagerId: number;
	public projectCode: string;
	public projectType: string;
	public comment: string;
	public defaultBundle: any;
	public defaultBundleName: string;
	public timeZone: string;
	public collectMetrics: number;
	public planMethodology: any;
}

export class ProjectColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Project Code',
				property: 'projectCode',
				filterType: FilterType.text
			},
			{
				label: 'Name',
				property: 'projectName',
				filterType: FilterType.text
			},
			{
				label: 'Start Date',
				filterable: true,
				property: 'startDate',
				format: dateFormat,
				filterType: FilterType.date
			},
			{
				label: 'Completion Date',
				filterable: true,
				property: 'completionDate',
				format: dateFormat,
				filterType: FilterType.date
			},
			{
				label: 'Comment',
				filterable: true,
				property: 'comment',
				filterType: FilterType.text
			}
		];
	}
}
