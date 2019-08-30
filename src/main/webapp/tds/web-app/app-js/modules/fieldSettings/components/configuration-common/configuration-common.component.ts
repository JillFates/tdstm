import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

export class ConfigurationCommonComponent {
	constructor(
		public prompt: UIPromptService,
		public translate: TranslatePipe) {
		/* constructor */
	}

	/**
	 * Display warning message about loosing values if user moves forward
	 * @returns {Promise<boolean>}
	 */
	protected displayWarningMessage(): Promise<boolean> {
		return this.prompt.open(
			this.translate.transform('GLOBAL.CONFIRM'),
			this.translate.transform('FIELD_SETTINGS.WARNING_CHANGE_VALIDATION'),
			this.translate.transform('GLOBAL.YES'),
			this.translate.transform('GLOBAL.CANCEL'));
	}
}
