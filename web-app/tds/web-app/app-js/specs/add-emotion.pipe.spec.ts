/**
 * Created by aaferreira on 15/02/2017.
 */
import { AddEmotionPipe } from '../shared/pipes/add-emotion.pipe';

describe('AddEmotionPipe:', () => {
    let pipe: AddEmotionPipe;

    beforeEach(() => {
        pipe = new AddEmotionPipe();
    });

    it('should return empty string if nothing is passed', () => {
        expect(pipe.transform('', false)).toBe('');
    });

    it('should return a uppercase string', () => {
        expect(pipe.transform('wow', false)).toBe('WOW');
    });

    it('should return a uppercase string with emotion', () => {
        expect(pipe.transform('wow', true)).toBe('WOW!!!!');
    });

});