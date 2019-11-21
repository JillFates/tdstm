// Angular
import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { pathOr } from 'ramda';
import * as R from  'ramda';
import 'hammerjs';
// Store
import {Store} from '@ngxs/store';
// Action
// import {SetEvent} from '../../action/insight.actions';
// Services
import { InsightService } from '../../service/insight.service';
// import { EventModel, EventPlanStatus } from '../../model/event.model';
// Components

// Model

import {ActivatedRoute} from '@angular/router';

@Component({
	selector: 'insight-dashboard',
	templateUrl: 'insight-dashboard.component.html'
})

export class InsightDashboardComponent implements OnInit {

	public insightData: any;

	private showAssetsByVendorTable = false;
	private showTopTags = false;
	private showDependenciesByVendor = false;
	private showApplicationGroupedByDependencies = false;

	constructor(
		private route: ActivatedRoute,
		private insightService: InsightService
		) {}

	ngOnInit() {
		this.populateData();
	}

	/**
	 * Call the endpoints required to populate the initial data
	 */
	private populateData(): void {
		// this.store.select(state => state.TDSApp.userContext)
		// 	.subscribe((userContext: UserContextModel) => {
		// 		this.userTimeZone = userContext.timezone;
		// 	});
		// this.eventsService.getEvents()
		// 	.subscribe((events: any) => {
		// 		this.eventList = events;
		//
		// 		this.store.select(state => state.TDSApp.userContext)
		// 			.pipe(takeWhile((event: any) => !this.selectedEvent))
		// 			.subscribe((userContext: UserContextModel) => {
		// 				let selectedEventId = null;
		// 				if (userContext && userContext.event) {
		// 					selectedEventId = userContext.event.id;
		// 				}
		//
		// 				this.selectedEvent = this.getDefaultEvent(this.route.snapshot.queryParams['moveEvent'] || selectedEventId);
		// 				if (this.selectedEvent) {
		// 					this.onSelectedEvent(this.selectedEvent.id, this.selectedEvent.name);
		// 				}
		// 			});
		// 	});

		this.insightService.getInsightData().subscribe( (insightData: any) => {
			// For demo purposes:
			// insightData.assetsByVendor[0].count = 74;
			// insightData.dependenciesByVendor[0].count = 20;
			// insightData.topTags[0].count = 40;
			// insightData.applicationsGroupedByDependencies[0].count = 44;
			this.insightData = insightData;
			console.log(insightData);
		} );
	}

	toggleTableFor(chartName) {
		switch (chartName) {
			case 'ASSETS_BY_VENDOR': {
				this.showAssetsByVendorTable = !this.showAssetsByVendorTable;
				break;
			}
			case 'DEPENDENCIES_BY_VENDOR': {
				this.showDependenciesByVendor = !this.showDependenciesByVendor;
				break;
			}
			case 'TOP_TAGS': {
				this.showTopTags = !this.showTopTags;
				break;
			}
			case 'APPLICATION_GROUPED_BY_DEPENDENCIES': {
				this.showApplicationGroupedByDependencies = !this.showApplicationGroupedByDependencies;
				break;
			}
		}
	}
}
