import { Ng2StateDeclaration } from '@uirouter/angular';
import { AssetExplorerReportSelectorComponent } from './components/report-selector/asset-explorer-report-selector.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

import { AssetExplorerService } from './service/asset-explorer.service';

export class AssetExplorerStates {
	public static readonly REPORT_SELECTOR = {
		name: 'tds.assetexplorer',
		url: '/assetexplorer/reports'
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
		'containerView@tds': { component: AssetExplorerReportSelectorComponent }
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

export const ASSET_EXPLORER_STATES = [
	assetExplorerReportSelectorState
];