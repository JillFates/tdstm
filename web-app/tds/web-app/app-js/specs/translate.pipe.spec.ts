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
				}
			}
		};

		pipe = new TranslatePipe(dictionary);
	});

	it('should return the correct string', () => {
		expect(pipe.transform('Level1.Level2A')).toBe('Foo.');
	});

	it('should return the correct string of a deeper level property', () => {
		expect(pipe.transform('Level1.Level2B.Level3A.Level4A')).toBe('Foo Bar.');
	});

	it('should return the key if property not exists', () => {
		expect(pipe.transform('Level1.Level2C')).toBe('Level1.Level2C');
	});

	it('should return an empty string if key is empty', () => {
		expect(pipe.transform('')).toBe('');
	});

});