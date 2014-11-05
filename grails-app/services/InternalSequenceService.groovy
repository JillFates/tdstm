import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import java.util.*
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource


class InternalSequenceService {

	boolean transactional = true
	def jdbcTemplate
	
	def Integer next(Integer contextId, String name) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate).withProcedureName("tdstm_sequencer");
		
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("context_id", contextId);
		inParamMap.put("name", name);
		SqlParameterSource sqlParam = new MapSqlParameterSource(inParamMap);
		
		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(sqlParam);
		return simpleJdbcCallResult.get('sequence_number');
	}
}
