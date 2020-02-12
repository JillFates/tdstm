// Angular
import { Component, OnInit, ViewChild } from '@angular/core';
// Services
import { TaskService } from '../../../taskManager/service/task.service';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { UserService } from '../../service/user.service';
import { UserContextService } from '../../../auth/service/user-context.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
// Components
import { TaskDetailComponent } from '../../../taskManager/components/detail/task-detail.component';
import { UserManageStaffComponent } from '../../../../shared/modules/header/components/manage-staff/user-manage-staff.component';
import { AssetShowComponent } from '../../../assetExplorer/components/asset/asset-show.component';
// Model
import { TaskDetailModel } from '../../../taskManager/model/task-detail.model';
import { PersonModel } from '../../../../shared/components/add-person/model/person.model';
import {
	ActivePersonColumnModel,
	ApplicationColumnModel,
	EventColumnModel,
	EventNewsColumnModel,
	TaskColumnModel,
} from '../../model/user-dashboard-columns.model';
import { COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { DIALOG_SIZE , ModalType} from '../../../../shared/model/constants';
import { GridComponent } from '@progress/kendo-angular-grid';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { Store } from '@ngxs/store';
import {SetProject} from '../../../project/actions/project.actions';
import {TaskEditCreateModelHelper} from '../../../taskManager/components/common/task-edit-create-model.helper';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TaskEditCreateComponent} from '../../../taskManager/components/edit-create/task-edit-create.component';
import {clone} from 'ramda';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'user-dashboard',
	templateUrl: 'user-dashboard.component.html'
})
export class UserDashboardComponent implements OnInit {
	public currentPerson;
	public selectedProjectID;
	public selectedProject;
	public projectInstance;
	public projectLogoId;
	public movedayCategories;
	public projectList;
	public applicationList;
	public applicationColumnModel;
	public activePersonList;
	public activePersonColumnModel;
	public eventList;
	public showActiveEvents = true;
	public eventColumnModel;
	public eventNewsList;
	public eventNewsColumnModel;
	public taskList;
	public taskColumnModel;
	public summaryDetail;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public items: any[] = [
		{
			text: 'Sample Box',
		},
	];
	public userContext: UserContextModel;
	@ViewChild('taskGrid', { static: false }) taskGrid: GridComponent;

