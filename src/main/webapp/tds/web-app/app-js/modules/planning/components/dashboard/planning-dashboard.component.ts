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
						type: "Applications",
						total: 0,
						toValidate: 0
					},
					{
						type: "Physical Servers",
						total: 0,
						toValidate: 0
					},
					{
						type: "Virtual Servers",
						total: 0,
						toValidate: 0
					},
					{
						type: "Databases",
						total: 0,
						toValidate: 0
					},
					{
						type: "Physical Storage",
						total: 0,
						toValidate: 0
					},
					{
						type: "Logical Storage",
						total: 0,
						toValidate: 0
					},
					{
						type: "Other Devices",
						total: 0,
						toValidate: 0
					}
				],
				activeTasks: 0
			}
		};
		this.planningDashboardModel = Object.assign({},  defaultModel, this.planningDashboardModel);
		this.planningService.fetchModelForPlanningDashboard().subscribe(
			(result) => {
				if (result) {
					this.planningDashboardModel.discovery.appsValidatedPercentage = 100 - result.percentageAppToValidate;
					this.planningDashboardModel.discovery.appsPlanReadyPercentage = result.percentagePlanReady;
				}
			});
	}
}