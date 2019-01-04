package com.tdsops.tm.enums.domain

import net.transitionmanager.service.InvalidParamException;
import spock.lang.Specification;

class AssetCommentPropertyEnumTests extends Specification {

	def '1. getting AssetCommentPropertyEnum as map returns a map'() {
		when: 'getting AssetCommentPropertyEnum as map'
			Map<String, String> map = AssetCommentPropertyEnum.toMap()
		then: 'a Map instance is returned'
			map instanceof Map
		and: 'resultant map size is the same size as the list of AssetCommentPropertyEnum elements'
			map.size() == AssetCommentPropertyEnum.values().size()
	}

	def '2. getting valueOfFieldOrLabel with valid field returns the expected AssetCommentPropertyEnum element'() {
		when: 'getting an AssetCommentPropertyEnum by field'
			AssetCommentPropertyEnum subject = AssetCommentPropertyEnum.valueOfFieldOrLabel('taskNumber')
		then: 'an AssetCommentPropertyEnum is returned and it represents the expected AssetCommentPropertyEnum element'
			AssetCommentPropertyEnum.TASK_NUMBER == subject
	}

	def '3. getting valueOfFieldOrLabel with valid label returns the expected AssetCommentPropertyEnum element'() {
		when: 'getting an AssetCommentPropertyEnum by label'
			AssetCommentPropertyEnum subject = AssetCommentPropertyEnum.valueOfFieldOrLabel('Task Number')
		then: 'an AssetCommentPropertyEnum is returned and it represents the expected AssetCommentPropertyEnum element'
			AssetCommentPropertyEnum.TASK_NUMBER == subject
	}

	def '4. getting valueOfFieldOrLabel with invalid field or value throws InvalidParamException'() {
		when: 'getting an AssetCommentPropertyEnum with invalid field or label'
			AssetCommentPropertyEnum.valueOfFieldOrLabel('i-v-a-l-i-d-n-a-m-e-o-r-v-a-l-u-e')
		then: 'an InvalidParamException is thrown'
			InvalidParamException e = thrown()
		and: 'the message matches the expected exception'
			e.message == 'AssetCommentPropertyEnum field or label invalid: i-v-a-l-i-d-n-a-m-e-o-r-v-a-l-u-e'
	}

}
