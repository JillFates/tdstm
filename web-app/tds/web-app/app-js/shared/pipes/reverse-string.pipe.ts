/**
 * Created by aaferreira on 15/02/2017.
 */
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'reverseString' })
export class ReverseStringPipe implements PipeTransform {
    transform(value: string): string {
        return value.split('').reverse().join('');
    }
}