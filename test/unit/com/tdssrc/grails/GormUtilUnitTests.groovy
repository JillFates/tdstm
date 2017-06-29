package com.tdssrc.grails

import java.text.SimpleDateFormat
import com.tdssrc.grails.GormUtil

import com.tds.asset.AssetEntity

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the GormUtil class
 */
class GormUtilUnitTests extends Specification {

    protected void setup() {
    }

    protected void cleanup() {
    }

	void 'Test isDomainProperty'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			GormUtil.isDomainProperty(asset, 'assetName')
			GormUtil.isDomainProperty(asset, 'id')
			! GormUtil.isDomainProperty(asset, 'bogusPropertyName')
	}

	void 'Test getDomainPropertyType'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			(GormUtil.getDomainPropertyType(asset, 'assetName') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'custom1') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'priority') == java.lang.Integer)
		and:
			! GormUtil.getDomainPropertyType(asset, 'bogusPropertyName')
	}

	void 'Test getDomainPropertyTypeForClass'() {
		expect:
			GormUtil.getDomainPropertyTypeForClass(clazz, property) == type
		where:
			clazz				|	property			|	type
			AssetEntity			|	"assetName"			|	java.lang.String
			AssetEntity			|	"custom1"			|	java.lang.String
			AssetEntity			|	"priority"			|	java.lang.Integer
			AssetEntity			|	"bogusPropertyName"	|	null
	}

}
