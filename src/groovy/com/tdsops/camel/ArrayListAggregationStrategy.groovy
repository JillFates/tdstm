package com.tdsops.camel

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.processor.aggregate.AggregationStrategy

/**
 * Exchange message aggregator
 * This class is used to aggregate messages bodies into an ArrayList<String>
 */
class ArrayListAggregationStrategy implements AggregationStrategy {

    ArrayListAggregationStrategy() {
        super()
    }

    Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Message newIn = newExchange.getIn()
        Object newBody = newIn.getBody()
        ArrayList list = null
        if (oldExchange == null) {
            list = new ArrayList()
            list.add(newBody)
            newIn.setBody(list)
            return newExchange
        } else {
            Message inMsg = oldExchange.getIn()
            list = inMsg.getBody(ArrayList.class)
            list.add(newBody)
            return oldExchange
        }
    }

}