import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {BundleModel} from '../../model/bundle.model';
import {Router} from '@angular/router';

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

	constructor(
		private bundleService: BundleService,
		private router: Router) {
	}

	ngOnInit() {
		this.getModel();
		this.bundleModel = new BundleModel();
	}

	private getModel() {
		this.bundleService.getModelForBundleCreate().subscribe((result: any) => {
			let data = result.data;
			this.bundleModel.operationalOrder = 1;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.workflowCodes = data.workflowCodes;
			this.rooms = data.rooms;
		});
	}

	public saveForm() {
		this.bundleService.saveBundle(this.bundleModel).subscribe((result: any) => {
			if (result.status === 'success') {
				this.router.navigateByUrl('bundle/' + result.data.id + '/show');
			}
		});
	}
}