	constructor(
		private userService: UserService,
		private taskService: TaskService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private translate: TranslatePipe,
		private store: Store) {
		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	ngOnInit() {
		this.notifierService.broadcast({
			name: 'notificationHeaderTitleChange',
			title: 'User Dashboard for ' + this.userContext.person.fullName
		});

		this.populateData();
	}

	public onChangeProject($event ?: any): void {
		this.selectProjectByID($event.id);
		this.populateData();
	}

	public selectProjectByID(id): any {
		// Please disregard linting on the following line, comparison should be '==' (See TM-16490)
		const project = this.projectList.find(p => id == p.id); // tslint:disable-line
		if (project) {
			this.selectedProject = project;
			return project;
		}
		return null;
	}

	private populateData(): void {
		this.userService
			.fetchModelForUserDashboard(this.selectedProject ? this.selectedProject.id : '')
			.subscribe(result => {
				this.fetchApplicationsForGrid();
				this.fetchEventNewsForGrid();
				this.fetchEventsForGrid();
				this.fetchPeopleForGrid();
				this.fetchTasksForGrid();
				this.projectList = result.projects;
				this.currentPerson = result.person;
				this.movedayCategories = result.movedayCategories;
				this.projectInstance = result.projectInstance;
				this.projectLogoId = result.projectLogoId;
				this.selectedProject = this.projectInstance;
				this.selectedProjectID = this.projectInstance.id;
				this.store.dispatch(new SetProject({id: this.projectInstance.id, name: this.projectInstance.name, logoUrl: this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId : ''}));
			});
		this.applicationColumnModel = new ApplicationColumnModel();
		this.activePersonColumnModel = new ActivePersonColumnModel();
		this.eventColumnModel = new EventColumnModel();
		this.eventNewsColumnModel = new EventNewsColumnModel();
		this.taskColumnModel = new TaskColumnModel();
	}

	public openLinkInNewTab(url): void {
		window.open(url, '_blank');
	}

	public getMarkupUrlData(url): any {
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
		this.taskService.changeTimeEst(id, days).subscribe(() => {
			this.fetchTasksForGrid();
		});
	}

	public openTaskDetailView(comment: any): void {
		const currentUserId = (
			this.userContext &&
			this.userContext.person &&
			this.userContext.person
		) ? this.userContext.person.id : null ;
		let taskDetailModel: TaskDetailModel = {
			id: comment.taskId,
			modal: {
				title: 'Task Detail'
			},
			detail: {
				currentUserId
			}
		};
		this.dialogService.extra(TaskDetailComponent, [
			{provide: TaskDetailModel, useValue: taskDetailModel}
		]).then((result) => {
			if (result && result.shouldOpenTask) {
				this.openTaskDetailView(result.commentInstance)
			} else if (result && result.shouldEdit) {
				this.openTaskEditView(result.id);
			} else {
				this.fetchTasksForGrid();
			}

		}).catch(result => {
			if (!result) {
				this.fetchTasksForGrid();
			}
		});
	}

	public openTaskEditView(comment: any): void {
		let taskDetailModel: TaskDetailModel = new TaskDetailModel();
		this.taskService.getTaskDetails(comment.id)
			.subscribe((res) => {
				let modelHelper = new TaskEditCreateModelHelper(
					this.userContext.timezone,
					this.userContext.dateFormat,
					this.taskService,
					this.dialogService,
					this.translate);
				taskDetailModel.detail = res;
				taskDetailModel.modal = {
					title: 'Task Edit',
					type: ModalType.EDIT
				};
				let model = modelHelper.getModelForDetails(taskDetailModel);
				model.instructionLink = modelHelper.getInstructionsLink(taskDetailModel.detail);
				model.durationText = DateUtils.formatDuration(model.duration, model.durationScale);
				model.modal = taskDetailModel.modal;
				this.dialogService.extra(TaskEditCreateComponent, [
					{ provide: TaskDetailModel, useValue: clone(model) }
				], false, false)
					.then(result => {
						this.fetchTasksForGrid();
					}).catch(result => {
					this.fetchTasksForGrid();
				});
			});
	}

	public updateTaskStatus(id, status): void {
		this.taskService.updateStatus(id, status).subscribe(() => {
			this.fetchTasksForGrid();
		});
	}

	public fetchApplicationsForGrid(): void {
		this.userService.getAssignedApplications().subscribe(result => {
			this.applicationList = result.applications;
		});
	}

	public fetchEventsForGrid(): void {
		let projectId = this.selectedProject ? this.selectedProject.id : 0;
		this.userService
			.getAssignedEvents(projectId, this.showActiveEvents)
			.subscribe(result => {
				this.eventList = result.events;
			});
	}

	public fetchEventNewsForGrid(): void {
		this.userService.getAssignedEventNews().subscribe(result => {
			this.eventNewsList = result.eventNews;
		});
	}

	public fetchTasksForGrid(): void {
		if (this.taskList) {
			for (let i = 0; i < this.taskList.length; i++) {
				this.taskGrid.collapseRow(i);
			}
		}
		this.userService.getAssignedTasks().subscribe(result => {
			this.taskList = result.tasks;
			this.summaryDetail =
				result.summaryDetail === 'No active tasks were found.'
					? ''
					: result.summaryDetail;
			for (let i = 0; i < this.taskList.length; i++) {
				this.taskList[i].parsedInstructions = this.getMarkupUrlData(
					this.taskList[i].instructionsLink
				);
			}
		});
	}

	public fetchPeopleForGrid(): void {
		this.userService.getAssignedPeople().subscribe(result => {
			this.activePersonList = result.activePeople;
		});
	}

	private launchManageStaff(id): void {
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

	public handleApplicationClicked(event): void {
		this.openAssetDialog(event.dataItem.appId, event.dataItem.assetClass);
	}

	public openAssetDialog(id, assetClass): void {
		this.dialogService.open(AssetShowComponent, [
			{provide: 'ID', useValue: id},
			{provide: 'ASSET', useValue: assetClass}
		], DIALOG_SIZE.XXL);
	}

	public handlePersonClicked(event): void {
		this.launchManageStaff(event.dataItem.personId);
	}
}
