// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {PreEventCheckListSelectorComponent} from './components/event-checklist/pre-event-checklist.component';
import {ApplicationConflictsComponent} from './components/application-conflicts/application-conflicts.component';

/**
 * Top menu parent section class for all Reports module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-planning';

export class ReportStates {
	public static readonly PRE_EVENT_CHECK_LIST = {
		url: 'preEventCheckList'
	};
	public static readonly APPLICATION_CONFLICTS = {
		url: 'applicationConflicts'
	}
}

export const ReportsRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ReportStates.PRE_EVENT_CHECK_LIST.url},
	{
		path: ReportStates.PRE_EVENT_CHECK_LIST.url,
		data: {
			page: {
				title: 'PLANNING.PRE_EVENT_CHECKLIST',
				instruction: '',
				menu: ['PLANNING.PLANNING', 'PLANNING.PRE_EVENT_CHECKLIST'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-planning-pre-checklist', subMenu: true }
			},
			requiresAuth: true,
		},
		component: PreEventCheckListSelectorComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	},
	{
		path: ReportStates.APPLICATION_CONFLICTS.url,
		data: {
			page: {
				title: 'REPORTS.APPLICATION_CONFLICTS',
				instruction: '',
				menu: ['REPORTS.REPORTS', 'REPORTS.APPLICATION_CONFLICTS'],
				topMenu: { parent: 'menu-parent-reports', child: 'menu-reports-application-conflicts2', subMenu: true }
			},
			requiresAuth: true,
		},
		component: ApplicationConflictsComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ReportsRoute)]
})
export class ReportsRouteModule {
}
