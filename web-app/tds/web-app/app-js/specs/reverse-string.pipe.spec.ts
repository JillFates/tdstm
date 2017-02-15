/**
 * Created by aaferreira on 15/02/2017.
 */
import {ReverseStringPipe} from '../shared/pipes/reverse-string.pipe'

describe('ReverseStringPipe:',()=>{
   let pipe: ReverseStringPipe

    beforeEach(() => {
        pipe = new ReverseStringPipe();
    });

    it('should return empty string if nothing is passed', () => {
        expect(pipe.transform('')).toBe('');
    });

    it('should return a reverted string', () => {
        expect(pipe.transform('potato')).toBe('otatop');
    });


});