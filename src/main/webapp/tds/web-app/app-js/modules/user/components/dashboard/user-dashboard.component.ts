import {Component, OnInit} from '@angular/core';
import {UserService} from '../../service/user.service';
import {
	ActivePersonColumnModel,
	ApplicationColumnModel,
	EventColumnModel, EventNewsColumnModel
} from '../../model/user-dashboard-columns.model';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';

@Component({
	selector: 'user-dashboard',
	templateUrl: '../tds/web-app/app-js/modules/user/components/dashboard/user-dashboard.component.html'
})

export class UserDashboardComponent implements OnInit {
	public applicationList;
	public applicationColumnModel;
	public activePersonList;
	public activePersonColumnModel;
	public eventList;
	public eventColumnModel;
	public eventNewsList;
	public eventNewsColumnModel;
	public summaryDetail;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	constructor(private userService: UserService) {

	}

	ngOnInit() {
		this.userService.fetchModelForUserDashboard()
			.subscribe((result) => {
				this.applicationList = result.applications;
				this.activePersonList = result.activePeople;
				this.summaryDetail = result.summaryDetail;
				this.eventList = result.events;
				this.eventNewsList = result.eventNews;
			});

		this.applicationColumnModel = new ApplicationColumnModel();
		this.activePersonColumnModel = new ActivePersonColumnModel();
		this.eventColumnModel = new EventColumnModel();
		this.eventNewsColumnModel = new EventNewsColumnModel();
	}
}