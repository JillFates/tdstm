import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {PermissionService} from '../../../shared/services/permission.service';
import {DeviceManufacturer} from '../components/device/manufacturer/model/device-manufacturer.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ManufacturerService {
	private manufacturerUrl = '/tdstm/manufacturer';
	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	isValidAlias(alias: string, id: number, parentName: string): Observable<boolean> {
		const url = `${this.manufacturerUrl}/validateAliasForForm?alias=${alias}&id=${id}&parentName=${parentName}` ;

		return this.http.get(url, '')
			.map((res: any) => res === 'valid')
	}

	getDeviceManufacturer(id: string): Observable<DeviceManufacturer> {
		const url = `${this.manufacturerUrl}/retrieveManufacturerAsJSON?id=${id}`;
		return this.http.post(url, '')
			.map((res: Response) => res.json())
			.map((res: any) => res && Object.assign({aka: res.aliases || '', akaCollection: res.akaCollection}, res.manufacturer) || {})
			.catch((error: any) => error.json());
	}
}