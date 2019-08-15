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
	private export_asset_url = '/tdstm/ws/asset/';
	private progress_export_asset_url = '/tdstm/ws/progress/';
	private bundle_file_url = '/tdstm/assetEntity/downloadExport';

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
		return this.http.get(this.export_asset_url + 'bundlesAndPreferencesForAssetExport')
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * Gets a export assets key for a file based on what the user selected.
	 * @returns {Observable<R>}
	 */
	public downloadBundleFile(exportData: any): Observable<any[]> {
		return this.http.post(`${this.export_asset_url }exportAssets`, exportData )
			.map((response: any) => {
				return response && response.data;
			})
			.catch((error: any) => error);
	}

	/** Gets the actual file
	 * @returns info for file
	 * */
	public getProgress(key: string): Observable<any[]> {
		return this.http.get(`${this.progress_export_asset_url + key}`).map(
			(response: any) => {
				return response && response.data
			})
			.catch( (error: any) => error );
	}

	/** Gets the actual file
	 * @returns info for file
	 * */
	public getBundleFile(key: string) {
		return `${this.bundle_file_url}?key=${key}`;
	}
}
