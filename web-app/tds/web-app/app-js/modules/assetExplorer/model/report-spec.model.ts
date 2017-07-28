export class ReportColumn {
	domain: string;
	property: string;
	width?= 50;
	locked?= false;
	edit?= false;
	filter?= '';

}

export class ReportSort {
	domain: string;
	property: string;
	order: 'a' | 'd';
}

export class ReportSpec {
	domains: Array<String> = [];
	columns: Array<ReportColumn> = [];
	sort: Array<ReportSort> = [];
}