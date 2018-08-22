import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response, RequestOptions, Headers} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {PermissionService} from '../../../shared/services/permission.service';
import {DeviceManufacturer} from '../components/device/manufacturer/model/device-manufacturer.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ManufacturerService {
	private manufacturerUrl = '/tdstm/manufacturer';
	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	isValidAlias(alias: string, id: number, parentName: string): Observable<string> {
		const url = `${this.manufacturerUrl}/validateAliasForForm?alias=${alias}&id=${id}&parentName=${parentName}` ;

		return this.http.get(url, '')
			.map((res: any) => res.text())
	}

	getDeviceManufacturer(id: string): Observable<DeviceManufacturer> {
		const url = `${this.manufacturerUrl}/retrieveManufacturerAsJSON?id=${id}`;
		return this.http.post(url, '')
			.map((res: Response) => res.json())
			.map((res: any) => res && Object.assign({aka: res.aliases || '', akaCollection: res.akaCollection}, res.manufacturer) || {})
			.catch((error: any) => error.json());
	}

	updateManufacturer(manufacturer: DeviceManufacturer): Observable<any> {
		let body = `id=${manufacturer.id}`;
		body += `&name=${manufacturer.name}`;
		body += `&description=${manufacturer.description}`;
		body += `&corporateName=${manufacturer.corporateName}`;
		body += `&corporateLocation=${manufacturer.corporateLocation}`;
		body += `&website=${manufacturer.website}`;
		const {edited, deleted, added} = manufacturer.akaChanges;

		if (edited.length) {
			body += edited
				.map(aka => `&aka_${aka.id}=${aka.name}`)
				.join('');
		}

		if (deleted.length) {
			body += '&deletedAka=';
			body += deleted
				.map(aka => aka.id)
				.join(',')
		}

		if (added.length) {
			body += added
				.map(aka => `&aka=${aka.name}`)
				.join('')
		}

		console.log(body);

		const headers = new Headers();
		headers.append('Content-Type', 'application/x-www-form-urlencoded');
		const requestOptions = new RequestOptions({headers: headers});

		const url = `${this.manufacturerUrl}/update` ;
		return this.http.post(url, body, requestOptions)
			.map(res => res.ok)
			.catch((error: any) => error);
	}
}