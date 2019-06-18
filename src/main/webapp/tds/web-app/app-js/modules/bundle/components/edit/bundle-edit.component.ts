import {Component, ElementRef, OnInit, Renderer2, ViewChild} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';
import {ActivatedRoute, Router} from '@angular/router';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `bundle-edit`,
	templateUrl: 'bundle-edit.component.html',
})
export class BundleEditComponent implements OnInit {
	public managers;
	public workflowCodes;
	public rooms;
	protected userTimeZone: string;
	private bundleId;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public typeOptions = [{value: 'L', text: 'Linear'}, {value: 'M', text: 'Manual'}];
	public bundleModel: BundleModel = null;
	public savedModel: BundleModel = null;
	public dashboardSteps;
	@ViewChild('startTimePicker') startTimePicker;
	@ViewChild('completionTimePicker') completionTimePicker;

	constructor(
		private bundleService: BundleService,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService,
		private route: ActivatedRoute,
		private router: Router) {
		this.bundleId = this.route.params['_value']['id'];
	}

	ngOnInit() {
		this.getModel();
		this.bundleModel = new BundleModel();
		const defaultBundle = {
			name: '',
			description: '',
			fromId: 0,
			toId: 0,
			startTime: '',
			completionTime: '',
			projectManagerId: 0,
			moveManagerId: 0,
			operationalOrder: 1,
			workflowCode: 'STD_PROCESS',
			useForPlanning: false,
		};
		this.userTimeZone = this.preferenceService.getUserTimeZone();
		this.bundleModel = Object.assign({}, defaultBundle, this.bundleModel);
	}

	private getModel() {
		this.bundleService.getModelForBundleEdit(this.bundleId).subscribe((result: any) => {
			console.log(result);
			let data = result.data;
			let bundleModel = this.bundleModel;
			// Fill the model based on the current person.
			Object.keys(data.moveBundleInstance).forEach(function (key) {
				if (key in bundleModel && data.moveBundleInstance[key]) {
					bundleModel[key] = data.moveBundleInstance[key];
				}
			});
			this.bundleModel = bundleModel;
			if (this.bundleModel.startTime) {
				this.startTimePicker.dateValue = this.formatForDateTimePicker(this.bundleModel.startTime);
			}
			if (this.bundleModel.completionTime) {
				this.completionTimePicker.dateValue = this.formatForDateTimePicker(this.bundleModel.completionTime);
			}
			this.bundleModel.projectManagerId = data.projectManager ? data.projectManager : 0;
			this.bundleModel.moveManagerId = data.moveManager ? data.moveManager : 0;

			this.savedModel = JSON.parse(JSON.stringify(this.bundleModel));
			this.dashboardSteps = data.dashboardSteps;
			for (let i = 0; i < this.dashboardSteps.length; i++) {
				this.dashboardSteps[i].dashboard = this.dashboardSteps[i].moveBundleStep ? true : false;
			}
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.workflowCodes = data.workflowCodes;
			this.rooms = data.rooms;
		});
	}

	public formatForDateTimePicker (time) {
		let localDateFormatted = DateUtils.convertFromGMT(time, this.userTimeZone);
		return time ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	public saveForm() {
		this.bundleService.updateBundle(this.bundleId, this.bundleModel, this.dashboardSteps).subscribe((result: any) => {
			if (result.status === 'success') {
				this.router.navigateByUrl('bundle/' + this.bundleId + '/show')
			}
		});
	}

	public cancelEdit() {
			if (JSON.stringify(this.bundleModel) !== JSON.stringify(this.savedModel)) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.router.navigateByUrl('bundle/' + this.bundleId + '/show')
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.router.navigateByUrl('bundle/' + this.bundleId + '/show')
		}
	}

	public onChangeDashboardStep(step) {
		if (step.dashboard) {
			step.moveBundleStep = {
				planStartTime: this.bundleModel.startTime,
				planCompletionTime: this.bundleModel.completionTime,
				calcMethod: 'L',
				showInGreen: false
			};
		}
	}
}