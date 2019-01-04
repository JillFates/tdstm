/**
 * Enable full screen and resizable capabilities to modal windows
 */
import {Directive, AfterViewInit, OnDestroy, ElementRef, Renderer2, Input, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { PreferenceService} from '../../shared/services/preference.service';
import { DecoratorOptions, WindowSettings } from '../model/ui-modal-decorator.model';

declare var jQuery: any;
const SCROLLBAR_BORDER = 5;
const TOP_MARGIN = -30;

@Directive({
	selector: '[tds-ui-modal-decorator]'
})
export class UIModalDecoratorDirective implements AfterViewInit, OnDestroy {
	private isMaximized = false;
	private defaultOptions: DecoratorOptions = {isFullScreen: false, isCentered: true, isResizable: false, isDraggable: true};
	private decoratorOptions: DecoratorOptions;
	private initialWindowSettings: WindowSettings;
	private parentModal: any;
	@Output() resizeEvent = new EventEmitter<any>();

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

	constructor(private el: ElementRef, private renderer: Renderer2, private preferenceService: PreferenceService) {}

	ngOnDestroy() {
		if (this.options && this.options.sizeNamePreference) {
			this.saveWindowSize()
				.subscribe((result) => console.log(result))
		}
	}

	ngAfterViewInit() {
		// On resize the windows, recalculate the center position
		jQuery(window).resize((event) => {
			if (event.target === window) {
				this.centerWindow();
			}
		});

		// hide host while setup is executing
		this.renderer.setStyle(this.el.nativeElement, 'visibility', 'hidden');
		// we need to delay because the bootstrap effect displaying modals
		setTimeout(() => {
			this.getWindowSize()
				.subscribe((size: {width: number, height: number}) => {
					// If the window is maximized by default, apply the default size for the restored size, and maximize the window
					if (size && !this.isWindowMaximized) {
						this.renderer.setStyle(this.el.nativeElement, 'width', this.toPixels(size.width));
						this.renderer.setStyle(this.el.nativeElement, 'height', this.toPixels(size.height));
					}
					this.setOptions();
					// show host when setup is done
					this.renderer.setStyle(this.el.nativeElement, 'visibility', 'visible');

					if (this.isWindowMaximized) {
						this.maximizeWindow();
					}
				});
		}, 500);
	}

	private getWindowSize(): Observable<any> {
		if (this.options && this.options.sizeNamePreference) {
			return this.preferenceService.getDataScriptDesignerSize();
		}
		return Observable.of(null);
	}

	/**
	 * Based on options passed to directive apply the corresponding decorators
	 */
	private setOptions(): void {
		this.parentModal = this.el.nativeElement.closest('.modal');
		this.setMaxSizeWindow();
		const {left, top, width, height} = this.el.nativeElement.style;
		this.initialWindowSettings = {left, top, width, height};

		if (this.options && this.options.isDraggable) {
			this.enableDraggable(true);
		}

		if (this.options && this.options.isResizable) {
			this.enableResizable(true);
		}

		if (this.options && this.options.isCentered) {
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
			element.draggable({
				containment: '#tdsUiDialog',
				handle: '.modal-header'
			});
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
			element.resizable({
				resize: (event: any, ui: any) => this.resizeWindow(event, ui),
				start: (event: any, ui: any) => this.startResize(event, ui),
				stop: (event: any, ui: any) => this.stopResize(event, ui),
			});
		} else {
			element.resizable('destroy');
		}

		return;
	}

	/**
	 * Listen to the Resize Event (in progress)
	 * http://api.jqueryui.com/resizable/#event-resize
	 */
	private resizeWindow(event: any, ui: any): void {
		this.resizeEvent.emit({type: 'resizing', event: event, ui: ui});
	}

	/**
	 * Listen to the Resize Event Start
	 * http://api.jqueryui.com/resizable/#event-start
	 */
	private startResize(event: any, ui: any): void {
		// this.resizeEvent.emit({type: 'start', event: event, ui: ui});
	}

	/**
	 * Listen to the Resize Event Stop
	 * http://api.jqueryui.com/resizable/#event-stop
	 */
	private stopResize(event: any, ui: any): void {
		// this.resizeEvent.emit({type: 'stop', event: event, ui: ui});
	}

	/**
	 * Based on width and height of parent modal, set the width and height of the host to fill up the width and height available,
	 * previously save the initial window setting in order to be able to get back to previous width and height setting
	 */
	private maximizeWindow(): void {
		if (!this.initialWindowSettings) { return; }
		const {left, top, width, height} = this.el.nativeElement.style;
		this.initialWindowSettings = {left, top, width, height};

		const fullWidth = parseInt(this.parentModal.width || this.parentModal.scrollWidth, 10) - SCROLLBAR_BORDER;
		const fullHeight = parseInt(this.parentModal.height || this.parentModal.scrollHeight, 10) - SCROLLBAR_BORDER;

		this.renderer.setStyle(this.el.nativeElement, 'width', this.toPixels(fullWidth));
		this.renderer.setStyle(this.el.nativeElement, 'height', this.toPixels(fullHeight));
		this.renderer.setStyle(this.el.nativeElement, 'margin-left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'top', this.toPixels(TOP_MARGIN));

		if (this.options && this.options.isResizable) {
			this.enableResizable(false);
		}
		this.resizeEvent.emit('maximize');

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

		if (this.options && this.options.isCentered) {
			this.centerWindow();
		}

		if (this.options && this.options.isResizable) {
			this.enableResizable(true);
		}

		this.resizeEvent.emit('restore');
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

	/**
	 * Save the width/height size as a user preference setting
	 */
	private saveWindowSize(): Observable<any> {
		const { width, height } = this.el.nativeElement.style;

		const sizeDataScript = [{width: width || 0,  height: height ||  0}]
			.map((size: {width: string, height: string}) => ({ width: parseInt(size.width, 10), height: parseInt(size.height, 10) }))
			.shift();

		return this.preferenceService.setPreference(this.options.sizeNamePreference, `${sizeDataScript.width}x${sizeDataScript.height}`)
	}
}