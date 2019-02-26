import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {HttpResponse} from '@angular/common/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

export const PROGRESSBAR_COMPLETED_STATUS = 'COMPLETED';
export const PROGRESSBAR_FAIL_STATUS = 'Failed';

export const FILE_SYSTEM_URL = '../ws/fileSystem';
export const ETL_SCRIPT_UPLOAD_URL = '../ws/fileSystem/uploadFileETLDesigner';
export const ASSET_IMPORT_UPLOAD_URL = '../ws/fileSystem/uploadFileETLAssetImport';

@Injectable()
export class KendoFileHandlerService {

	constructor(private http: HttpClient) {
	}

	uploadETLScriptFile(formdata: any): Observable<any | HttpResponse<any>> {
		return this.http.post(ETL_SCRIPT_UPLOAD_URL, formdata)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	uploadAssetImportFile(formdata: any): Observable<any | HttpResponse<any>> {
		return this.http.post(ASSET_IMPORT_UPLOAD_URL, formdata)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: {data: response.data}});
			})
			.catch((error: any) => error);
	}

	uploadFile(formdata: any): Observable<any | HttpResponse<any>> {
		return this.http.post(`${FILE_SYSTEM_URL}/uploadFile`, formdata)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: {data: response.data}});
			})
			.catch((error: any) => error);
	}

	deleteFile(filename: string): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'}), body: JSON.stringify({filename: filename} )
		};

		return this.http.delete(`${FILE_SYSTEM_URL}/delete`, httpOptions)
			.map((response: any) => {
				response.operation = 'delete';
				return new HttpResponse({status: 200, body: { data : response } });
			})
			.catch((error: any) => error);
	}
}