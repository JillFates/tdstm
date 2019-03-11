// Angular
import {Component, OnInit} from '@angular/core';
// Services
import {TaskService} from '../../../taskManager/service/task.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserService} from '../../service/user.service';
// Components
import {TaskDetailComponent} from '../../../taskManager/components/detail/task-detail.component';
import {UserManageStaffComponent} from '../../../../shared/modules/header/components/manage-staff/user-manage-staff.component';
import {AssetShowComponent} from '../../../assetExplorer/components/asset/asset-show.component';
// Model
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
import {
	ActivePersonColumnModel,
	ApplicationColumnModel,
	EventColumnModel, EventNewsColumnModel, TaskColumnModel
} from '../../model/user-dashboard-columns.model';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {DIALOG_SIZE} from '../../../../shared/model/constants';

@Component({
	selector: 'user-dashboard',
	templateUrl: 'user-dashboard.component.html'
})

export class UserDashboardComponent implements OnInit {
	public currentPerson;
	public selectedProject;
	public projectInstance;
	public movedayCategories;
	public projectList;
	public applicationList;
	public applicationColumnModel;
	public activePersonList;
	public activePersonColumnModel;
	public eventList;
	public eventColumnModel;
	public eventNewsList;
	public eventNewsColumnModel;
	public taskList;
	public taskColumnModel;
	public summaryDetail;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	private itemForDoubleClick;

	constructor(private userService: UserService, private taskService: TaskService, private dialogService: UIDialogService) {

	}

	ngOnInit() {
		this.populateData();
	}

	onChangeProject() {
		this.populateData(this.selectedProject.id);
	}

	populateData(projId = '') {
		this.userService.fetchModelForUserDashboard(projId)
			.subscribe((result) => {
				this.fetchApplicationsForGrid();
				this.fetchEventNewsForGrid();
				this.fetchEventsForGrid();
				this.fetchPeopleForGrid();
				this.fetchTasksForGrid();
				this.projectList = result.projects;
				this.currentPerson = result.person;
				this.movedayCategories = result.movedayCategories;
				this.projectInstance = result.projectInstance;
				this.selectedProject = this.projectInstance;
			});
		this.applicationColumnModel = new ApplicationColumnModel();
		this.activePersonColumnModel = new ActivePersonColumnModel();
		this.eventColumnModel = new EventColumnModel();
		this.eventNewsColumnModel = new EventNewsColumnModel();
		this.taskColumnModel = new TaskColumnModel();
	}

	openLinkInNewTab(url) {
		window.open(url, '_blank');
	}

	getMarkupUrlData(url) {
		if (url) {
			let data = url.split('|');
			if (data.length === 1) {
				data[1] = data[0];
				data[0] = 'Instructions...';
			}
			return data;
		}
		return null;
	}

	changeTimeEst(id, days) {
		this.taskService.changeTimeEst(id, days)
			.subscribe(() => {
				this.fetchTasksForGrid();
			});
	}

	public openTaskDetailView(comment: any): void {
		let taskDetailModel: TaskDetailModel = {
			id: comment.taskId,
			modal: {
				title: 'Task Detail'
			},
			detail: {
				currentUserId: 5662
			}
		};
		this.dialogService.extra(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		]).then(() => {
			this.fetchTasksForGrid();
		}).catch(result => {
			if (!result) {
				this.fetchTasksForGrid();
			}
		});
	}

	updateTaskStatus(id, status) {
		this.taskService.updateStatus(id, status)
			.subscribe(() => {
				this.fetchTasksForGrid();
			});
	}

	fetchApplicationsForGrid() {
		this.userService.getAssignedApplications()
			.subscribe((result) => {
				this.applicationList = result.applications;
			});
	}

	fetchEventsForGrid() {
		this.userService.getAssignedEvents()
			.subscribe((result) => {
				this.eventList = result.events;
			});
	}

	fetchEventNewsForGrid() {
		this.userService.getAssignedEventNews()
			.subscribe((result) => {
				this.eventNewsList = result.eventNews;
			});
	}

	fetchTasksForGrid() {
		this.userService.getAssignedTasks()
			.subscribe((result) => {
				this.taskList = result.tasks;
				this.summaryDetail = result.summaryDetail
				for (let i = 0; i < this.taskList.length; i++) {
					this.taskList[i].parsedInstructions = this.getMarkupUrlData(this.taskList[i].instructionsLink);
				}
			});
	}

	fetchPeopleForGrid() {
		this.userService.getAssignedPeople()
			.subscribe((result) => {
				this.activePersonList = result.activePeople;
			});
	}

	launchManageStaff(id): void {
		if (id) {
			this.dialogService.extra(UserManageStaffComponent, [
				{provide: 'id', useValue: id},
				{provide: PersonModel, useValue: {}}
			], false, false).then( (result: any)  => {
				console.log(result);
			}).catch(result => {
				if (result) {
					console.error(result);
				}
			});
		}
	}

	handleApplicationClicked(event) {

		// ------------------------------------------------

		this.dialogService.open(AssetShowComponent, [
				{provide: 'ID', useValue: event.dataItem.appId},
				{provide: 'ASSET', useValue: event.dataItem.assetClass}
			], DIALOG_SIZE.LG).then(x => {
			// ---
		});

		// ------------------------------------------------

		if (event.dataItem === this.itemForDoubleClick) {
			// ---
		} else {
			this.itemForDoubleClick = event.dataItem;
			setTimeout((dataItem) => {
				if (this.itemForDoubleClick === dataItem) {
					this.itemForDoubleClick = null;
				}
			}, 500, event.dataItem);
		}
	}

	handlePersonClicked(event) {
		if (event.dataItem === this.itemForDoubleClick) {
			this.launchManageStaff(event.dataItem.personId);
		} else {
			this.itemForDoubleClick = event.dataItem;
			setTimeout((dataItem) => {
				if (this.itemForDoubleClick === dataItem) {
					this.itemForDoubleClick = null;
				}
			}, 500, event.dataItem);
		}
	}
}