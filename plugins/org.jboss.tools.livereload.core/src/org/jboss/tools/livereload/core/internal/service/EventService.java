/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xavier Coulon - Initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.livereload.core.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.tools.livereload.core.internal.util.Logger;

/**
 * The Publish/Subscribe engine to broadcast notifications 
 * 
 * @author xcoulon
 */
public class EventService {

	private static final EventService instance = new EventService();

	private final Map<Subscriber, List<EventFilter>> subscribers = new ConcurrentHashMap<Subscriber, List<EventFilter>>();

	/** Singleton constructor */
	private EventService() {
		super();
	}

	public static EventService getInstance() {
		return instance;
	}

	/**
	 * Notifies (only once) the subscribers that registered for the exact type
	 * of the given event, provided the accompanied filter matches.
	 * 
	 * @param event
	 */
	public void publish(final EventObject event) {
		if (this.subscribers.size() > 0) {
			Logger.debug("Notifying {} client(s) of event {}", this.subscribers.size(), event.toString());
			for (Entry<Subscriber, List<EventFilter>> entry: this.subscribers.entrySet()) {
				final Subscriber subscriber = entry.getKey();
				final List<EventFilter> filters = entry.getValue();
				for(EventFilter filter : filters) {
					if (filter.accept(event)) {
						Logger.debug("Informing subscriber '{}' of {}", subscriber.getId(), event.toString());
						subscriber.inform(event);
					} else {
						Logger.trace("Ignored event {} by subscriber {}", event, subscriber.getId());
					}
				}
			}
		} else {
			Logger.debug("*** No subscriber to inform about event {} ***", event.getSource());
		}
	}

	public void subscribe(final Subscriber subscriber, final EventFilter filter) {
		Logger.debug("Welcome to new subscriber {} " , subscriber.getId());
		if(!this.subscribers.containsKey(subscriber)) {
			this.subscribers.put(subscriber, new ArrayList<EventFilter>());
		}
		this.subscribers.get(subscriber).add(filter);
	}

	public void unsubscribe(final Subscriber subscriber) {
		Logger.debug("Good bye, {} " , subscriber.getId());
		this.subscribers.remove(subscriber);
	}

	public void resetSubscribers() {
		this.subscribers.clear();
	}

	/**
	 * @return the subscribers
	 */
	public Map<Subscriber, List<EventFilter>> getSubscribers() {
		return Collections.unmodifiableMap(this.subscribers);
	}

}
