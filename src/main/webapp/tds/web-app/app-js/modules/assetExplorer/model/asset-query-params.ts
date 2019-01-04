export class AssetQueryParams {
	offset: number;
	limit: number;
	sortDomain: string;
	sortProperty: string;
	sortOrder: string;
	filters: {
		domains: Array<any>;
		columns: Array<any>;
	};
	forExport? = false;
}