import {Component, ViewChild, ElementRef} from '@angular/core';

import { UIExtraDialog} from '../../../shared/services/ui-dialog.service';
import { PersonModel } from './model/person.model';

@Component({
	selector: 'add-person',
	templateUrl: '../tds/web-app/app-js/shared/components/add-person/add-person.component.html'
})
export class AddPersonComponent extends UIExtraDialog {
	yesNoList = ['Y', 'N'];
	teams: any[] = [];
	constructor(
		public personModel: PersonModel) {
		super('#add-person-component');
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	addTeam(): void {
		const team = {id: null, data: [...this.personModel.teams]};
		this.teams.push(team);
	}

	removeTeam(index: number): void {
		this.teams.splice(index, 1);
	}

	onTeamSelected(id: string, index: number): void {
		console.log(id);
		this.teams[index].id = id;
	}

}