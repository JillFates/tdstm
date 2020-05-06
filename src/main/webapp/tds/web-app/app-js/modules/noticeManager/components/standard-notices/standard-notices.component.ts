// Angular
import {Component, Input, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
// Service
import {UserContextService} from '../../../auth/service/user-context.service';
// Model
import {NoticeModel} from '../../model/notice.model';
import {NoticeCommonComponent} from './../notice-common'
import {PostNoticesManagerService} from '../../../auth/service/post-notices-manager.service';
declare var jQuery: any;

@Component({
	selector: 'tds-standard-notices',
	templateUrl: 'standard-notices.component.html'
})
export class StandardNoticesComponent extends NoticeCommonComponent implements OnInit {
	@Input() data: any;
	private notices: NoticeModel[];

	constructor(
		protected userContextService: UserContextService,
		private postNoticesManager: PostNoticesManagerService,
		protected sanitizer: DomSanitizer) {
			super(sanitizer);
	}

	ngOnInit() {
		this.notices = this.data.notices;
		jQuery('.close').hide();
	}

	/**
	 * Set the flag to dont show the notice any more
	*/
	onAccept() {
		const updates = this.notices
			.filter((notice) => notice.notShowAgain)
			.map((notice) => this.postNoticesManager.setAcknowledge(notice.id));

		if (updates.length) {
			Observable.forkJoin(updates)
				.subscribe((results) => this.onCancelClose());
		} else {
			this.onCancelClose();
		}
	}

	onCancel() {
		// Disabled for this component
	}

	onDismiss() {
		// Disabled for this component
	}
}
