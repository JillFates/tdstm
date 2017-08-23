import { Ng2StateDeclaration, Transition } from '@uirouter/angular';
import { Observable } from 'rxjs/Rx';

import { AssetExplorerIndexComponent } from './components/index/asset-explorer-index.component';
import { AssetExplorerReportConfigComponent } from './components/report-config/asset-explorer-report-config.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

import { AssetExplorerService } from './service/asset-explorer.service';
import { ReportModel } from './model/report.model';
import { CustomDomainService } from '../fieldSettings/service/custom-domain.service';

export class AssetExplorerStates {
	public static readonly REPORT_SELECTOR = {
		name: 'tds.assetexplorer',
		url: '/assetexplorer/views'
	};
	public static readonly REPORT_CREATE = {
		name: 'tds.assetexplorer_create',
		url: '/assetexplorer/views/create'
	};
	public static readonly REPORT_EDIT = {
		name: 'tds.assetexplorer_edit',
		url: '/assetexplorer/views/:id/edit'
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
		requiresPermission: 'AssetExplorerCreate',
		hasPendingChanges: false
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerReportConfigComponent }
	},
	resolve: [
		{
			token: 'fields',
			policy: { async: 'RXWAIT' },
			deps: [CustomDomainService],
			resolveFn: (service: CustomDomainService) => service.getCommonFieldSpecs().map(domains => {
				let commonIndex = domains.findIndex(x => x.domain.toUpperCase() === 'COMMON');
				if (commonIndex !== -1) {
					let common = domains.splice(commonIndex, 1);
					domains = common.concat(domains);
				}
				domains.forEach(d => {
					d.fields = d.fields.sort((a, b) => a.label > b.label ? 1 : b.label > a.label ? -1 : 0);
				});
				return domains;
			})
		}, {
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [Transition],
			resolveFn: (trans: Transition) => {
				let model = new ReportModel();
				let params = trans.targetState().params() as any;
				model.isSystem = params.system || false;
				model.isShared = params.shared || false;
				return Observable.from([model]);
			}
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
		requiresPermission: 'AssetExplorerCreate',
		hasPendingChanges: false
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerReportConfigComponent }
	},
	resolve: [
		{
			token: 'fields',
			policy: { async: 'RXWAIT' },
			deps: [CustomDomainService],
			resolveFn: (service: CustomDomainService) => service.getCommonFieldSpecs().map(domains => {
				let commonIndex = domains.findIndex(x => x.domain.toUpperCase() === 'COMMON');
				if (commonIndex !== -1) {
					let common = domains.splice(commonIndex, 1);
					domains = common.concat(domains);
				}
				domains.forEach(d => {
					d.fields = d.fields.sort((a, b) => a.label > b.label ? 1 : b.label > a.label ? -1 : 0);
					d.fields.forEach(f => f['domain'] = d.domain);
				});
				return domains;
			})
		}, {
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService, Transition],
			resolveFn: (service: AssetExplorerService, trans: Transition) => service.getReport(trans.params().id)
		}
	]
};

export const ASSET_EXPLORER_STATES = [
	assetExplorerReportSelectorState,
	assetExplorerReportCreatorState,
	assetExplorerReportEditState
];