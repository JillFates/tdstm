import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
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
}