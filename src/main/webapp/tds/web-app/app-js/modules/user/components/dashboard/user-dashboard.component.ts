import {Component, OnInit} from '@angular/core';
import {UserService} from '../../service/user.service';
import {
	ActivePersonColumnModel,
	ApplicationColumnModel,
	EventColumnModel, EventNewsColumnModel, TaskColumnModel
} from '../../model/user-dashboard-columns.model';
import {COLUMN_MIN_WIDTH} from '../../../dataScript/model/data-script.model';
import {TaskService} from '../../../taskManager/service/task.service';
import {ModalType} from '../../../../shared/model/constants';
import {AssetCommentModel} from '../../../assetComment/model/asset-comment.model';
import {AssetCommentViewEditComponent} from '../../../assetComment/components/view-edit/asset-comment-view-edit.component';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from '../../../taskManager/model/task-detail.model';
import {TaskDetailComponent} from '../../../taskManager/components/detail/task-detail.component';

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
		if(url) {
			let data = url.split("|");
			if(data.length == 1) {
				data[1] = data[0];
				data[0] = 'Instructions...';
			}
			return data;
		}
		return null;
	}

	changeTimeEst(id,days) {
		this.taskService.changeTimeEst(id,days)
			.subscribe((result) => {
				if(result) {
					this.fetchTasksForGrid();
				}
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
		]).then(result => {
			this.fetchTasksForGrid();
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	updateTaskStatus(id,status) {
		this.taskService.updateStatus(id,status)
			.subscribe((result) => {
				if(result) {
					this.fetchTasksForGrid();
				}
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
				for(let i=0; i < this.taskList.length; i++) {
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
}