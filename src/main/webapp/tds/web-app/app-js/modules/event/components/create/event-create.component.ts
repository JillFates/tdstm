import {Component, OnInit} from '@angular/core';
import {EventService} from '../../service/event.service';
import {EventModel} from '../../model/event.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

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
		private eventService: EventService,
		private promptService: UIPromptService,
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
		this.eventService.getModelForEventCreate().subscribe((result: any) => {
			this.bundles = result.bundles;
			this.runbookStatuses = result.runbookStatuses;
			this.assetTags = result.tags;
		});
	}

	public onAssetTagChange(event) {
		this.eventModel.tagIds = event.tags;
	}

	public saveForm() {
		this.eventService.saveEvent(this.eventModel).subscribe((result: any) => {
			if (result.status === 'success') {
				this.activeDialog.close();
			}
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.eventModel) !== JSON.stringify(this.defaultModel)) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
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