import { ReportModel } from './report.model';

export class ReportGroupModel {
	name: string;
	items: Array<ReportModel>;
	open: boolean;
}