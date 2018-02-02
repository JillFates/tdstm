/**
 * Created by aaferreira on 04/03/2017.
 */
import {UIBooleanPipe} from '../shared/pipes/ui-boolean.pipe';

describe('UIBooleanPipe:', () => {
	let pipe: UIBooleanPipe;

	beforeEach(() => {
		pipe = new UIBooleanPipe();
	});

	it('should return No when false is passed', () => {
		expect(pipe.transform(false)).toBe('No');
	});

	it('should return Yes when true is passed', () => {
		expect(pipe.transform(true)).toBe('Yes');
	});

});