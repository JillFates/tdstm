export class PlanningDashboardModel {
	discovery: {
		appsValidatedPercentage: number,
		appsPlanReadyPercentage: number,
		totals: [
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
				total: number,
				toValidate: number
			},
			{
				type: string,
				img: string,
				link: string,
				queryParams: string,
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
		unassignedAppPerc: number,
		unassignedPhyServerPerc: number,
		unassignedVirtServerPerc: number,
		unassignedDbPerc: number,
		unassignedPhyStoragePerc: number,
		unassignedFilesPerc: number,
		unassignedOtherPerc: number,
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
