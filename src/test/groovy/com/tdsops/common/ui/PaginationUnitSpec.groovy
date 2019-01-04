package com.tdsops.common.ui

import spock.lang.Specification
import com.tdsops.common.ui.Pagination

class PaginationUnitSpec extends Specification {

    def 'Tests for maxRowForParam method'() {
        expect: 
            result == Pagination.maxRowForParam(value)

        where: 
            value   | result
            '100'   | 100
            "100"   | 100
            '42'    | Pagination.MAX_DEFAULT
            null    | Pagination.MAX_DEFAULT
            'abc'   | Pagination.MAX_DEFAULT
            '-1'    | Pagination.MAX_DEFAULT
            Pagination.MAX_OPTIONS[-1].toString() | Pagination.MAX_OPTIONS[-1]
    }

    def 'Tests for pageForParam method'() {
        expect:
            result == Pagination.pageForParam(value)

        where:
            value   | result
            '1'     | 1
            "1"     | 1
            ''      | 1
            'x'     | 1
            null    | 1
            '-1'    | 1
            '42'    | 42
    }

    def 'Tests for rowOffset method'() {
        expect:
            result == Pagination.rowOffset(page, rows)

        where:
            page    | rows  | result
            1       | 100   | 0
            0       | 100   | 0
            2       | 100   | 100
            null    | 100   | 0
            3       | 50    | 100
    }

}
