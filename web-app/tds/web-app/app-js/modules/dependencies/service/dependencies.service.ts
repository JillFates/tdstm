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

		const pagination = `rows=${take}&page=${page}`;
		const sorting = `&sidx=${sort.field}&sord=${sort.dir}`;
		const filtering  = filters
				.map(filter => `&${filter.field}=${filter.value}`).join('') +
				`&_search=${filters.length > 0 ? 'true' : 'false'}`;

		const url = `${endpoint}?${pagination}${sorting}${filtering}`;
		return this.http.get(`${url}`)
			.pipe(
				map((response: any) => {
					const results = response.json();
					let {dependencies = [], total = 0} = (results && results.data) || {};
					// todo remove later
					const data = this.addFakeTags(dependencies);

					return {data, total};
				})
			)
	}

	/* Todo remove when the endpoint changes are ready */
	addFakeTags(dependencies: Dependency[]): Dependency[] {
		const tagsAsset = [
			{
				'color': 'Yellow',
				'css': 'tag-yellow',
				'description': 'Sarbanes–Oxley Act Compliance',
				'id': 659,
				'name': 'SOX',
				'tagId': 8
			},
			{
				'color': 'Blue',
				'css': 'tag-blue',
				'description': 'Custom QAE2E acqVm Tag description',
				'id': 276,
				'name': 'QAE2E acqVm Tag',
				'tagId': 184
			}
		];
		const tagsDependency = [
			{
				'color': 'Yellow',
				'css': 'tag-yellow',
				'description': 'Sarbanes–Oxley Act Compliance',
				'id': 659,
				'name': 'SOX',
				'tagId': 8
			},
			{
				'color': 'Blue',
				'css': 'tag-blue',
				'description': 'Custom QAE2E acqVm Tag description',
				'id': 276,
				'name': 'QAE2E acqVm Tag',
				'tagId': 184
			}
		];

		dependencies = dependencies
			.map((dependency: Dependency) => ({...dependency, tagsAsset: tagsAsset, tagsDependency: tagsDependency}));

		return dependencies;
	}
}