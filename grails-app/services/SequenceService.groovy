import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class SequenceService {

	def internalSequenceService
	ExecutorService service
	
	public SequenceService() {
		this.service = Executors.newFixedThreadPool(30)
	}
	
	def Integer next(final Integer contextId, final String name, final Integer maxTries=10) {
		for (def i = 0; i < maxTries; i++) {
			try {
				Future<Integer> number = this.service.submit(new Callable<Integer>() {
					Integer call() {
						return internalSequenceService.next(contextId, name)
					}
				});
			
				Integer value = number.get(5, TimeUnit.SECONDS)
				if (value != null) {
					return value
				}
			} catch (Exception e) {
				log.info("Problem obtaining next value for sequence ${name} - ${contextId}")
			}
		}
		
		return null
	}
}
