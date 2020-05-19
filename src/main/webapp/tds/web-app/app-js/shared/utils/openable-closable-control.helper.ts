import {takeUntil} from 'rxjs/operators';
import {pathOr} from 'ramda';

export const TDS_ATTRIBUTE_IS_OPEN = 'tds-attribute-control-is-open';

export class OpenableClosableControlHelper {
	/**
	 * Take an array of controls and setup the listeners to the open/close events
	 * On open/close it adds the attribute to mark the control as 'open' or 'close' accordly
	 * @param controls
	 * @param takeUntilSubject
	 */
	public static setUpListeners(controls: any[], takeUntilSubject: any): void {
		controls.forEach(control => {
			control.open
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() => {
					const nativeElement = pathOr(null, control.nativeTarget, control);
					if (nativeElement) {
						nativeElement.setAttribute(TDS_ATTRIBUTE_IS_OPEN, 'true');
					}
				});

			control.close
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() =>
					setTimeout(
						() => {
							const nativeElement = pathOr(null, control.nativeTarget, control);
							if (nativeElement) {
								nativeElement.setAttribute(TDS_ATTRIBUTE_IS_OPEN, 'false');
							}
						},
						200
					)
				);
		});
	}
}
