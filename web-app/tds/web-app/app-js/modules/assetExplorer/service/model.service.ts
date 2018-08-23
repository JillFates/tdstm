import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {PermissionService} from '../../../shared/services/permission.service';
import {DeviceModel} from '../components/device/model-device/model/device-model.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ModelService {
	private modelUrl = '/tdstm/model';
	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getModelAsJSON(id: string): Observable<DeviceModel> {
		const url = `${this.modelUrl}/retrieveModelAsJSON?id=${id}`;

		return this.http.post(url, '')
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}
}