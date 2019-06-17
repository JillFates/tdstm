// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {BundleListComponent} from './components/list/bundle-list.component';
import {BundleResolveService} from './resolve/bundle-resolve.service';
import {BundleShowComponent} from './components/show/bundle-show.component';
import {BundleCreateComponent} from './components/create/bundle-create.component';
import {BundleEditComponent} from './components/edit/bundle-edit.component';

/**
 * Asset Explorer Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Explorer Module
 */
export class BundleStates {
	public static readonly BUNDLE_LIST = {
		url: 'list'
	}
	public static readonly BUNDLE_SHOW = {
		url: ':id/show'
	}
	public static readonly BUNDLE_EDIT = {
		url: ':id/edit'
	}
	public static readonly BUNDLE_CREATE = {
		url: 'create'
	}
}

export const BundleRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: BundleStates.BUNDLE_LIST.url},
	{
		path: BundleStates.BUNDLE_LIST.url,
		data: {
			page: {
				title: 'BUNDLE.LIST',
				instruction: '',
				menu: ['BUNDLE.BUNDLE', 'BUNDLE.LIST'],
			},
			requiresAuth: true
		},
		component: BundleListComponent,
		resolve: {
			bundles: BundleResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: BundleStates.BUNDLE_SHOW.url,
		data: {
			page: {
				title: 'BUNDLE.SHOW',
				instruction: '',
				menu: ['BUNDLE.BUNDLE', 'BUNDLE.SHOW'],
			},
			requiresAuth: true
		},
		component: BundleShowComponent,
		resolve: {
			bundles: BundleResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: BundleStates.BUNDLE_EDIT.url,
		data: {
			page: {
				title: 'BUNDLE.EDIT',
				instruction: '',
				menu: ['BUNDLE.BUNDLE', 'BUNDLE.EDIT'],
			},
			requiresAuth: true
		},
		component: BundleEditComponent,
		resolve: {
			bundles: BundleResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: BundleStates.BUNDLE_CREATE.url,
		data: {
			page: {
				title: 'BUNDLE.CREATE',
				instruction: '',
				menu: ['BUNDLE.CREATE', 'BUNDLE.CREATE'],
			},
			requiresAuth: true
		},
		component: BundleCreateComponent,
		resolve: {
			bundles: BundleResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(BundleRoute)]
})

export class BundleRouteModule {
}
