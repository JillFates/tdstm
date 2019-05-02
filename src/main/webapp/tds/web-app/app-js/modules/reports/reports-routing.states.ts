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
import {ServerConflictsReportComponent} from './components/server-conflicts/server-conflicts-report.component';
import {TaskReportComponent} from './components/task-report/task-report.component';
import {ApplicationEventResultsReportComponent} from './components/application-event-results/application-event-results-report.component';
import {ApplicationProfilesReportComponent} from './components/application-profiles/application-profiles-report.component';

/**
 * Top menu parent section class for all Reports module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-planning';
const TOP_MENU_PARENT_REPORT_SECTION = 'menu-parent-reports';

export class ReportStates {
	public static readonly PRE_EVENT_CHECK_LIST = {
		url: 'preEventCheckList'
	};
	public static readonly TASK_REPORT = {
		url: 'taskReport'
	};
	public static readonly SERVER_CONFLICTS_REPORT = {
		url: 'serverConflicts'
	};
	public static readonly APPLICATION_EVENT_RESULTS = {
		url: 'applicationEventResults'
	};
	public static readonly APPLICATION_PROFILES = {
		url: 'applicationProfiles'
	};
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
		path: ReportStates.TASK_REPORT.url,
		data: {
			page: {
				title: 'REPORTS.TASK_REPORT',
				instruction: '',
				menu: ['REPORTS.REPORT', 'REPORTS.TASK_REPORT'],
				topMenu: { parent: TOP_MENU_PARENT_REPORT_SECTION, child: 'menu-reports-task-report', subMenu: true }
			},
			requiresAuth: true,
		},
		component: TaskReportComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	},
	{
		path: ReportStates.SERVER_CONFLICTS_REPORT.url,
		data: {
			page: {
				title: 'REPORTS.SERVER_CONFLICTS',
				instruction: '',
				menu: ['REPORTS.REPORTS', 'REPORTS.SERVER_CONFLICTS'],
				topMenu: { parent: TOP_MENU_PARENT_REPORT_SECTION, child: 'menu-reports-server-conflicts', subMenu: true }
			},
			requiresAuth: true,
		},
		component: ServerConflictsReportComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	},
	{
		path: ReportStates.APPLICATION_EVENT_RESULTS.url,
		data: {
			page: {
				title: 'REPORTS.APPLICATION_EVENT_RESULTS',
				instruction: '',
				menu: ['REPORTS.REPORTS', 'REPORTS.APPLICATION_EVENT_RESULTS'],
				topMenu: { parent: TOP_MENU_PARENT_REPORT_SECTION, child: 'menu-reports-application-migration', subMenu: true }
			},
			requiresAuth: true,
		},
		component: ApplicationEventResultsReportComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService
		],
		resolve: {},
		runGuardsAndResolvers: 'always'
	},
	{
		path: ReportStates.APPLICATION_PROFILES.url,
		data: {
			page: {
				title: 'Application Profiles',
				instruction: '',
				menu: ['REPORTS.REPORTS', 'Application Profiles'],
				topMenu: { parent: TOP_MENU_PARENT_REPORT_SECTION, child: 'menu-reports-application-profiles', subMenu: true }
			},
			requiresAuth: true,
		},
		component: ApplicationProfilesReportComponent,
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
