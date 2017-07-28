export class ReportModel {
	id: number;
	name: string;
	favorite: boolean;
	shared: boolean;
	subscribe: boolean;
}

export class ReportGroupModel {
	name: string;
	items: Array<ReportModel>;
	open: boolean;
}