import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {HttpResponse} from '@angular/common/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

export const PROGRESSBAR_COMPLETED_STATUS = 'COMPLETED';
export const PROGRESSBAR_FAIL_STATUS = 'Failed';

@Injectable()
export class KendoFileHandlerService {

	private fileSystemUrl = '../ws/fileSystem';
	private ETLScriptUploadURL = '../ws/fileSystem/uploadFileETLDesigner';
	private assetImportUploadURL = '../ws/fileSystem/uploadFileETLAssetImport';

	constructor(private http: HttpClient) {
	}

	uploadETLScriptFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(this.ETLScriptUploadURL, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	uploadAssetImportFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(this.assetImportUploadURL, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	uploadFile(formdata: any): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		return this.http.post(`${this.fileSystemUrl}/uploadFile`, formdata, httpOptions)
			.map((response: any) => {
				return new HttpResponse({status: 200, body: { data : response.data } });
			})
			.catch((error: any) => error);
	}

	deleteFile(filename: string): Observable<any | HttpResponse<any>> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'}), body: JSON.stringify({filename: filename} )
		};

		return this.http.delete(`${this.fileSystemUrl}/delete`, httpOptions)
			.map((response: any) => {
				response.operation = 'delete';
				return new HttpResponse({status: 200, body: { data : response } });
			})
			.catch((error: any) => error);
	}
}