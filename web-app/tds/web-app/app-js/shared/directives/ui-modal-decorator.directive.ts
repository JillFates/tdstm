import {Directive, AfterViewInit, ElementRef, Renderer2, Input } from '@angular/core';

declare var jQuery: any;

interface WindowSettings {
	left: string | number;
	top: string | number;
	width: string | number;
	height: string | number;
}

export interface DecoratorOptions {
	isFullScreen?: boolean;
	isCentered?: boolean;
	isResizable?: boolean;
	isDraggable?: boolean;
}

const SCROLLBAR_BORDER = 5;
const TOP_MARGIN = -30;

@Directive({ selector: '[tds-ui-window-decorator]' })
export class UIModalDecoratorDirective implements AfterViewInit {
	private isMaximized = false;
	private defaultOptions: DecoratorOptions = {isFullScreen: false, isCentered: true, isResizable: false, isDraggable: true};
	private decoratorOptions: DecoratorOptions;

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

	private initialWindowSettings: WindowSettings;

	constructor(private el: ElementRef, private renderer: Renderer2) {}

	ngAfterViewInit() {
		this.renderer.setStyle(this.el.nativeElement, 'visibility', 'hidden');
		// we need to delay because the bootstrap effect displaying modals
		setTimeout(() => {
			this.setOptions();
			this.renderer.setStyle(this.el.nativeElement, 'visibility', 'visible');
		}, 500);
	}

	private setOptions() {
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

	}

	private centerWindow() {
		const marginLeft = -(this.el.nativeElement.clientWidth / 2) ;

		this.renderer.setStyle(this.el.nativeElement, 'position', 'absolute');
		this.renderer.setStyle(this.el.nativeElement, 'left', '50%');
		this.renderer.setStyle(this.el.nativeElement, 'top', '0');
		this.renderer.setStyle(this.el.nativeElement, 'margin-left', this.toPixels(marginLeft));
	}

	private toPixels(value: string | number): string {
		return value + 'px';
	}

	enableDraggable(enable: boolean) {
		const element = jQuery(this.el.nativeElement);
		if (enable) {
			element.draggable({ containment: '#tdsUiDialog' });
		} else {
			element.draggable('destroy');
		}
	}

	enableResizable(enable: boolean) {
		const element = jQuery(this.el.nativeElement);
		if (enable) {
			element.resizable();
		} else {
			element.resizable('destroy');
		}
	}

	private maximizeWindow() {
		const {left, top, width, height} = this.el.nativeElement.style;
		this.initialWindowSettings = {left, top, width, height};

		const modalParent = this.el.nativeElement.closest('.modal');
		const fullWidth = parseInt(modalParent.width || modalParent.scrollWidth, 10) - SCROLLBAR_BORDER;
		const fullHeight = parseInt(modalParent.height || modalParent.scrollHeight, 10) - SCROLLBAR_BORDER;

		this.renderer.setStyle(this.el.nativeElement, 'width', this.toPixels(fullWidth));
		this.renderer.setStyle(this.el.nativeElement, 'height', this.toPixels(fullHeight));
		this.renderer.setStyle(this.el.nativeElement, 'margin-left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'left', '0');
		this.renderer.setStyle(this.el.nativeElement, 'top', this.toPixels(TOP_MARGIN));

		if (this.options.isResizable) {
			this.enableResizable(false);
		}
	}

	private restoreWindow() {
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
	}
}