// Angular
import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {tap} from 'rxjs/operators';
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
import {
	ActivePersonColumnModel,
	ApplicationColumnModel,
	EventColumnModel,
	EventNewsColumnModel,
	TaskColumnModel,
} from '../../model/user-dashboard-columns.model';
import { COLUMN_MIN_WIDTH } from '../../../dataScript/model/data-script.model';
import { ModalType} from '../../../../shared/model/constants';
import { GridComponent } from '@progress/kendo-angular-grid';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { Store } from '@ngxs/store';
import {SetProject} from '../../../project/actions/project.actions';
import {SetEvent} from '../../../event/action/event.actions';
import {TaskEditCreateModelHelper} from '../../../taskManager/components/common/task-edit-create-model.helper';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DialogService, ModalSize} from 'tds-component-library';
import {AssetExplorerModule} from '../../../assetExplorer/asset-explorer.module';
import {TaskEditCreateComponent} from '../../../taskManager/components/edit-create/task-edit-create.component';

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
	public selectedEvent = null;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private userService: UserService,
		private taskService: TaskService,
		private dialogService: DialogService,
		private notifierService: NotifierService,
		private translate: TranslatePipe,
		private route: ActivatedRoute,
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

		this.applicationColumnModel = new ApplicationColumnModel();
		this.activePersonColumnModel = new ActivePersonColumnModel();
		this.eventColumnModel = new EventColumnModel();
		this.eventNewsColumnModel = new EventNewsColumnModel();
		this.taskColumnModel = new TaskColumnModel();

		this.populateData()
			.subscribe((result) => {
				this.selectProjectByID(this.projectInstance && this.projectInstance.id || null);
			})
	}

	public onChangeProject($event ?: any): void {
		const projectId = $event.id;

		this.selectProjectByID(projectId);
		this.populateData()
			.subscribe((result) => {
				console.log(result);
				const payload = {
					id: this.projectInstance.id,
					name: this.projectInstance.name,
					logoUrl: this.projectLogoId ? '/tdstm/project/showImage/' + this.projectLogoId : ''
				};
				this.store.dispatch(new SetProject(payload));
				setTimeout(() => this.updateEvent(), 500);
			});
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

	private populateData(): any {
		return this.userService
			.fetchModelForUserDashboard(this.selectedProject ? this.selectedProject.id : '')
			.pipe(
				tap((result) => {
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
				})
			);
	}

	/**
	 * Passing and event id search for it in the event list, on not found it returns
	 * the first list element whenever the list has elements, otherwise it returns null
 	 * @param {number} id  Event id
	 * @returns {any} Event found otherwhise null
	*/
	private getDefaultEvent(id: string): any {
		// event ids are integer so we need to cast accordly
		const selectedId = id ? parseInt(id, 10) : null;

		if (selectedId) {
			return this.eventList.find((event) => event.eventId === selectedId) || null;
		} else if (this.eventList.length)  {
			return this.eventList[0];
		}
		return null;
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
		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: TaskDetailComponent,
			data: {
				taskDetailModel: taskDetailModel
			},
			modalConfiguration: {
				title: 'Task Detail',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-task-modal-edit-view-create'
			}
		}).subscribe((data: any) => {
			if (data && data.shouldOpenTask) {
				this.openTaskDetailView(data.commentInstance)
			} else if (data && data.shouldEdit) {
				this.openTaskEditView(data.id);
			} else {
				this.fetchTasksForGrid();
			}
		});
	}

	public openTaskEditView(taskRow: any): void {
		let taskDetailModel: TaskDetailModel = new TaskDetailModel();
		this.taskService.getTaskDetails(taskRow.id)
			.subscribe((res) => {
				let modelHelper = new TaskEditCreateModelHelper(
					this.userContext.timezone,
					this.userContext.dateFormat,
					this.taskService,
					this.dialogService,
					this.translate,
					this.componentFactoryResolver);
				taskDetailModel.detail = res;
				taskDetailModel.modal = {
					title: 'Task Edit',
					type: ModalType.EDIT
				};
				let model = modelHelper.getModelForDetails(taskDetailModel);
				model.instructionLink = modelHelper.getInstructionsLink(taskDetailModel.detail);
				model.durationText = DateUtils.formatDuration(model.duration, model.durationScale);
				model.modal = taskDetailModel.modal;

				this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: TaskEditCreateComponent,
					data: {
						taskDetailModel: model
					},
					modalConfiguration: {
						title: 'Task Edit',
						draggable: true,
						modalSize: ModalSize.CUSTOM,
						modalCustomClass: 'custom-task-modal-edit-view-create'
					}
				}).subscribe((data: any) => {
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

	updateEvent() {
		let selectedEventId = null;

		if (this.userContext && this.userContext.event) {
			selectedEventId = this.userContext.event.id;
		}
		this.selectedEvent = this.getDefaultEvent(this.route.snapshot.queryParams['moveEvent'] || selectedEventId);
		if (this.selectedEvent) {
			// const payload = {
			// 	id: this.selectedEvent.eventId,
			// 	name: this.selectedEvent.name
			// };
			this.store.dispatch(new SetEvent({id: this.selectedEvent.eventId, name: this.selectedEvent.name}));
			// this.store.dispatch(new SetEvent(payload));
		}
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

	/**
	 * Open the User Management Staff Component
	 * @param personModelId
	 */
	private launchManageStaff(personId: number): void {
		if (personId) {
			this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: UserManageStaffComponent,
				data: {
					personId: personId
				},
				modalConfiguration: {
					title: 'Manage Staff',
					draggable: true,
					modalSize: ModalSize.CUSTOM,
					modalCustomClass: 'custom-user-manage-dialog'
				}
			}).subscribe();
		}
	}

	public handleApplicationClicked(event): void {
		this.openAssetDialog(event.dataItem.appId, event.dataItem.assetClass);
	}

	public openAssetDialog(id, assetClass): void {

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetShowComponent,
			data: {
				assetId: id,
				assetClass: assetClass,
				assetExplorerModule: AssetExplorerModule
			},
			modalConfiguration: {
				title: 'Asset',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	public handlePersonClicked(event): void {
		this.launchManageStaff(event.dataItem.personId);
	}
}
