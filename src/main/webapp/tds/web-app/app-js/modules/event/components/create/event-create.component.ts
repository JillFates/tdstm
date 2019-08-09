import {Component, OnInit} from '@angular/core';
import {EventsService} from '../../service/events.service';
import {EventModel} from '../../model/event.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: `event-create`,
	templateUrl: 'event-create.component.html',
})
export class EventCreateComponent implements OnInit {
	public bundles: any[] = [];
	public runbookStatuses: string[] = [];
	public assetTags: any[] = [];
	public eventModel: EventModel = null;
	private defaultModel = null;

	constructor(
		private eventsService: EventsService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private activeDialog: UIActiveDialogService) {
	}

	ngOnInit() {
		this.getModel();
		this.eventModel = new EventModel();
		this.defaultModel = {
			name: '',
			description: '',
			tagIds: [],
			moveBundle: [],
			runbookStatus: '',
			runbookBridge1: '',
			runbookBridge2: '',
			videolink: '',
			estStartTime: '',
			estCompletionTime: '',
			apiActionBypass: false
		};
		this.eventModel = Object.assign({}, this.defaultModel, this.eventModel);
	}

	private getModel() {
		this.eventsService.getModelForEventCreate().subscribe((result: any) => {
			this.bundles = result.bundles;
			this.runbookStatuses = result.runbookStatuses;
			this.assetTags = result.tags;
		});
	}

	public onAssetTagChange(event) {
		this.eventModel.tagIds = event.tags;
	}

	public saveForm() {
		if (DateUtils.validateDateRange(this.eventModel.estStartTime, this.eventModel.estCompletionTime) && this.validateRequiredFields(this.eventModel)) {
			this.eventsService.saveEvent(this.eventModel).subscribe((result: any) => {
				if (result.status === 'success') {
					this.activeDialog.close();
				}
			});
		}
	}

	private validateRequiredFields(model: EventModel): boolean {
		if (!model.name) {
			alert('Field "Name" is required.');
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.defaultModel)) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CANCEL'),
			)
				.then(confirm => {
					if (confirm) {
						this.activeDialog.close();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.close();
		}
	}
}