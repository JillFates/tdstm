import { ReportSpec } from './report-spec.model';

export class ReportModel {
	id: number;
	name: string;
	isOwner: boolean;
	isSystem: boolean;
	isShared: boolean;
	schema?: ReportSpec;
	constructor() {
		this.schema = new ReportSpec();
	}
}

export class ReportGroupModel {
	name: string;
	items: Array<ReportModel>;
	open: boolean;
	icon: ReportFolderIcon;
}

export enum ReportFolderIcon {
	folder,
	star
}