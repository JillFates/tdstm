// Angular
import {Component} from '@angular/core';
// Service
import {UserContextService} from '../../../../../modules/security/services/user-context.service';
import {UserContextModel} from '../../../../../modules/security/model/user-context.model';

@Component({
	selector: 'tds-header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/components/header/header.component.html',
})

export class HeaderComponent {

	protected userContext: UserContextModel;

	constructor(private userContextService: UserContextService) {
		this.getUserContext();
	}

	protected getUserContext(): void {
		this.userContextService.getUserContext().subscribe( (userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}
}