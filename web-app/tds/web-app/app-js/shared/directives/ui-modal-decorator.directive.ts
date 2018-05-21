/**
 * Enable full screen and resizable capabilities to modal windows
 */
import {Directive, AfterViewInit, ElementRef, Renderer2, Input } from '@angular/core';
import { DecoratorOptions, WindowSettings } from '../model/ui-modal-decorator.model';

declare var jQuery: any;
const SCROLLBAR_BORDER = 5;
const TOP_MARGIN = -30;

@Directive({
	selector: '[tds-ui-modal-decorator]'
})
export class UIModalDecoratorDirective implements AfterViewInit {
	private isMaximized = false;
	private defaultOptions: DecoratorOptions = {isFullScreen: false, isCentered: true, isResizable: false, isDraggable: true};
	private decoratorOptions: DecoratorOptions;
	private initialWindowSettings: WindowSettings;
	private parentModal: any;

	@Input()
	set isWindowMaximized(isWindowMaximized: boolean) {
		this.isMaximized = isWindowMaximized;
		if (this.isMaximized) {
			this.maximizeWindow();
		} else {
			this.restoreWindow();
		}
	}
	get isWindowMaximized(): boolean { return this.isMaximized; }

	@Input()
	set options(options: DecoratorOptions) {
		this.decoratorOptions = Object.assign({}, this.defaultOptions, options);
	}
	get options(): DecoratorOptions { return this.decoratorOptions; }

	constructor(private el: ElementRef, private renderer: Renderer2) {}

	ngAfterViewInit() {
		// hide host while setup is executing
		this.renderer.setStyle(this.el.nativeElement, 'visibility', 'hidden');
		// we need to delay because the bootstrap effect displaying modals
		setTimeout(() => {
			this.setOptions();
			// show host when setup is done
			this.renderer.setStyle(this.el.nativeElement, 'visibility', 'visible');
		}, 500);
	}

	/**
	 * Based on options passed to directive apply the corresponding decorators
	 */
	private setOptions(): void {
		this.parentModal = this.el.nativeElement.closest('.modal');
		this.setMaxSizeWindow();
		const {left, top, width, height} = this.el.nativeElement.style;
		this.initialWindowSettings = {left, top, width, height};

		if (this.options.isDraggable) {
			this.enableDraggable(true);
		}

		if (this.options.isResizable) {
			this.enableResizable(true);
		}

		if (this.options.isCentered) {
			this.centerWindow();
		}

		return;
	}

	/**
	 * Set the corresponding styles to center the host attached to this directive
	 */
	private centerWindow(): void {
		const marginLeft = -(this.el.nativeElement.clientWidth / 2) ;

		this.renderer.setStyle(this.el.nativeElement, 'position', 'absolute');
		this.renderer.setStyle(this.el.nativeElement, 'left', '50%');
		this.renderer.setStyle(this.el.nativeElement, 'top', '0');
		this.renderer.setStyle(this.el.nativeElement, 'margin-left', this.toPixels(marginLeft));

		return;
	}

	/**
	 * Convert value to pixels
	 */
	private toPixels(value: string | number): string {
		const stringValue = value.toString();
		return (stringValue.endsWith('px')) ? stringValue : value + 'px';
	}

	/**
	 * Enable draggable capability to the host attached to this directive
	 */
	private enableDraggable(enable: boolean): void {
		const element = jQuery(this.el.nativeElement);

		if (enable) {
			element.draggable({ containment: '#tdsUiDialog' });
		} else {
			element.draggable('destroy');
		}

		return;
	}

	/**
	 * Enable resizable capability to the host attached to this directive
	 */
	private enableResizable(enable: boolean): void {
		const element = jQuery(this.el.nativeElement);

		if (enable) {
			element.resizable();
		} else {
			element.resizable('destroy');
		}

		return;
	}

	/**
	 * Based on width and height of parent modal, set the width and height of the host to fill up the width and height available,
	 * previously save the initial window setting in order to be able to get back to previous width and height setting
	 */
	private maximizeWindow(): void {
		const {left, top, width, height} = this.el.nativeElement.style;
		this.initialWindowSettings = {left, top, width, height};

		const fullWidth = parseInt(this.parentModal.width || this.parentModal.scrollWidth, 10) - SCROLLBAR_BORDER;
		const fullHeight = parseInt(this.parentModal.height || this.parentModal.scrollHeight, 10) - SCROLLBAR_BORDER;

		this.renderer.setStyle(this.el.nativeElement, 'width', this.toPixels(fullWidth));
		this.renderer.setStyle(this.el.nativeElement, 'height', this.toPixels(fullHeight));
		this.renderer.setStyle(this.el.nativeElement, 'margin-left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'top', this.toPixels(TOP_MARGIN));

		if (this.options.isResizable) {
			this.enableResizable(false);
		}

		return;
	}

	/**
	 * Restore width/height of host to previous initial window settings
	 */
	private restoreWindow(): void {
		if (!this.initialWindowSettings) { return; }

		const {left, top, width, height} = this.initialWindowSettings;
		this.renderer.setStyle(this.el.nativeElement, 'width', width);
		this.renderer.setStyle(this.el.nativeElement, 'height', height);

		if (this.options.isCentered) {
			this.centerWindow();
		}

		if (this.options.isResizable) {
			this.enableResizable(true);
		}

		return;
	}
	/**
	 * restrict size/height if current size modal window exceeds the limits of the screen
	 */
	private setMaxSizeWindow(): void {
		const {width, height} = this.el.nativeElement.style;
		const currentWidth = parseInt(width.toString(), 10);
		const currentHeight = parseInt(height.toString(), 10);

		this.renderer.setStyle(this.parentModal, 'padding-left', '0');

		if (currentWidth && currentWidth > this.parentModal.clientWidth) {
			this.renderer.setStyle(this.el.nativeElement, 'width', this.toPixels(this.parentModal.clientWidth - SCROLLBAR_BORDER));
		}

		if (currentHeight && currentHeight > this.parentModal.clientHeight) {
			this.renderer.setStyle(this.el.nativeElement, 'height', this.toPixels(this.parentModal.clientHeight - SCROLLBAR_BORDER));
		}
	}
}