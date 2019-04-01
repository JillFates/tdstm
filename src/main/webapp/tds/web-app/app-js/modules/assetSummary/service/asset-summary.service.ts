import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PermissionService} from '../../../shared/services/permission.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class AssetSummaryService {

	private defaultUrl = '../ws';

	constructor(private http: HttpClient, private permissionService: PermissionService) {}

}