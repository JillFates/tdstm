import {AssetQueryParams} from './asset-query-params';
import {DomainModel} from '../../fieldSettings/model/domain.model';

export class AssetExportModel {
	assetQueryParams: AssetQueryParams;
	queryId?: number;
	totalData?: number;
	domains: DomainModel[];
	viewName?: string;
}