// Angular
import {Component, OnInit} from '@angular/core';
import { DomSanitizer} from '@angular/platform-browser';
import {Observable} from 'rxjs';
// Service
import {UIActiveDialogService, } from '../../../shared/services/ui-dialog.service';
// Model
import {NoticeModel, StandardNotices} from '../model/notice.model';

export abstract class NoticeCommonComponent {
	constructor(
		protected activeDialog: UIActiveDialogService,
		protected sanitizer: DomSanitizer) {
	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	// TODO move to base
	protected onCancel() {
		this.activeDialog.dismiss();
	}

	// TODO move to base
	protected sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	abstract  onAccept(): void;

}
