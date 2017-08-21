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
	type: ViewType;
}

export enum ViewType {
	ALL,
	RECENT,
	FAVORITES,
	MY_VIEWS,
	SHARED_VIEWS,
	SYSTEM_VIEWS
}
export enum ReportFolderIcon {
	FOLDER,
	START
}