package net.transitionmanager.common

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import net.transitionmanager.service.ServiceMethods

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@CompileStatic
@Slf4j()
class SequenceService implements ServiceMethods {

	static transactional = false

	private ExecutorService service = Executors.newFixedThreadPool(30)

	InternalSequenceService internalSequenceService

	/**
	 * Used to determine the next sequence number for given context and key
	 * @param contextId - the id number that is used to uniquely identify common keys amoungst
	 * clients (typically this is the client id)
	 * @param key - the key or label used to reference a particular sequence value
	 * @param maxTries - provides the ability to control the number of retry attempts if the service
	 * is having contention issues with accessing the sequence table record (default:10)
	 * @return The next sequence number for the context id + key
	 */
	Integer next(final Long contextId, final String key, final Integer maxTries=10) {
		for (int i = 0; i < maxTries; i++) {
			try {
				Future<Integer> number = service.submit(new Callable<Integer>() {
					Integer call() {
						return internalSequenceService.next(contextId.toInteger(), key)
					}
				})

				Integer value = number.get(5, TimeUnit.SECONDS)
				if (value != null) {
					return value
				}
			} catch (e) {
				log.error 'Problem obtaining next value for scontext={}, key={}, exception {}', contextId, key, e.message
			}
		}

		log.error 'Unable to retrieve next sequence number context={}, key=$key'
		throw new RuntimeException('Unable to retrieve next sequence number')
	}
}
