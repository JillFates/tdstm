import {Injectable} from '@angular/core';
import {HttpInterceptor} from '../providers/http-interceptor.provider';
import {Observable} from 'rxjs';
import {Response} from '@angular/http';

@Injectable()
export class UserService {

	private userUrl = '../ws/user';

	constructor(private http: HttpInterceptor) {
	}

	getUserInfo(): Observable<any> {
		return this.http.get(`${this.userUrl}`)
			.map((res: Response) => {
				let result = res.json();
				if (result && result.status === 'success') {
					return result.data;
				} else {
					throw new Error(result.errors.join(';'));
				}
			})
			.catch((error: any) => error.json());
	}
}