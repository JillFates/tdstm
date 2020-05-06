// Angular
import {Component, Input, OnInit} from '@angular/core';
// NGXS
import {SetProject} from '../../actions/project.actions';
import {Store} from '@ngxs/store';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {PostNotices} from '../../../auth/action/notice.actions';
import {Dialog} from 'tds-component-library';

// Model
enum ProjectType {
	ACTIVE = 'active',
	COMPLETED = 'completed'
};

@Component({
	selector: 'tds-select-project-modal',
	templateUrl: 'select-project-modal.component.html'
})
export class SelectProjectModalComponent extends Dialog implements OnInit {
	@Input() data: any;
	public selectedProjectId: any = null;
	public projectType = ProjectType;
	public selectedProjectStatus = this.projectType.ACTIVE;
	private projects: any[];

	constructor(
		private store: Store,
		protected preferenceService: PreferenceService,
		protected userContextService: UserContextService) {
		super();
	}

	ngOnInit() {
		this.projects = this.data.projects;
	}

	/**
	 * Set the new Project Selected
	 */
	public onContinue(): void {
		const selectedId = parseInt(this.selectedProjectId, 0);
		const project = this.projects.find((project: any) => {
			return project.id === selectedId;
		});

		// Set the preference at the Server Side
		this.preferenceService.setPreference(PREFERENCES_LIST.CURR_PROJ, `${project.id}`).subscribe(() => {
			// Set New Select Project at the user level
			this.store.dispatch(
				new SetProject({
					id: project.id,
					name: project.name,
					logoUrl: project.logoUrl
				})).subscribe(() => {
				this.store.dispatch(new PostNotices()).subscribe(() => {
					this.onCancel();
				});
			});
		});
	}

	/**
	 * if user doesn't want to continue with the current login info
	 */
	public onCancel(): void {
		this.onCancelClose();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.onCancelClose();
	}

	/**
	 * Changes Project Status
	 */
	public changeProjectStatus(): void {
		this.selectedProjectStatus = (this.selectedProjectStatus === this.projectType.ACTIVE) ? this.projectType.COMPLETED : this.projectType.ACTIVE;
	}
}
