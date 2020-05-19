// Angular
import {Component, Input, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
// Service
import {UserContextService} from '../../../auth/service/user-context.service';
// Model
import {NoticeModel} from '../../model/notice.model';
import {NoticeCommonComponent} from './../notice-common'
import {PostNoticesManagerService} from '../../../auth/service/post-notices-manager.service';

@Component({
	selector: 'tds-mandatory-notices',
	templateUrl: 'mandatory-notices.component.html'
})
export class MandatoryNoticesComponent extends NoticeCommonComponent implements OnInit {
	@Input() data: any;
	private notices: NoticeModel[];
	private currentNoticeIndex: number;

	constructor(
		protected userContextService: UserContextService,
		private postNoticesManager: PostNoticesManagerService,
		protected sanitizer: DomSanitizer) {
			super(sanitizer);
	}

	ngOnInit() {
		this.currentNoticeIndex = 0;
		this.userContextService.getUserContext()
		.subscribe((context) => {
			this.notices = this.data.notices.filter((notice) => notice.needAcknowledgement);
		});
	}

	/**
	 * Get the notice which the current index is pointing out
	*/
	getCurrentNotice() {
		return this.notices[this.currentNoticeIndex];
	}

	/**
	 * On cancel dismiss the active dialog
	*/
	onCancel() {
		this.onCancelClose();
	}

	/**
	 * On accept call the endpoint to marck the notice to not be shown again
	*/
	onAccept() {
		this.postNoticesManager
			.setAcknowledge(this.notices[this.currentNoticeIndex].id)
				.subscribe(() => {
					if ((this.currentNoticeIndex + 1) >= this.notices.length) {
						this.onAcceptSuccess();
					} else {
						this.currentNoticeIndex += 1;
					}
				}, (err) => console.error(err));
	}
}
