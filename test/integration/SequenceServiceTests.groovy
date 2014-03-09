import grails.test.*

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SequenceServiceTests extends GroovyTestCase {

	static transactional = false
	
	def sequenceService
	
	public void ignoreTestSequence() {
		final Set<Integer> numbers = new ConcurrentSkipListSet<Integer>()
		ExecutorService service = Executors.newFixedThreadPool(10)
		
		int numbersCount = 1000;
		final Integer contextId = 1
		final String name = 'task'
		final Integer maxTries = 10
		
		final CountDownLatch latch = new CountDownLatch(numbersCount);
		
		for (int i = 0; i < numbersCount; i++) {
			service.execute(new Runnable() {
				void run() {
					def sleepMills = Math.round(Math.random() * 100)
					Thread.sleep(sleepMills)
					
					Integer n = sequenceService.next(contextId, name, maxTries)
					if (n != null) {
						numbers.add(n)
					}
					latch.countDown();
				}
			})
		}
		
		latch.await(10, TimeUnit.SECONDS);
		assertEquals(numbers.size(), numbersCount)
	}
}
