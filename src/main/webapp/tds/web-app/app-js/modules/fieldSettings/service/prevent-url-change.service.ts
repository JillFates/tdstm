import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/fromPromise';
import 'rxjs/add/operator/switchMap';
import {UIPromptService} from '../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';
import {NotifierService} from '../../../shared/services/notifier.service';
@Injectable()
export class  PreventUrlChangeService implements CanDeactivate<ISavable> {
	constructor(
		private prompt: UIPromptService,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService) {
	}

	/**
	 * This guard it's used for preventing the user to go to another page if that work
	 * that it's being done is not saved yet.
	 */
	async canDeactivate(
		component: ISavable,
		currentRoute: ActivatedRouteSnapshot,
		currentState: RouterStateSnapshot,
		nextState?: RouterStateSnapshot): Promise<boolean> {
			this.disableGlobalAnimation(true);
			if (localStorage.getItem('formDirty') === 'true') {
				return this.prompt.open(
					this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
					this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
					this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL'),
				);
			} else {
				return true;
			}
	}

	/**
	 * To disable Global animation that is being manually represent by the Progress bar
	 * @param disabled
	 */
	private disableGlobalAnimation(disabled: boolean): void {
		this.notifierService.broadcast({
			name: 'stopLoader',
		});
	}
}

export interface ISavable {
	saving$: Observable<boolean>;
	saving: boolean;
	shouldSave$: Observable<boolean>;
	shouldSave: boolean;
}