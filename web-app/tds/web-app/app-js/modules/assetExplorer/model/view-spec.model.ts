export class QueryColumn {
	domain: string;
	property: string;
}

export class ViewColumn extends QueryColumn {
	width?= 200;
	locked?= false;
	edit?= false;
	filter?= '';
	label: string;
}

export class FilterColumn extends QueryColumn {
	filter: string;
}

export class ViewSort extends QueryColumn {
	order: 'a' | 'd';
}

export class ViewSpec {
	domains: Array<String> = [];
	columns: Array<QueryColumn | ViewColumn> = [];
	filters: Array<FilterColumn> = [];
	sort: ViewSort;
}

export class QuerySpec extends ViewSpec {
	columns: Array<QueryColumn> = [];
	paging: {
		offset: number,
		limit: number
	};
}