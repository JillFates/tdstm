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
	static setUpListeners(controls: {controlType: string, list: any[]}[], takeUntilSubject: any): void {
		const controlTypes = {
			dropDownList: ['wrapper', 'nativeElement'],
			dateControlList: ['datePicker', 'first', 'element', 'nativeElement'],
		};

		const listControls = [];
		controls.forEach((item: {controlType: string, list: any[]}) => {
			item.list.forEach((listItem: any) => {
				const nativeTarget = controlTypes[item.controlType];
				listControls.push({...listItem, nativeTarget});
			});
		});

		listControls.forEach(control => {
			control.open
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() => OpenableClosableControlHelper.setAttribute(control, 'true'));

			control.close
				.pipe(takeUntil(takeUntilSubject))
				.subscribe(() => setTimeout(() => OpenableClosableControlHelper.setAttribute(control, 'false'), 200));
		});
	}

	/**
	 * Set the value for the control attribute that indicates if it is open/close
	 * @param control
	 * @param value
	 */
	static setAttribute(control: any, value: string): void {
		const nativeElement = pathOr(null, control.nativeTarget, control);
		if (nativeElement) {
			nativeElement.setAttribute(TDS_ATTRIBUTE_IS_OPEN, value);
		}
	}
}
