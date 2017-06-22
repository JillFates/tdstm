import { Injectable } from '@angular/core';

@Injectable()
export class UILoaderService {
	loaderConfig = {
		show: false
	};

	show(): void {
		this.loaderConfig.show = true;
	}

	hide(): void {
		this.loaderConfig.show = false;
	}

	toggle(): void {
		this.loaderConfig.show = !this.loaderConfig.show;
	}
}