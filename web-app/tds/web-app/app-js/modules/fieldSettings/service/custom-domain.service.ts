import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { FieldSettingsModel } from '../model/field-settings.model';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class CustomDomainService {
	constructor(private http: HttpInterceptor) {
	}

	getDistinctValues(domain: string, field: FieldSettingsModel): Observable<string[]> {
		return this.http.post(`../wsCustomDomain/distinctValues/${domain}`, JSON.stringify({ fieldSpec: field }))
			.map(res => res.json())
			.catch((error: any) => error.json());
	}

	getInvalidValues(domain: string, field: FieldSettingsModel): Observable<string[]> {
		// return this.http.post(`../wsCustomDomain/invalidValues/${domain}`, JSON.stringify(field))
		//     .map(res => res.json())
		//     .catch((error: any) => error.json());
		return Observable.from(['value1', 'value2', 'value3', 'value4']).bufferCount(4);
	}

	checkConstraints(domain: string, field: FieldSettingsModel): Observable<boolean> {
		// return this.http.post(`../wsCustomDomain/checkConstraints/${domain}`, JSON.stringify(field))
		//     .map(res => res.json())
		//     .catch((error: any) => error.json());
		return Observable.from([true]);
	}

}