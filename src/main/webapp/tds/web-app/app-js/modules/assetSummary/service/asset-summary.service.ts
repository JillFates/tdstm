// Angular
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
// Others
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Observable} from 'rxjs';

@Injectable()
export class AssetSummaryService {

	private defaultUrl = '../ws';

	constructor(private http: HttpClient) {
	}

	getSummaryTable(): Observable<any> {
		return this.http.get(`${this.defaultUrl}/asset/summaryTable`)
			.map((response: any) => {
				let gridData: any[] = [];

				if (response.data && response.data.assetSummaryList) {
					response.data.assetSummaryList.forEach((bundle: any) => {
						gridData.push({
							bundle: {
								id: bundle.id,
								name: bundle.name,
							},
							application: {
								id: 7,
								count: bundle.applicationCount
							},
							server: {
								id: 4,
								count: bundle.serverCount
							},
							device: {
								id: 3,
								count: bundle.physicalCount
							},
							database: {
								id: 2,
								count: bundle.databaseCount
							},
							storage: {
								id: 6,
								count: bundle.filesCount
							}
						});
					});
				}
				return gridData;
			})
			.catch((error: any) => error);
	}

}