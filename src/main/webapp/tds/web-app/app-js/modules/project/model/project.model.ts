export class ProjectModel {
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
				filterable: true,
				property: 'projectCode',
				type: 'text',
				width: '*'
			},
			{
				label: 'Name',
				filterable: true,
				property: 'projectName',
				type: 'text',
				width: '*'
			},
			{
				label: 'Start Date',
				filterable: true,
				property: 'startDate',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Completion Date',
				filterable: true,
				property: 'completionDate',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Comment',
				filterable: true,
				property: 'comment',
				type: 'text',
				width: '*'
			}
		];
	}
}
