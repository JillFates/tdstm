// Angular
import {Component} from '@angular/core';
// NGXS
import {SetProject} from '../../actions/project.actions';
import {Store} from '@ngxs/store';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {PostNotices} from '../../../auth/action/notice.actions';

// Model
enum ProjectType {
	ACTIVE = 'active',
	COMPLETED = 'completed'
};

@Component({
	selector: 'tds-select-project-modal',
	templateUrl: 'select-project-modal.component.html'
})
export class SelectProjectModalComponent {
	public selectedProjectId: any = null;
	public projectType = ProjectType;
	public selectedProjectStatus = this.projectType.ACTIVE;

	constructor(
		private projects: any[],
		private store: Store,
		protected activeDialog: UIActiveDialogService,
		protected preferenceService: PreferenceService,
		protected userContextService: UserContextService) {
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
					this.activeDialog.close({success: true});
				});
			});
		});
	}

	/**
	 * if user doesn't want to continue with the current login info
	 */
	public onCancel(): void {
		this.activeDialog.close({success: false});
	}

	/**
	 * Changes Project Status
	 */
	public changeProjectStatus(): void {
		this.selectedProjectStatus = (this.selectedProjectStatus === this.projectType.ACTIVE) ? this.projectType.COMPLETED : this.projectType.ACTIVE;
	}
}
