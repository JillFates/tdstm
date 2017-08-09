import { Ng2StateDeclaration } from '@uirouter/angular';
import { Observable } from 'rxjs/Rx';

import { AssetExplorerIndexComponent } from './components/index/asset-explorer-index.component';
import { AssetExplorerReportConfigComponent } from './components/report-config/asset-explorer-report-config.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

import { AssetExplorerService } from './service/asset-explorer.service';
import { ReportModel } from './model/report.model';
import { FieldSettingsService } from '../fieldSettings/service/field-settings.service';

export class AssetExplorerStates {
	public static readonly REPORT_SELECTOR = {
		name: 'tds.assetexplorer',
		url: '/assetexplorer/reports'
	};
	public static readonly REPORT_CREATE = {
		name: 'tds.assetexplorer.create',
		url: '/create'
	};
	public static readonly REPORT_EDIT = {
		name: 'tds.assetexplorer.edit',
		url: ':id/edit'
	};
}

/**
 * This state displays the field settings list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The field settings are fetched using a resolve.
 */
export const assetExplorerReportSelectorState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetExplorerStates.REPORT_SELECTOR.name,
	url: AssetExplorerStates.REPORT_SELECTOR.url,
	data: {
		page: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER']
		},
		requiresAuth: true,
		// requiresPermission: 'AssetExplorerSelection'
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerIndexComponent }
	},
	resolve: [
		{
			token: 'reports',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService],
			resolveFn: (service: AssetExplorerService) => service.getReports()
		}
	]
};

export const assetExplorerReportCreatorState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetExplorerStates.REPORT_CREATE.name,
	url: AssetExplorerStates.REPORT_CREATE.url,
	data: {
		page: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER', 'ASSET_EXPLORER.CREATE']
		},
		requiresAuth: true,
		requiresPermission: 'AssetExplorerCreate'
	},
	views: {
		'containerView@tds': { component: AssetExplorerReportConfigComponent }
	},
	resolve: [
		{
			token: 'fields',
			policy: { async: 'RXWAIT' },
			deps: [FieldSettingsService],
			resolveFn: (service: FieldSettingsService) => service.getFieldSettingsByDomain()
		}, {
			token: 'report',
			policy: { async: 'RXWAIT' },
			resolveFn: () => Observable.from([new ReportModel()])
		}
	]
};

export const assetExplorerReportEditState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetExplorerStates.REPORT_EDIT.name,
	url: AssetExplorerStates.REPORT_EDIT.url,
	data: {
		page: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER', 'ASSET_EXPLORER.CREATE']
		},
		requiresAuth: true,
		requiresPermission: 'AssetExplorerCreate'
	},
	views: {
		'containerView@tds': { component: AssetExplorerReportConfigComponent }
	},
	resolve: [
		{
			token: 'fields',
			policy: { async: 'RXWAIT' },
			deps: [FieldSettingsService],
			resolveFn: (service: FieldSettingsService) => service.getFieldSettingsByDomain()
		}, {
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService],
			resolveFn: (service: AssetExplorerService, trans) => service.getReport(trans.params().id)
		}
	]
};

export const ASSET_EXPLORER_STATES = [
	assetExplorerReportSelectorState,
	assetExplorerReportCreatorState,
	assetExplorerReportEditState
];