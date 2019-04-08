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
				unassignedAppPerc: 0,
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
				moveEventList: [],
				openTasks: [],
				appList: [],
				phyServerList: [],
				virtServerList: [],
				dbList: [],
				phyStorageList: [],
				filesList: [],
				otherTypeList: [],
				eventStartDate: {}
			}
		};
		this.planningDashboardModel = Object.assign({},  defaultModel, this.planningDashboardModel);
		this.planningService.fetchModelForPlanningDashboard().subscribe(
			(result) => {
				if (result) {
					this.planningDashboardModel.discovery.appsValidatedPercentage = result.discovery.appsValidatedPercentage;
					this.planningDashboardModel.discovery.appsPlanReadyPercentage = result.discovery.appsPlanReadyPercentage;
					this.planningDashboardModel.discovery.activeTasks = result.discovery.activeTasks;
					this.planningDashboardModel.discovery.overdueTasks = result.discovery.overdueTasks;
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
					this.planningDashboardModel.analysis.assignedAppPerc = result.analysis.assignedAppPerc;
					this.planningDashboardModel.analysis.confirmedAppPerc = result.analysis.confirmedAppPerc;
					this.planningDashboardModel.analysis.validated = result.analysis.validated;
					this.planningDashboardModel.analysis.planReady = result.analysis.planReady;
					this.planningDashboardModel.analysis.pendingAppDependenciesCount = result.analysis.pendingAppDependenciesCount;
					this.planningDashboardModel.analysis.pendingServerDependenciesCount = result.analysis.pendingServerDependenciesCount;
					this.planningDashboardModel.analysis.appDependenciesCount = result.analysis.appDependenciesCount;
					this.planningDashboardModel.analysis.serverDependenciesCount = result.analysis.serverDependenciesCount;
					this.planningDashboardModel.analysis.activeTasks = result.analysis.activeTasks;
					this.planningDashboardModel.analysis.overdueTasks = result.analysis.overdueTasks;
					let groups = Object.keys(result.analysis.groupPlanMethodologyCount);
					for (let prop of groups) {
						this.planningDashboardModel.analysis.groupPlanMethodologyCount.push({type: prop, value: result.analysis.groupPlanMethodologyCount[prop]});
					}
					this.planningDashboardModel.execution.movedAppPerc = result.execution.movedAppPerc;
					this.planningDashboardModel.execution.movedServerPerc = result.execution.movedServerPerc;
					this.planningDashboardModel.execution.moveEventList = result.execution.moveEventList;
					this.planningDashboardModel.execution.openTasks = result.execution.openTasks;
					this.planningDashboardModel.execution.unassignedAppCount = result.execution.unassignedAppCount;
					this.planningDashboardModel.execution.unassignedAppPerc = result.execution.unassignedAppPerc;
					this.planningDashboardModel.execution.appDonePerc = result.execution.appDonePerc;
					this.planningDashboardModel.execution.appList = result.execution.appList;
					this.planningDashboardModel.execution.unassignedPhyServerCount = result.execution.unassignedPhyServerCount;
					this.planningDashboardModel.execution.phyServerDonePerc = result.execution.phyServerDonePerc;
					this.planningDashboardModel.execution.phyServerList = result.execution.phyServerList;
					this.planningDashboardModel.execution.unassignedVirtServerCount = result.execution.unassignedVirtServerCount;
					this.planningDashboardModel.execution.virtServerDonePerc = result.execution.virtServerDonePerc;
					this.planningDashboardModel.execution.virtServerList = result.execution.virtServerList;
					this.planningDashboardModel.execution.unassignedDbCount = result.execution.unassignedDbCount;
					this.planningDashboardModel.execution.dbDonePercentage = result.execution.dbDonePercentage;
					this.planningDashboardModel.execution.dbList = result.execution.dbList;
					this.planningDashboardModel.execution.unassignedPhyStorageCount = result.execution.unassignedPhyStorageCount;
					this.planningDashboardModel.execution.phyStorageDonePerc = result.execution.phyStorageDonePerc;
					this.planningDashboardModel.execution.phyStorageList = result.execution.phyStorageList;
					this.planningDashboardModel.execution.unassignedFilesCount = result.execution.unassignedFilesCount;
					this.planningDashboardModel.execution.filesDonePerc = result.execution.filesDonePerc;
					this.planningDashboardModel.execution.filesList = result.execution.filesList;
					this.planningDashboardModel.execution.unassignedOtherCount = result.execution.unassignedOtherCount;
					this.planningDashboardModel.execution.otherDonePerc = result.execution.otherDonePerc;
					this.planningDashboardModel.execution.otherList = result.execution.otherList;
					this.planningDashboardModel.execution.eventStartDate = result.execution.eventStartDate;
				}
			});
	}
}