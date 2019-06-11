import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';

@Component({
	selector: `bundle-create`,
	templateUrl: 'bundle-create.component.html',
})
export class BundleCreateComponent implements OnInit {
	public managers;
	public workflowCodes;
	public rooms;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public bundleModel = null;

	constructor(private bundleService: BundleService) {
	}

	ngOnInit() {
		this.getModel();
		this.bundleModel = new BundleModel();
	}

	private getModel() {
		this.bundleService.getModelForBundleCreate().subscribe((result: any) => {
			console.log(result);
			let data = result.data;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.workflowCodes = data.workflowCodes;
			this.rooms = data.rooms;
		});
	}

	public saveForm() {
		console.log(this.bundleModel);
		this.bundleService.saveBundle(this.bundleModel).subscribe((result: any) => {
			console.log(result);
		});
	}
}