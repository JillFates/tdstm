import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {DeviceModel} from '../components/device/model-device/model/device-model.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ModelService {
	private modelUrl = '/tdstm/model';

	constructor(private http: HttpClient) {
	}

	getModelAsJSON(id: string): Observable<DeviceModel> {
		const url = `${this.modelUrl}/retrieveModelAsJSON?id=${id}`;

		return this.http.post(url, '')
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	editModel(id: string): Observable<String> {
		// 'redirect'To': 'modelDialog'
		const headers = new HttpHeaders().set('Content-Type', 'text/plain; charset=utf-8');

		const url = `${this.modelUrl}/edit?id=${id}`;
		return this.http.post(url, JSON.stringify({
			id: id,
			redirectTo: 'modelDialog'
		}), {headers, responseType: 'text'})
		.map((response: any) => response)
	}
}