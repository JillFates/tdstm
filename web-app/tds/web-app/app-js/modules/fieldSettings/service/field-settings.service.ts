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
				let result = [];
				Object.keys(response).forEach((key) => result.push({
					domain: key.toUpperCase(),
					fields: response[key]
				}));
				return result;
			})
			.catch((error: any) => error.json());
	}

	saveFieldSettings(domain: string, fields: FieldSettingsModel[]): Observable<DomainModel[]> {
		let payload = {};
		payload[domain] = fields;
		return this.http.post(`${this.fieldSettingsUrl}/${domain}`, JSON.stringify(payload))
			.map((res: Response) => res.json())
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}
}