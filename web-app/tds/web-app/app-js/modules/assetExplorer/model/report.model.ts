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
		this.isOwner = true;
	}
}

export class ReportGroupModel {
	name: string;
	items: Array<ReportModel>;
	open: boolean;
	type: ReportType;
}

export enum ReportType {
	ALL,
	RECENT,
	FAVORITES,
	MY_REPORTS,
	SHARED_REPORTS,
	SYSTEM_REPORTS
}
export enum ReportFolderIcon {
	FOLDER,
	START
}