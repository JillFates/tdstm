import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';

@Injectable()
export class ImportAssetsService {

	private importEndpointURL = '../ws/assetImport/';

	constructor(private http: HttpClient) {}

	/**
	 * Returns a collection of data lists including the actions and datascripts used to populate the form
	 * @returns {Observable<Array<any>>}
	 */
	public getManualOptions(): Observable<any> {
		let url = this.importEndpointURL + 'manualFormOptions';
		return this.http.get(url)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Used to invoke the fetch job that will call remote system to pull data back to the server.
	 * @param option
	 * @returns {Observable<any>} This will return status and the filename.
	 */
	public postFetch(action: any): Observable<any> {
		let url = this.importEndpointURL + 'invokeFetchAction/' + action.id;
		return this.http.post(url, null)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Retrieves the raw data from the Fetch or Transform. This requires the name of the file that was generated.
	 * The content-type: will be returned with the request. Initially it will be for CSV or JSON (future releases will return XML and Excel)
	 * @param {string} filename
	 * @returns {Observable<any>}
	 */
	public getFileContent(filename: string): Observable<any> {
		let url = this.importEndpointURL + 'viewData?filename=' + filename;
		return this.http.get(url, {responseType: 'text'})
			.map((response: any) => {
				let fileExtension = filename.split('.').pop();
				if (fileExtension === 'json') {
					return response;
				} else {
					return response;
				}
			}).catch((error: any) => error);
	}

	/**
	 * Invoke an ETL process on the filename (from invokeFetch) passed in using the DataScript that was specified.
	 * @param option
	 * @returns {Observable<any>} It will return the status including counts, errors, and output filename.
	 */
	public postTransform(datascript: any, filename: string): Observable<ApiResponseModel> {
		let url = this.importEndpointURL + 'initiateTransformData?dataScriptId=' + datascript.id + '&filename=' + filename;
		return this.http.post(url, null)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * The call should pass in the filename returned from the transformData step.
	 * @returns {Observable<any>} It should return the results of the import.
	 */
	public postImport(filename: string): Observable<any> {
		let url = this.importEndpointURL + 'loadData?filename=' + filename;
		return this.http.post(url, null)
			.map((response: any) => response)
			.catch((error: any) => error);
	}
}
