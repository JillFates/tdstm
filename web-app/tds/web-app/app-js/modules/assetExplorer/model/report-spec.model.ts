export class QueryColumn {
	domain: string;
	property: string;
}

export class ReportColumn extends QueryColumn {
	width?= 50;
	locked?= false;
	edit?= false;
	filter?= '';
	label: string;
}

export class FilterColumn extends QueryColumn {
	filter: string;
}

export class ReportSort extends QueryColumn {
	order: 'a' | 'd';
}

export class ReportSpec {
	domains: Array<String> = [];
	columns: Array<QueryColumn | ReportColumn> = [];
	filters: Array<FilterColumn> = [];
	sort: ReportSort;
}

export class QuerySpec extends ReportSpec {
	columns: Array<QueryColumn> = [];
	paging: {
		offset: number,
		limit: number
	};
}