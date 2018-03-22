import { Injectable } from '@angular/core';
import { Response, RequestOptions, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class PreferenceService {

	private preferenceUrl = '../ws/user/preferences';
	private preferenceUrlPost = '../ws/user/preference';

	preferences: any = {};

	constructor(private http: HttpInterceptor) {
	}

	getPreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.map((res: Response) => {
				let response = res.json();
				Object.keys(response.data.preferences).forEach((key) => {
					this.preferences[key] = response.data.preferences[key];
				});
			})
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	setPreference(preferenceCode: string, value: string): Observable<any>  {
		const headers = new Headers();
		headers.append('Content-Type', 'application/x-www-form-urlencoded');
		const requestOptions = new RequestOptions({headers: headers});

		const body = `code=${preferenceCode}&value=${value}`;
		return this.http.post(this.preferenceUrlPost, body, requestOptions);
	}

}