import {Injectable} from '@angular/core';
import * as R from 'ramda';

@Injectable()
export class AssetGlobalFiltersService {

	/**
	 * Add Global Filters to the Query Params based on the URL
	 */
	public prepareFilters(params: any, globalQueryParams: any): void {
		// Verify if we have a global query param
		if (!R.isEmpty(globalQueryParams)) {
			// Iterate the URL Params
			Object.entries(globalQueryParams).forEach(query => {
				let queryFilter = query[0].split('_');
				// Validate the domain exists
				if (params.filters.domains.find((x) => x === queryFilter[0])) {
					let filterColumn = params.filters.columns.find(r => r.domain === queryFilter[0] && r.property === queryFilter[1]);
					// Validate the property exists
					if (filterColumn) {
						filterColumn.filter = query[1];
					}
				} else {
					// if no domain, we add an extra section to handle special filters
					if (!params.filters.extra) {
						params.filters.extra = [];
					}

					if (query[0].indexOf('_ufp') >= 0) {
						params.justPlanning = query[1] === 'true';
					} else {
						params.filters.extra.push({
							property: query[0],
							filter: query[1]
						});
					}
				}
			});
		}
	}

	/**
	 * Get the Just Planning property if exists
	 * @param globalQueryParams
	 * return null | boolean
	 */
	public getJustPlaningFilter(globalQueryParams: any): boolean {
		let justPlanning = null;
		// Verify if we have a global query param
		if (!R.isEmpty(globalQueryParams)) {
			// Iterate the URL Params
			Object.entries(globalQueryParams).forEach(query => {
				if (query[0].indexOf('_ufp') >= 0) {
					justPlanning = query;
				}
			});
		}
		return justPlanning !== null ? justPlanning[1] === 'true' : null;
	}
}
