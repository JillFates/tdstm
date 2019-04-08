export class PlanningDashboardModel {
	discovery: {
		appsValidatedPercentage: number,
		appsPlanReadyPercentage: number,
		totals: [
			{
				type: 'Applications',
				img: '/tdstm/assets/icons/svg/application_menu.svg',
				link: '/tdstm/application/list?filter=application',
				total: number,
				toValidate: number
			},
			{
				type: 'Physical Servers',
				img: '/tdstm/assets/icons/svg/serverPhysical_menu.svg',
				link: '/tdstm/assetEntity/list?filter=physicalServer',
				total: number,
				toValidate: number
			},
			{
				type: 'Virtual Servers',
				img: '/tdstm/assets/icons/svg/serverVirtual_menu.svg',
				link: '/tdstm/assetEntity/list?filter=virtualServer',
				total: number,
				toValidate: number
			},
			{
				type: 'Databases',
				img: '/tdstm/assets/icons/svg/database_menu.svg',
				link: '/tdstm/database/list?filter=db',
				total: number,
				toValidate: number
			},
			{
				type: 'Physical Storage',
				img: '/tdstm/assets/icons/svg/storagePhysical_menu.svg',
				link: '/tdstm/assetEntity/list?filter=storage',
				total: number,
				toValidate: number
			},
			{
				type: 'Logical Storage',
				img: '/tdstm/assets/icons/svg/storageLogical_menu.svg',
				link: '/tdstm/files/list?filter=storage',
				total: number,
				toValidate: number
			},
			{
				type: 'Other Devices',
				img: '/tdstm/assets/icons/svg/other_menu.svg',
				link: '/tdstm/assetEntity/list?filter=other',
				total: number,
				toValidate: number
			}
		],
		activeTasks: number,
		overdueTasks: number
	}
	analysis: {
		assignedAppPerc: number,
		confirmedAppPerc: number,
		validated: number,
		planReady: number,
		appDependenciesCount: number,
		serverDependenciesCount: number,
		pendingAppDependenciesCount: number,
		pendingServerDependenciesCount: number,
		activeTasks: number,
		overdueTasks: number,
		groupPlanMethodologyCount: any[]
	}
	execution: {
		movedAppPerc: number,
		movedServerPerc: number,
		unassignedAppCount: number,
		unassignedAppPerc: number,
		appDonePerc: number,
		unassignedPhyServerCount: number,
		phyServerDonePerc: number,
		unassignedVirtServerCount: number,
		virtServerDonePerc: number,
		unassignedDbCount: number,
		dbDonePercentage: number,
		unassignedPhyStorageCount: number,
		phyStorageDonePerc: number,
		unassignedFilesCount: number,
		filesDonePerc: number,
		unassignedOtherCount: number,
		otherDonePerc: number,
		moveEventList: any[],
		openTasks: any[],
		appList: any[],
		phyServerList: any[],
		virtServerList: any[],
		dbList: any[],
		phyStorageList: any[],
		filesList: any[],
		otherList: any[],
		eventStartDate: any
	}
}
