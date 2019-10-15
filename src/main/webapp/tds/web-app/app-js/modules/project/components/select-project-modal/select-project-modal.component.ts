// Angular
import {Component} from '@angular/core';
// NGXS
import {SetProject} from '../../actions/project.actions';
import {Store} from '@ngxs/store';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {LicenseInfo, PostNotices} from '../../../auth/action/login.actions';
import {withLatestFrom} from 'rxjs/operators';
import {forkJoin} from 'rxjs';

// Model

@Component({
	selector: 'tds-select-project-modal',
	templateUrl: 'select-project-modal.component.html'
})
export class SelectProjectModalComponent {
	public selectedProjectId: any = null;

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
		// Set New Select Project at the user level
		this.store.dispatch(new SetProject({
			id: project.id,
			name: project.name,
			logoUrl: project.logoUrl
		}));

		// Set the preference at the Server Side
		this.preferenceService.setPreference(PREFERENCES_LIST.CURR_PROJ, `${project.id}`).subscribe(() => {
			// Get again the License Info and the Post Notices
			this.store.dispatch([new PostNotices(), new LicenseInfo()]).subscribe(() => {
				this.activeDialog.close({success: true});
			});
		});
	}

	/**
	 * if user doesn't want to continue with the current login info
	 */
	public onCancel(): void {
		this.activeDialog.close({success: false});
	}

}
