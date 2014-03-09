import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder


class InternalSequenceService {

	boolean transactional = true
	def namedParameterJdbcTemplate
	
	def Integer next(Integer contextId, String name) {
		return namedParameterJdbcTemplate.queryForInt('SELECT tdstm_sequencer(:contextId, :name) as val', ['contextId' : contextId, 'name' : name])
	}
}
