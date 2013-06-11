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

import java.util.Collection;
import java.util.EventObject;
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

	private final Map<Subscriber, EventFilter> subscribers = new ConcurrentHashMap<Subscriber, EventFilter>();

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
			for (Entry<Subscriber, EventFilter> entry: this.subscribers.entrySet()) {
				final Subscriber subscriber = entry.getKey();
				final EventFilter filter = entry.getValue();
				if(filter.accept(event)) {
					Logger.debug("Informing subscriber '{}' of {}", subscriber.getId(), event.getSource());
					subscriber.inform(event);
				} else {
					Logger.debug("Ignored event {} by subscriber {}", event, subscriber.getId());
				}
			}
		} else {
			Logger.debug("*** No subscriber to inform about event {} ***", event.getSource());
		}
	}

	public void subscribe(final Subscriber subscriber, final EventFilter filter) {
		Logger.debug("Welcome to new subscriber {} " , subscriber.getId());
		this.subscribers.put(subscriber, filter);
	}

	public void unsubscribe(final Subscriber subscriber) {
		Logger.debug("Good bye, {} " , subscriber.getId());
		this.subscribers.remove(subscriber);
	}

	public void resetSubscribers() {
		getSubscribers().clear();
	}

	/**
	 * @return the subscribers
	 */
	public Collection<Subscriber> getSubscribers() {
		return subscribers.keySet();
	}

}
