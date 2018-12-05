import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';

import {PermissionService} from '../../../shared/services/permission.service';
import {Dependency, DependencyResults} from '../model/dependencies.model';
import {AssetTagSelectorComponent} from '../../../shared/components/asset-tag-selector/asset-tag-selector.component';

import {map} from 'rxjs/operators';

export interface DependenciesRequestParams {
	page: number;
	take: number;
	sort: any;
	filters: any[];
}

@Injectable()
export class DependenciesService {
	private readonly BASE_URL = '/tdstm/ws/asset';
	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	/**
	 * Get the dependencies matched by the reques parameters
	 * @param {DependenciesRequestParams} params paremeters to filter and sorting
	 * @return {Observable<DependencyResults>)
	 */
	getDependencies(params: DependenciesRequestParams): Observable<DependencyResults> {
		const endpoint = `${this.BASE_URL}/listDependencies`;
		const {page, take, sort, filters} = params;
		const payload = {
			rows: take,
			page,
			sidx: sort.field,
			sord: sort.dir
		};

		// add the filters to the payload
		filters.reduce((acc, current) => {
			acc[current.field] = current.value;
			return acc;
		}, payload);

		console.log('Payload');
		console.log(payload);

		return this.http.post(endpoint, JSON.stringify(payload))
			.pipe(
				map((response: any) => {
					const results = response.json();
					let {dependencies = [], total = 0} = (results && results.data) || {};
					return {data: dependencies, total};
				})
			)
	}
}