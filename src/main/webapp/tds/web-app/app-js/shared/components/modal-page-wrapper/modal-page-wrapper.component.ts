/**
 * Control to host a external page within an angular modal view
 * <tds-modal-page-wrapper title='Edit Model' url='tdstm/model/edit?id=10'></tds-modal-page-wrapper>
 */

import {Component, Inject, Input, AfterViewInit, ElementRef, ViewChild} from '@angular/core';
import { UIExtraDialog } from '../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-modal-page-wrapper',
	template: `
	<div class="modal fade in modal-page-wrapper-component"
		tds-handle-escape (escPressed)="cancelCloseDialog()"
		id="modal-page-wrapper" data-backdrop="static" tabindex="-1" role="dialog">
		<div class="modal-dialog modal-lg" role="document">
			<div class="tds-modal-content" [ngStyle]="{'visibility': isVisible ? 'visible' : 'hidden'}">
				<div class="modal-header">
					<button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
						<clr-icon aria-hidden="true" shape="close"></clr-icon>
					</button>
					<h4 class="modal-title">{{title}}</h4>
				</div>
				<div class="modal-body">
					<div class="modal-body-container">
					<form id="wrapperForm"
						#targetForm="ngForm"
						[action]="url"
						method="post"
						target="target-frame">
						<iframe #targetFrame name="target-frame" (load)="onLoad()" class="wrapper-frame"></iframe>
					</form>
				</div>
				</div>
				<div class="modal-footer">
				</div>
			</div>
		</div>
	</div>
`
})
export class TDSModalPageWrapperComponent extends UIExtraDialog implements AfterViewInit {
	public isVisible = false;
	private hasBeenSubmited = false;
	@ViewChild('targetFrame', {static: false}) targetFrame: ElementRef;
	constructor(
		@Inject('title') public title: string,
		@Inject('url') public url: string,
		) {
		super('#modal-page-wrapper');
	}

	/**
	 * On load show the view
	 */
	onLoad() {
		if (this.hasBeenSubmited) {
			this.isVisible = true;
		}
	}

	/**
	 * After view init submit inmediatly the form
	 */
	ngAfterViewInit() {
		document.getElementById('wrapperForm')['submit']();
		this.hasBeenSubmited = true;
	}

	/**
	 * On close the view
	 */
	public cancelCloseDialog() {
		this.close(null);
	}
}
