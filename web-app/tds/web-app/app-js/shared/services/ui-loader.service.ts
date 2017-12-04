import { Injectable } from '@angular/core';

@Injectable()
export class UILoaderService {
	loaderConfig = {
		show: false,
		inProgress: false
	};

	show(): void {
		this.loaderConfig.show = true;
	}

	hide(): void {
		this.loaderConfig.show = false;
	}

	initProgress(): void {
		this.loaderConfig.inProgress = true;
	}
	stopProgress(): void {
		this.loaderConfig.inProgress = false;
	}

	toggle(): void {
		this.loaderConfig.show = !this.loaderConfig.show;
	}
}