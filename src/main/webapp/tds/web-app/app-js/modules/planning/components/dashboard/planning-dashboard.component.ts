import {Component} from '@angular/core';
import {PlanningDashboardModel} from '../../models/planning-dashboard.model';

import {PlanningService} from '../../service/planning.service';

@Component({
	selector: 'planning-dashboard',
	templateUrl: 'planning-dashboard.component.html'
})

export class PlanningDashboardComponent {
	public percentage;
	public planningDashboardModel = new PlanningDashboardModel;

	constructor(private planningService: PlanningService) {
		const defaultModel = {
			discovery: {
				appsValidatedPercentage: 0,
				appsPlanReadyPercentage: 0,
				totals: [
					{
						type: 'Applications',
						img: '/tdstm/assets/icons/svg/application_menu.svg',
						link: '/tdstm/application/list?filter=application',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Physical Servers',
						img: '/tdstm/assets/icons/svg/serverPhysical_menu.svg',
						link: '/tdstm/assetEntity/list?filter=physicalServer',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Virtual Servers',
						img: '/tdstm/assets/icons/svg/serverVirtual_menu.svg',
						link: '/tdstm/assetEntity/list?filter=virtualServer',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Databases',
						img: '/tdstm/assets/icons/svg/database_menu.svg',
						link: '/tdstm/database/list?filter=db',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Physical Storage',
						img: '/tdstm/assets/icons/svg/storagePhysical_menu.svg',
						link: '/tdstm/assetEntity/list?filter=storage',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Logical Storage',
						img: '/tdstm/assets/icons/svg/storageLogical_menu.svg',
						link: '/tdstm/files/list?filter=storage',
						total: 0,
						toValidate: 0
					},
					{
						type: 'Other Devices',
						img: '/tdstm/assets/icons/svg/other_menu.svg',
						link: '/tdstm/assetEntity/list?filter=other',
						total: 0,
						toValidate: 0
					}
				],
				activeTasks: 0,
				overdueTasks: 0
			},
			analysis: {
				assignedAppPerc: 0,
				confirmedAppPerc: 0,
				validated: 0,
				planReady: 0,
				appDependenciesCount: 0,
				serverDependenciesCount: 0,
				pendingAppDependenciesCount: 0,
				pendingServerDependenciesCount: 0,
				activeTasks: 0,
				overdueTasks: 0,
				groupPlanMethodologyCount: []
			},
			execution: {
				movedAppPerc: 0,
				movedServerPerc: 0,
				unassignedAppCount: 0,
				appDonePerc: 0,
				unassignedPhyServerCount: 0,
				phyServerDonePerc: 0,
				unassignedVirtServerCount: 0,
				virtServerDonePerc: 0,
				unassignedDbCount: 0,
				dbDonePercentage: 0,
				unassignedPhyStorageCount: 0,
				phyStorageDonePerc: 0,
				unassignedFilesCount: 0,
				filesDonePerc: 0,
				unassignedOtherCount: 0,
				otherDonePerc: 0,
				unassignedAppPerc: 0,
				unassignedPhyServerPerc: 0,
				unassignedVirtServerPerc: 0,
				unassignedDbPerc: 0,
				unassignedPhyStoragePerc: 0,
				unassignedFilesPerc: 0,
				unassignedOtherPerc: 0,
				moveEventList: [],
				openTasks: [],
				appList: [],
				phyServerList: [],
				virtServerList: [],
				dbList: [],
				phyStorageList: [],
				filesList: [],
				otherList: [],
				eventStartDate: {}
			}
		};
		this.planningDashboardModel = Object.assign({},  defaultModel, this.planningDashboardModel);
		this.planningService.fetchModelForPlanningDashboard().subscribe(
			(result) => {
				if (result) {
					let model = this.planningDashboardModel;
					Object.keys(result.discovery).forEach(function(key) {
						if (key in model.discovery) {
							model.discovery[key] = result.discovery[key];
						}
					});
					Object.keys(result.analysis).forEach(function(key) {
						if (key in model.analysis) {
							model.analysis[key] = result.analysis[key];
						}
					});
					Object.keys(result.execution).forEach(function(key) {
						if (key in model.execution) {
							model.execution[key] = result.execution[key];
						}
					});
					this.planningDashboardModel.discovery.totals[0].total = result.discovery.appCount;
					this.planningDashboardModel.discovery.totals[0].toValidate = result.discovery.appToValidate;
					this.planningDashboardModel.discovery.totals[1].total = result.discovery.phyServerCount;
					this.planningDashboardModel.discovery.totals[1].toValidate = result.discovery.phyServerToValidate;
					this.planningDashboardModel.discovery.totals[2].total = result.discovery.virtServerCount;
					this.planningDashboardModel.discovery.totals[2].toValidate = result.discovery.virtServerToValidate;
					this.planningDashboardModel.discovery.totals[3].total = result.discovery.dbCount;
					this.planningDashboardModel.discovery.totals[3].toValidate = result.discovery.dbToValidate;
					this.planningDashboardModel.discovery.totals[4].total = result.discovery.phyStorageCount;
					this.planningDashboardModel.discovery.totals[4].toValidate = result.discovery.phyStorageToValidate;
					this.planningDashboardModel.discovery.totals[5].total = result.discovery.fileCount;
					this.planningDashboardModel.discovery.totals[5].toValidate = result.discovery.fileToValidate;
					this.planningDashboardModel.discovery.totals[6].total = result.discovery.otherCount;
					this.planningDashboardModel.discovery.totals[6].toValidate = result.discovery.otherToValidate;
					this.planningDashboardModel.analysis.groupPlanMethodologyCount = [];
					let groups = Object.keys(result.analysis.groupPlanMethodologyCount);
					for (let prop of groups) {
						this.planningDashboardModel.analysis.groupPlanMethodologyCount.push({type: prop, value: result.analysis.groupPlanMethodologyCount[prop]});
					}
				}
			});
	}
}