// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

// Others
import {Observable} from 'rxjs';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name ExportAssetService
 */
@Injectable()
export class ExportAssetService {

	// private instance variable to hold base url
	private export_asset_url = '../assets/export';

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get the Export Assets Data
	 * Bundles Options
	 * Selectable item to export based on user preferences
	 * @returns {Observable<R>}
	 */
	public getExportAssetsData(): Observable<any[]> {
		return this.http.get(this.export_asset_url + 'loginInfo')
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Gets a export assets file based on what the user selected.
	 * @returns {Observable<R>}
	 */
	public downloadBundleFile(userEmail: string): Observable<any[]> {
		return this.http.get(`${this.export_asset_url }sendResetPasswordEmail?email=${userEmail}`)
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}
}
