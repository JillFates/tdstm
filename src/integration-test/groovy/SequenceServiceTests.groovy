import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
@Ignore
@Integration
@Rollback
class SequenceServiceTests extends  Specification{

	def sequenceService

	void "ignore test sequence"(){
		setup:
			final Set<Integer> numbers = new ConcurrentSkipListSet<Integer>()
			ExecutorService service = Executors.newFixedThreadPool(10)

			int numbersCount = 1000
			final Integer contextId = 1
			final String name = 'task'
			final Integer maxTries = 10

			final CountDownLatch latch = new CountDownLatch(numbersCount)

		when:
			for (int i = 0; i < numbersCount; i++) {
				service.execute(new Runnable() {
					void run() {
						Integer n = sequenceService.next(contextId, name, maxTries)
						if (n != null) {
							numbers.add(n)
						}
						latch.countDown()
					}
				})
			}

			latch.await()

		then:
			numbers.size() == numbersCount
	}
}
