package net.transitionmanager.common

import groovy.transform.CompileStatic
import net.transitionmanager.service.ServiceMethods
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.simple.SimpleJdbcCall

@CompileStatic
class InternalSequenceService implements ServiceMethods {

	static transactional = false

	JdbcTemplate jdbcTemplate

	Integer next(Integer contextId, String name) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName('tdstm_sequencer')
		(Integer) simpleJdbcCall.execute(new MapSqlParameterSource(context_id: contextId, name: name)).sequence_number
	}
}
