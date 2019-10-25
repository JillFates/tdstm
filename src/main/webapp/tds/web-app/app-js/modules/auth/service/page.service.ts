// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Other
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable()
export class PageService {

	private internalPath = '../ws/';

	constructor(
		private http: HttpClient) {
	}

	/**
	 * Save the latest page the user has visited
	 * @param path
	 */
	public updateLastPage(path = ''): Observable<any> {

		let pathUrl = `${this.internalPath}user/updateLastPage`;
		if (path && path !== '') {
			pathUrl += '?path=' + path;
		}

		return this.http.get(pathUrl).pipe(
			map((result: any) => {
				return result.successful;
			})
		);
	}

}
