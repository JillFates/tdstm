import {Injectable} from '@angular/core';
import { Response } from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class ImportAssetsService {

	private importEndpointURL = '../ws/assetImport/';

	constructor(private http: HttpInterceptor) {}

	/**
	 * Returns a collection of data lists including the actions and data scripts used to populate the form
	 * @returns {Observable<Array<any>>}
	 */
	public getManualOptions(): Observable<any> {
		let url = this.importEndpointURL + 'manualFormOptions';
		return this.http.get(url)
			.map((res: Response) => {
				return res.json();
			}).catch((error: any) => error.json());

		/*
		// TODO: call real endpoint
		let results = [
			{id: 1, name: 'Service Now - Linux Servers'},
			{id: 2, name: 'Service Now - Linux Servers v2'},
			{id: 3, name: 'Service Now - Linux Servers v3'}
		];
		return Observable.of(results);
		*/
	}

	/**
	 * Used to invoke the fetch job that will call remote system to pull data back to the server.
	 * @param option
	 * @returns {Observable<any>} This will return status and the filename.
	 */
	public postFetch(action: any): Observable<any> {
		let url = this.importEndpointURL + 'invokeFetchAction?actionId=' + action.id;
		return this.http.post(url, null)
			.map((res: Response) => {
				return res.json();
			}).catch((error: any) => error.json());
		/*
		let result = {
			status: 'Success',
			filename: 'fetchResults.json',
			extension: 'JSON'
		};
		return Observable.of(result);*/
	}

	/**
	 * Retrieves the raw data from the Fetch or Transform. This requires the name of the file that was generated.
	 * The content-type: will be returned with the request. Initially it will be for CSV or JSON (future releases will return XML and Excel)
	 * @param {string} filename
	 * @returns {Observable<any>}
	 */
	public getFileContent(filename: string): Observable<any> {
		let url = this.importEndpointURL + 'viewData?filename=' + filename;
		return this.http.get(url)
			.map((res: Response) => {
				let fileExtension = filename.split('.').pop();
				if (fileExtension === 'json') {
					return res.json();
				} else {
					return res['_body'];
				}
			}).catch((error: any) => error.json());
		/*let result = {
			a: 'foo',
			b: 'bar',
			c: 'foobar'
		};
		return Observable.of(result);*/
	}

	/**
	 * Invoke an ETL process on the filename (from invokeFetch) passed in using the Data Script that was specified.
	 * @param option
	 * @returns {Observable<any>} It will return the status including counts, errors, and output filename.
	 */
	public postTransform(datascript: any, filename: string): Observable<any> {
		let url = this.importEndpointURL + 'transformData?dataScriptId=' + datascript.id + '&filename=' + filename;
		return this.http.post(url, null)
			.map((res: Response) => {
				return res.json();
			}).catch((error: any) => error.json());
		/*let result = {
			status: 'Success',
			counts: 100,
			errors: ['error1', 'error2'],
			outputFilename: 'transformResults.json',
			outputFilenameExtension: 'JSON'
		};
		return Observable.of(result);*/
	}

	/**
	 * The call should pass in the filename returned from the transformData step.
	 * @returns {Observable<any>} It should return the results of the import.
	 */
	public postImport(filename: string): Observable<any> {
		let url = this.importEndpointURL + 'loadData?filename=' + filename;
		return this.http.post(url, null)
			.map((res: Response) => {
				return res.json();
			}).catch((error: any) => error.json());
		/*let result = {
			status: 'Success',
			counts: 100,
			errors: ['error1', 'error2']
		};
		return Observable.of(result);*/
	}
}