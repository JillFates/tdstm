/**
 * Created by David Ontiveros on 5/19/2017.
 */
import {TranslatePipe} from '../shared/pipes/translate.pipe';

describe('TranslatePipe:', () => {

	let dictionary: object;

	let pipe: TranslatePipe;

	beforeEach(() => {

		dictionary = {
			'Level1': {
				'Level2A': 'Foo.',
				'Level2B': {
					'Level3A': {
						'Level4A': 'Foo Bar.'
					},
					'Level3B': 'Bar.'
				},
				'Level2C' : '{param1}B{param2}D'
			}
		};

		pipe = new TranslatePipe(dictionary);
	});

	it('should return the correct string', () => {
		expect(pipe.transform('Level1.Level2A', [])).toBe('Foo.');
	});

	it('should return the correct string of a deeper level property', () => {
		expect(pipe.transform('Level1.Level2B.Level3A.Level4A', [])).toBe('Foo Bar.');
	});

	it('should return the key if property not exists', () => {
		expect(pipe.transform('Level1.Level99', [])).toBe('Level1.Level99');
	});

	it('should return an empty string if key is empty', () => {
		expect(pipe.transform('', [])).toBe('');
	});

	it('should return the correct string even if params not needed are sent', () => {
		expect(pipe.transform('Level1.Level2A', ['A', 'B'])).toBe('Foo.');
	});

	it('should return the correct string with params replacement if params are given', () => {
		expect(pipe.transform('Level1.Level2C', ['A', 'C'])).toBe('ABCD');
	});

	it('should return the correct string with params replacement even if extra params are given', () => {
		expect(pipe.transform('Level1.Level2C', ['A', 'C', 'E'])).toBe('ABCD');
	});

	it('should return the correct string without params replacement if no params array are given', () => {
		expect(pipe.transform('Level1.Level2C', [])).toBe('{param1}B{param2}D');
	});
});