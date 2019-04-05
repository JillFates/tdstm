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
				activeTasks: 0
			},
			analysis: {
				assignedAppPerc: 0,
				confirmedAppPerc: 0,
				validated: 0,
				planReady: 0,
				pendingAppDependenciesCount: 0,
				pendingServerDependenciesCount: 0,
				appDependenciesCount: 0,
				serverDependenciesCount: 0,
				activeTasks: 0,
				overdueTasks: 0,
				groupPlanMethodologyCount: []
			},
			execution: {
				movedAppPerc: 0,
				movedServersPerc: 0,
				moveEventList: [],
				openTasks: [],
				unassignedAppCount: 0,
				percentageUnassignedAppCount: 0,
				percAppDoneCount: 0,
				unassignedPhysicalServerCount: 0,
				percentagePhysicalServerCount: 0,
				unassignedVirtualServerCount: 0,
				percVirtualServerCount: 0,
				unassignedDbCount: 0,
				percentageDBCount: 0,
				unAssignedPhyStorageCount: 0,
				percentagePhyStorageCount: 0,
				unassignedFilesCount: 0,
				percentageFilesCount: 0,
				unassignedOtherCount: 0,
				percentageOtherCount: 0,
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
					this.planningDashboardModel.discovery.appsValidatedPercentage = 100 - result.percentageAppToValidate;
					this.planningDashboardModel.discovery.appsPlanReadyPercentage = result.percentagePlanReady;
					this.planningDashboardModel.discovery.activeTasks = result.openIssue;
					this.planningDashboardModel.discovery.overdueTasks = result.dueOpenIssue;
					this.planningDashboardModel.discovery.totals[0].total = result.applicationCount;
					this.planningDashboardModel.discovery.totals[0].toValidate = result.appToValidate;
					this.planningDashboardModel.discovery.totals[1].total = result.phyServerCount;
					this.planningDashboardModel.discovery.totals[1].toValidate = result.psToValidate;
					this.planningDashboardModel.discovery.totals[2].total = result.virtServerCount;
					this.planningDashboardModel.discovery.totals[2].toValidate = result.vsToValidate;
					this.planningDashboardModel.discovery.totals[3].total = result.dbCount;
					this.planningDashboardModel.discovery.totals[3].toValidate = result.dbToValidate;
					this.planningDashboardModel.discovery.totals[4].total = result.phyStorageCount;
					this.planningDashboardModel.discovery.totals[4].toValidate = result.phyStorageToValidate;
					this.planningDashboardModel.discovery.totals[5].total = result.fileCount;
					this.planningDashboardModel.discovery.totals[5].toValidate = result.fileToValidate;
					this.planningDashboardModel.discovery.totals[6].total = result.otherAssetCount;
					this.planningDashboardModel.discovery.totals[6].toValidate = result.otherToValidate;
					this.planningDashboardModel.analysis.assignedAppPerc = result.assignedAppPerc;
					this.planningDashboardModel.analysis.confirmedAppPerc = result.confirmedAppPerc;
					this.planningDashboardModel.analysis.validated = result.validated;
					this.planningDashboardModel.analysis.planReady = result.planReady;
					this.planningDashboardModel.analysis.pendingAppDependenciesCount = result.pendingAppDependenciesCount;
					this.planningDashboardModel.analysis.pendingServerDependenciesCount = result.pendingServerDependenciesCount;
					this.planningDashboardModel.analysis.appDependenciesCount = result.appDependenciesCount;
					this.planningDashboardModel.analysis.serverDependenciesCount = result.serverDependenciesCount;
					this.planningDashboardModel.analysis.activeTasks = result.issuesCount;
					this.planningDashboardModel.analysis.overdueTasks = result.generalOverDue;
					let groups = Object.keys(result.groupPlanMethodologyCount);
					for (let prop of groups) {
						this.planningDashboardModel.analysis.groupPlanMethodologyCount.push({type: prop, value: result.groupPlanMethodologyCount[prop]});
					}
					this.planningDashboardModel.execution.movedAppPerc = result.movedAppPerc;
					this.planningDashboardModel.execution.movedServersPerc = result.movedServersPerc;
					this.planningDashboardModel.execution.moveEventList = result.moveEventList;
					this.planningDashboardModel.execution.openTasks = result.openTasks;
					this.planningDashboardModel.execution.unassignedAppCount = result.unassignedAppCount;
					this.planningDashboardModel.execution.percentageUnassignedAppCount = result.percentageUnassignedAppCount;
					this.planningDashboardModel.execution.percAppDoneCount = result.percAppDoneCount;
					this.planningDashboardModel.execution.appList = result.appList;
					this.planningDashboardModel.execution.unassignedPhysicalServerCount = result.unassignedPhysicalServerCount;
					this.planningDashboardModel.execution.percentagePhysicalServerCount = result.percentagePhysicalServerCount;
					this.planningDashboardModel.execution.phyServerList = result.phyServerList;
					this.planningDashboardModel.execution.unassignedVirtualServerCount = result.unassignedVirtualServerCount;
					this.planningDashboardModel.execution.percVirtualServerCount = result.percVirtualServerCount;
					this.planningDashboardModel.execution.virtServerList = result.virtServerList;
					this.planningDashboardModel.execution.unassignedDbCount = result.unassignedDbCount;
					this.planningDashboardModel.execution.percentageDBCount = result.percentageDBCount;
					this.planningDashboardModel.execution.dbList = result.dbList;
					this.planningDashboardModel.execution.unAssignedPhyStorageCount = result.unAssignedPhyStorageCount;
					this.planningDashboardModel.execution.percentagePhyStorageCount = result.percentagePhyStorageCount;
					this.planningDashboardModel.execution.phyStorageList = result.phyStorageList;
					this.planningDashboardModel.execution.unassignedFilesCount = result.unassignedFilesCount;
					this.planningDashboardModel.execution.percentageFilesCount = result.percentageFilesCount;
					this.planningDashboardModel.execution.filesList = result.filesList;
					this.planningDashboardModel.execution.unassignedOtherCount = result.unassignedOtherCount;
					this.planningDashboardModel.execution.percentageOtherCount = result.percentageOtherCount;
					this.planningDashboardModel.execution.otherTypeList = result.otherTypeList;
					this.planningDashboardModel.execution.eventStartDate = result.eventStartDate;
				}
			});
	}
}