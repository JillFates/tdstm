import {Component, Inject, Input, AfterViewInit, ElementRef, ViewChild} from '@angular/core';
import { UIExtraDialog } from '../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-modal-page-wrapper',
	template: `
	<div class="modal fade in modal-page-wrapper-component"
		tds-handle-escape (escPressed)="cancelCloseDialog()"
		id="modal-page-wrapper" data-backdrop="static" tabindex="-1" role="dialog">
		<div class="modal-dialog modal-lg" role="document">
			<div class="modal-content" [ngStyle]="{'visibility': isVisible ? 'visible' : 'hidden'}">
				<div class="modal-header">
					<button (click)="cancelCloseDialog()" type="button" class="close" aria-label="Close">
						<span aria-hidden="true">×</span>
					</button>
					<h4 class="modal-title">{{title}}</h4>
				</div>
				<div class="modal-body">
					<div class="modal-body-container">
					<form id="wrapperForm"
						#targetForm="ngForm"
						[action]="action"
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
	@ViewChild('targetFrame') targetFrame: ElementRef;
	constructor(
		@Inject('title') public title: string,
		@Inject('action') public action: string,
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