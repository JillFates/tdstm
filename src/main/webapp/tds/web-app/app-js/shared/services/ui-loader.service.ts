import {Injectable} from '@angular/core';

@Injectable()
export class UILoaderService {
	public loaderConfig = {
		show: false,
		inProgress: false,
		disabled: false
	};

	show(): void {
		this.loaderConfig.show = true;
	}

	hide(): void {
		this.loaderConfig.show = false;
	}

	/**
	 * Init the progress loading unless it is disabled
	 */
	initProgress(): void {
		// Can't init the progress if it is disabled
		if (!this.loaderConfig.disabled) {
			this.loaderConfig.inProgress = true;
		}
	}

	stopProgress(): void {
		this.loaderConfig.inProgress = false;
	}

	toggle(): void {
		this.loaderConfig.show = !this.loaderConfig.show;
	}

	/**
	 * To disabled the animation completely
	 * @param disabled
	 */
	disableProgress(disabled: boolean): void {
		this.loaderConfig.disabled = disabled;
	}
}
