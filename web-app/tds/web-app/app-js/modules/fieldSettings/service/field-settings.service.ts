import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { DomainModel } from '../model/domain.model';
import { FieldSettingsModel } from '../model/field-settings.model';
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
				return Object.keys(response).map(key => {
					response[key].domain = response[key].domain.toUpperCase();
					return response[key];
				});
			})
			.catch((error: any) => error.json());
	}

	saveFieldSettings(domains: DomainModel[]): Observable<DomainModel[]> {
		let payload = {};
		domains.forEach(domainModel => {
			payload[domainModel.domain.toUpperCase()] = domainModel;
		});
		return this.http.post(`${this.fieldSettingsUrl}/ASSETS`, JSON.stringify(payload))
			.map((res: Response) => res['_body'] ? res.json() : { status: 'Ok' })
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}
}