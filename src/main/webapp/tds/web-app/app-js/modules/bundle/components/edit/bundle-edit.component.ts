import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';
import {ActivatedRoute} from '@angular/router';

@Component({
	selector: `bundle-edit`,
	templateUrl: 'bundle-edit.component.html',
})
export class BundleEditComponent implements OnInit {
	public managers;
	public workflowCodes;
	public rooms;
	private bundleId;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public typeOptions = [{value: 'L', text: 'Linear'}, {value: 'M', text: 'Manual'}];
	public bundleModel = null;
	public dashboardSteps;

	constructor(
		private bundleService: BundleService,
		private route: ActivatedRoute) {
		this.bundleId = this.route.params['_value']['id'];
	}

	ngOnInit() {
		this.getModel();
		this.bundleModel = new BundleModel();
		const defaultBundle = {
			name: '',
			description: '',
			sourceRoom: {},
			targetRoom: {},
			startTime: '',
			completionTime: '',
			projectManagerId: 0,
			moveManagerId: 0,
			operationalOrder: 1,
			workflowCode: 'STD_PROCESS',
			useForPlanning: false,
		};
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
			this.bundleModel.projectManagerId = data.projectManager ? data.projectManager : 0;
			this.bundleModel.moveManagerId = data.moveManager ? data.moveManager : 0;
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

	public saveForm() {
		console.log(this.bundleModel);
		this.bundleModel.dashboardSteps = this.dashboardSteps;
		this.bundleService.updateBundle(this.bundleId, this.bundleModel).subscribe((result: any) => {
			console.log(result);
		});
	}
}