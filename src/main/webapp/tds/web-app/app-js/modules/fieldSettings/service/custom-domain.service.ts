import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/operator/bufferCount';
import {FieldSettingsModel} from '../model/field-settings.model';
import {DomainModel} from '../model/domain.model';
import {DOMAIN} from '../../../shared/model/constants';
import {HttpClient} from '@angular/common/http';
import {SortUtils} from '../../../shared/utils/sort.utils';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class CustomDomainService {
	constructor(private http: HttpClient) {
	}

	getDistinctValues(domain: string, field: FieldSettingsModel): Observable<string[]> {
		return this.http.post(`../wsCustomDomain/distinctValues/${domain}`, JSON.stringify({ fieldSpec: field }))
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	getCommonFieldSpecs(): Observable<DomainModel[]> {
		return this.http.get('../ws/customDomain/fieldSpecsWithCommon')
			.map((response: any) => {
				let domains: DomainModel[] = Object.keys(response).map(key => {
					response[key].domain = response[key].domain.toUpperCase();
					return response[key];
				});
				return domains as any;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get Fields by Domains
	 * 02/27/2018 Only Tasks is implemented
	 * Example: task,other,another
	 * @param domainsFields
	 * @returns {Observable<DomainModel[]>}
	 */
	getFieldSpecsPerDomain(domainsFields: any = 'task'): Observable<DomainModel[]> {
		return this.http.get(`../ws/apiAction/fields?domains=${domainsFields}`)
			.map((response: any) => {
				let resultSet = response && response.status === 'success' && response.data;
				let domains: DomainModel[] = [];
				if (resultSet.domains) {
					resultSet.domains.forEach(d => {
						let domain: DomainModel;
						Object.keys(d).map(key => {
							domain = {
								domain: key.toUpperCase(),
								fields: Object.keys(d[key]).map( (f) => {
									return {
										field: f,
										label: d[key][f],
										// All Fields below are not defined
										tip: null,
										udf: null,
										shared: null,
										imp: null,
										show: null,
										constraints: null
									};
								})
							};
						});
						domain.fields = domain.fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'label'));
						domains.push(domain);
					});
				}
				return domains as any;
			})
			.catch((error: any) => error);
	}

	/**
	 * Return all Fields and their shared fields per domain
	 * @returns {Observable<DomainModel[]>}
	 */
	getCommonFieldSpecsWithShared(): Observable<DomainModel[]> {

		let observableCommonFieldSpecs = Observable.forkJoin([
			/* [0] */ this.getCommonFieldSpecs(),
			/* [1] */ this.getFieldSpecsPerDomain(DOMAIN.TASK)
		]);

		return Observable.from(observableCommonFieldSpecs).map((res: any) => {
			let domainWithShared = [];
			// Process All Fields
			if (res[0]) {
				domainWithShared = res[0];
				let commonModel = domainWithShared.find((r) => r.domain === DOMAIN.COMMON);
				if (commonModel) {
					domainWithShared.forEach((r) => {
						if (r.domain !== DOMAIN.COMMON) {
							r.fields = r.fields.concat([], commonModel.fields);
						}
						r.fields = r.fields.sort((a, b) => SortUtils.compareByProperty(a, b, 'label'));
					});
				}
			}
			// Process new Task Field Definition
			if (res[1]) {
				domainWithShared = domainWithShared.concat([], res[1]);
			}

			return domainWithShared as any;
		});
	}

	getInvalidValues(domain: string, field: FieldSettingsModel): Observable<string[]> {
		return Observable.from(['value1', 'value2', 'value3', 'value4']).bufferCount(4);
	}

	checkConstraints(domain: string, field: FieldSettingsModel): Observable<boolean> {
		return Observable.from([true]);
	}

}