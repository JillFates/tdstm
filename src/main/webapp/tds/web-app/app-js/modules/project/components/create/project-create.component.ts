import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {ProjectService} from '../../service/project.service';
import {ProjectModel} from '../../model/project.model';
import {Router} from '@angular/router';
import {UIActiveDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `project-create`,
	templateUrl: 'project-create.component.html',
})
export class ProjectCreateComponent implements OnInit {
	public managers;
	public workflowCodes;
	public planMethodologies;
	public clients;
	public partners;
	public orderNums = Array(25).fill(0).map((x, i) => i + 1);
	public projectModel: ProjectModel = null;
	private defaultModel = null;

	constructor(
		private projectService: ProjectService,
		private promptService: UIPromptService,
		private activeDialog: UIActiveDialogService) {
	}

	ngOnInit() {
		this.getModel();
		this.projectModel = new ProjectModel();
		this.defaultModel = {
			clientId: 0,
			projectName: '',
			description: '',
			startDate: new Date(),
			completionDate: new Date(),
			partnerIds: [],
			projectManagerId: 0,
			workflowCode: 'STD_PROCESS',
			projectCode: '',
			projectType: 'Standard',
			comment: '',
			defaultBundle: 'TBD',
			timeZone: '',
			collectReportingMetrics: true,
			planMethodology: 'Migration Method'
		};
		this.projectModel = Object.assign({}, this.defaultModel, this.projectModel);
	}

	private getModel() {
		this.projectService.getModelForProjectCreate().subscribe((result: any) => {
			let data = result.data;
			this.managers = data.managers;
			this.managers = data.managers.filter((item, index) => index === 0 || item.name !== data.managers[index - 1].name); // Filter duplicate names
			this.workflowCodes = data.workflowCodes;
			this.clients = data.clients;
			this.partners = data.partners;
			this.planMethodologies = data.planMethodologies;
		});
	}

	public saveForm() {
		this.projectService.saveProject(this.projectModel).subscribe((result: any) => {
			if (result.status === 'success') {
				this.activeDialog.close();
			}
		});
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.projectModel) !== JSON.stringify(this.defaultModel)) {
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