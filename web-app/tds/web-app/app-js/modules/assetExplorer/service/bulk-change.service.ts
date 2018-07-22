import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Response} from '@angular/http';
import {ViewModel, ViewGroupModel, ViewType} from '../model/view.model';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Permission} from '../../../shared/model/permission.model';
import {ComboBoxSearchModel} from '../../../shared/components/combo-box/model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from '../../../shared/components/combo-box/model/combobox-search-result.model';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class BulkChangeService {
	private bulkChangeUrl = '../ws/bulkChange';

	constructor(private http: HttpInterceptor, private permissionService: PermissionService) {}

	getFields(): Observable<any[]> {
		return this.http.get(`${this.bulkChangeUrl}/fields`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

}