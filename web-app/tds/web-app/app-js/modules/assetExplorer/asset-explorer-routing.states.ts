import { Ng2StateDeclaration, Transition } from '@uirouter/angular';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { AssetExplorerIndexComponent } from './components/index/asset-explorer-index.component';
import { AssetExplorerViewConfigComponent } from './components/view-config/asset-explorer-view-config.component';
import { AssetExplorerViewShowComponent } from './components/view-show/asset-explorer-view-show.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

import { AssetExplorerService } from './service/asset-explorer.service';
import { CustomDomainService } from '../fieldSettings/service/custom-domain.service';
import { PreferenceService } from '../../shared/services/preference.service';
import {TagService} from '../assetTags/service/tag.service';
import {ApiResponseModel} from '../../shared/model/ApiResponseModel';

/**
 * Top menu parent section class for all Assets Explorer module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

const assetsListSizeResolve = {
	token: 'preferences',
	policy: { async: 'RXWAIT', when: 'EAGER' },
	deps: [PreferenceService],
	resolveFn: (service: PreferenceService) => service.getPreference('assetListSize')
};

const fieldsResolve = {
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
			d.fields.forEach(f => f['domain'] = d.domain.toLowerCase());
		});
		return domains;
	})
};

const resolveTagList = {
	token: 'tagList',
	policy: { async: 'RXWAIT' },
	deps: [TagService],
	resolveFn: (tagService: TagService) => tagService.getTags().map( (result: ApiResponseModel) => {
		return result.status === ApiResponseModel.API_SUCCESS && result.data ? result.data : [];
	})
};

export class AssetExplorerStates {
	public static readonly REPORT_SELECTOR = {
		name: 'tds.assetexplorer',
		url: '/asset/views'
	};
	public static readonly REPORT_CREATE = {
		name: 'tds.assetexplorer_create',
		url: '/asset/views/create'
	};
	public static readonly REPORT_EDIT = {
		name: 'tds.assetexplorer_edit',
		url: '/asset/views/:id/edit'
	};
	public static readonly REPORT_SHOW = {
		name: 'tds.assetexplorer_show',
		url: '/asset/views/:id/show'
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
			menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER'],
			topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-asset-manager', subMenu: true }
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
			menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: AssetExplorerStates.REPORT_SELECTOR.name }, 'ASSET_EXPLORER.CREATE']
		},
		requiresAuth: true,
		requiresPermission: 'AssetExplorerCreate',
		hasPendingChanges: false
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerViewConfigComponent }
	},
	resolve: [
		assetsListSizeResolve,
		fieldsResolve,
		{
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [Transition],
			resolveFn: (trans: Transition) => Observable.of(trans.targetState().params())
		}, {
			token: 'reports',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService],
			resolveFn: (service: AssetExplorerService) => service.getReports()
		},
		resolveTagList
	]
};

export const assetExplorerReportEditState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetExplorerStates.REPORT_EDIT.name,
	url: AssetExplorerStates.REPORT_EDIT.url,
	data: {
		page: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: AssetExplorerStates.REPORT_SELECTOR.name }, 'ASSET_EXPLORER.EDIT']
		},
		requiresAuth: true,
		requiresPermission: 'AssetExplorerEdit',
		hasPendingChanges: false
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerViewConfigComponent }
	},
	resolve: [
		assetsListSizeResolve,
		fieldsResolve,
		{
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService, Transition],
			resolveFn: (service: AssetExplorerService, trans: Transition) => service.getReport(trans.params().id)
		}, {
			token: 'reports',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService],
			resolveFn: (service: AssetExplorerService) => service.getReports()
		},
		resolveTagList
	]
};

export const assetExplorerReportShowState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: AssetExplorerStates.REPORT_SHOW.name,
	url: AssetExplorerStates.REPORT_SHOW.url,
	data: {
		page: {
			title: 'ASSET_EXPLORER.ASSET_EXPLORER',
			instruction: '',
			menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: AssetExplorerStates.REPORT_SELECTOR.name }, 'ASSET_EXPLORER.SHOW'],
			topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-asset-explorer', subMenu: true }
		},
		requiresAuth: true
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: AssetExplorerViewShowComponent }
	},
	resolve: [
		assetsListSizeResolve,
		fieldsResolve,
		{
			token: 'report',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService, Transition],
			resolveFn: (service: AssetExplorerService, trans: Transition) => service.getReport(trans.params().id)
		}, {
			token: 'reports',
			policy: { async: 'RXWAIT' },
			deps: [AssetExplorerService],
			resolveFn: (service: AssetExplorerService) => service.getReports()
		},
		resolveTagList
	]
};

export const ASSET_EXPLORER_STATES = [
	assetExplorerReportSelectorState,
	assetExplorerReportCreatorState,
	assetExplorerReportEditState,
	assetExplorerReportShowState,
	{
		name: 'tds.assetexplorerCopy',
		url: '/asset/views/',
		redirectTo: 'tds.assetexplorer'
	}
];
