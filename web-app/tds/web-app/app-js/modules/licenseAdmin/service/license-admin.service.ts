import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class LicenseAdminService {

	private readonly licenseUrl = '../ws/license';

	constructor(private http: HttpInterceptor) {
	}

	getLicenses(): Observable<any[]> {
		return this.http.get(`${this.licenseUrl}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}
}