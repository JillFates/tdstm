import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs';
import { DomainModel } from '../model/domain.model';
import {CUSTOM_FIELD_CONTROL_TYPE, FieldSettingsModel} from '../model/field-settings.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class FieldSettingsService {

	private fieldSettingsUrl = '../ws/customDomain/fieldSpec';

	constructor(private http: HttpInterceptor) {
	}

	getFieldSettingsByDomain(domain = 'ASSETS'): Observable<DomainModel[]> {
		return this.http.get(`${this.fieldSettingsUrl}/${domain}`)
			.map((res: Response) => {
				let response = res.json();
				let domains: DomainModel[] = Object.keys(response).map(key => {
					response[key].domain = response[key].domain.toUpperCase();
					return response[key];
				});
				if (domains.length > 0) {
					let sharedFields = domains[0].fields.filter(x => x.shared);
					domains.forEach(d => {
						d.fields.filter(s => s.control === CUSTOM_FIELD_CONTROL_TYPE.YesNo)
							.forEach(s => {
								s.constraints.values = ['Yes', 'No'];
								if (!s.constraints.required) {
									s.constraints.values.splice(0, 0, '');
								}
							});
						sharedFields.forEach(s => {
							let indexOf = d.fields.findIndex(f => f.field === s.field);
							if (indexOf !== -1) {
								d.fields.splice(indexOf, 1, s);
							}
						});
					});
				}
				return domains as any;
			})
			.catch((error: any) => error.json());
	}

	saveFieldSettings(domains: DomainModel[]): Observable<DomainModel[]> {
		let payload = {};
		domains
			.reduce((p: FieldSettingsModel[], c: DomainModel) => p.concat(c.fields), [])
			.forEach((item: any) => {
				item.constraints.required = +item.constraints.required;
				item.udf = +item.udf;
				item.show = +item.show;
				item.shared = +item.shared;
			});
		domains.forEach(domainModel => {
			payload[domainModel.domain.toUpperCase()] = domainModel;
		});
		return this.http.post(`${this.fieldSettingsUrl}/ASSETS`, JSON.stringify(payload))
			.map((res: Response) => res['_body'] ? res.json() : { status: 'Ok' })
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}
}