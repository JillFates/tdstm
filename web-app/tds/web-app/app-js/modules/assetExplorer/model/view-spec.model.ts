export class QueryColumn {
	domain: string;
	property: string;
}

export const VIEW_COLUMN_MIN_WIDTH = 140;

export class ViewColumn extends QueryColumn {
	width ? = VIEW_COLUMN_MIN_WIDTH;
	locked ? = false;
	edit ? = false;
	filter = '';
	label: string;
	notFound?: boolean;
}

export class ViewSort extends QueryColumn {
	order: 'a' | 'd';
}

export class ViewSpec {
	domains: Array<String> = [];
	columns: Array<QueryColumn | ViewColumn> = [];
	sort: ViewSort;
}

export class QuerySpec extends ViewSpec {
	columns: Array<QueryColumn> = [];
	paging: {
		offset: number,
		limit: number
	};
}