export class PlanningDashboardModel {
	discovery: {
		appsValidatedPercentage: number,
		appsPlanReadyPercentage: number,
		totals: [
			{
				type: "Applications",
				total: number,
				toValidate: number
			},
			{
				type: "Physical Servers",
				total: number,
				toValidate: number
			},
			{
				type: "Virtual Servers",
				total: number,
				toValidate: number
			},
			{
				type: "Databases",
				total: number,
				toValidate: number
			},
			{
				type: "Physical Storage",
				total: number,
				toValidate: number
			},
			{
				type: "Logical Storage",
				total: number,
				toValidate: number
			},
			{
				type: "Other Devices",
				total: number,
				toValidate: number
			}
		],
		activeTasks: number
	}
}
