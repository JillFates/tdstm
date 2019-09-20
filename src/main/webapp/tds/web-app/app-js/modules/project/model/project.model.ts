import {ModalType} from '../../../shared/model/constants';
import {KendoFileUploadBasicConfig} from '../../../shared/providers/kendo-file-upload.interceptor';

export class ProjectModel {
	public clientId: number;
	public projectName: string;
	public description: string;
	public startDate: Date;
	public completionDate: Date;
	public partnerIds: number[];
	public projectLogo: File;
	public projectManagerId: number;
	public projectCode: string;
	public projectType: string;
	public comment: string;
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