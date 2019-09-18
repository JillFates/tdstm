export class ProjectModel {
	public clientId: number;
	public projectName: string;
	public description: string;
	public startDate: Date;
	public completionDate: Date;
	public partnerIds: number[];
	public projectLogo: any;
	public projectManagerId: number;
	public workflowCode: string;
	public projectCode: string;
	public projectType: string;
	public comment: string;
	public defaultBundle: any;
	public defaultBundleName: string;
	public timeZone: string;
	public collectMetrics: boolean;
	public planMethodology: string;
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
				property: 'comment',
				type: 'text',
				width: '*'
			}
		];
	}
}