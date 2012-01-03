package com.google.android.location.route;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.google.android.location.route.RouteManager.ListenerDataType;

import android.util.Log;
import static com.google.android.location.content.Constants.*;

public class RouteDataListeners {
	/** Internal representation of a listener's registration. */
	static class ListenerRegistration {
		final RouteMapDataListener listener;
		final EnumSet<ListenerDataType> types;

		// State that was last notified to the listener, for resuming after a
		// pause.
		long lastRouteId;
		long lastPointId;
		int lastSamplingFrequency;
		int numLoadedPoints;

		public ListenerRegistration(RouteMapDataListener listener,
				EnumSet<ListenerDataType> types) {
			this.listener = listener;
			this.types = types;
		}

		public boolean isInterestedIn(ListenerDataType type) {
			return types.contains(type);
		}

		public void resetState() {
			lastRouteId = 0L;
			lastPointId = 0L;
			lastSamplingFrequency = 0;
			numLoadedPoints = 0;
		}

		@Override
		public String toString() {
			return "ListenerRegistration [listener=" + listener + ", types="
					+ types + ", lastRouteId=" + lastRouteId + ", lastPointId="
					+ lastPointId + ", lastSamplingFrequency="
					+ lastSamplingFrequency + ", numLoadedPoints="
					+ numLoadedPoints + "]";
		}
	}

	private final List<ListenerRegistration> registeredArrayListeners = new ArrayList<ListenerRegistration>();
	/** Map of external listener to its registration details. */
	private final Map<RouteMapDataListener, ListenerRegistration> registeredListeners = new HashMap<RouteMapDataListener, ListenerRegistration>();

	/**
	 * Map of external paused listener to its registration details. This will
	 * automatically discard listeners which are GCollected.
	 */
	private final WeakHashMap<RouteMapDataListener, ListenerRegistration> oldListeners = new WeakHashMap<RouteMapDataListener, ListenerRegistration>();

	/**
	 * Map of data type to external listeners interested in it. EnumMap is a Map
	 * with enums as keys and the keys are of type Class<K>KeyType
	 */
	private final Map<ListenerDataType, Set<RouteMapDataListener>> listenerSetsPerType = new EnumMap<ListenerDataType, Set<RouteMapDataListener>>(
			ListenerDataType.class);

	/**
	 * the constructor. here we try to add all the ListenerDataType in a set
	 * with the TrackDataListner attached
	 */
	public RouteDataListeners() {
		// Create sets for all data types at startup.
		for (ListenerDataType type : ListenerDataType.values()) {
			listenerSetsPerType.put(type,
					new LinkedHashSet<RouteMapDataListener>());
		}
	}

	/**
	 * Registers a listener to send data to. It is ok to call this method before
	 * {@link TrackDataHub#start}, and in that case the data will only be passed
	 * to listeners when {@link TrackDataHub#start} is called.
	 * 
	 * @param listener
	 *            the listener to register
	 * @param dataTypes
	 *            the type of data that the listener is interested in
	 */
	public ListenerRegistration registerTrackDataListener(
			final RouteMapDataListener listener,
			EnumSet<ListenerDataType> dataTypes) {
		Log.d(TAG, "Registered track data listener: " + listener);
		if (registeredListeners.containsKey(listener)) {
			throw new IllegalStateException("Listener already registered");
		}

		ListenerRegistration registration = oldListeners.remove(listener);
		if (registration == null) {
			registration = new ListenerRegistration(listener, dataTypes);
		}
		registeredListeners.put(listener, registration);

		for (ListenerDataType type : dataTypes) {
			listenerSetsPerType.get(type).add(listener);
		}
		return registration;
	}

	/**
	   * 
	   */

	/**
	 * Unregisters a listener to send data to.
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	public void unregisterTrackDataListener(RouteMapDataListener listener) {
		Log.d(TAG, "Unregistered track data listener: " + listener);
		// Remove and keep the corresponding registration.
		ListenerRegistration match = registeredListeners.remove(listener);
		if (match == null) {
			Log.w(TAG, "Tried to unregister listener which is not registered.");
			return;
		}

		// Remove it from the per-type sets
		for (ListenerDataType type : match.types) {
			listenerSetsPerType.get(type).remove(listener);
		}

		// Keep it around in case it's re-registered soon
		oldListeners.put(listener, match);
	}

	public ListenerRegistration getRegistration(RouteMapDataListener listener) {
		ListenerRegistration registration = registeredListeners.get(listener);
		if (registration == null) {
			registration = oldListeners.get(listener);
		}
		return registration;
	}

	/**
	 * 
	 * @param type
	 *            type of listener data type , Track_Update , etc
	 * @return the value corresponding to the parameter key type
	 */
	public Set<RouteMapDataListener> getListenersFor(ListenerDataType type) {
		return listenerSetsPerType.get(type);
	}

	/**
	 * 
	 * @return a set of registered listeners basically all should be registered
	 */
	public EnumSet<ListenerDataType> getAllRegisteredTypes() {
		EnumSet<ListenerDataType> listeners = EnumSet
				.noneOf(ListenerDataType.class);
		for (ListenerRegistration registration : this.registeredListeners
				.values()) {
			listeners.addAll(registration.types);
		}
		return listeners;
	}

	/**
	 * 
	 * @return true if it's not empty false if it's empty
	 */
	public boolean hasListeners() {
		return !registeredListeners.isEmpty();
	}

	/**
	 * 
	 * @return the number of listeners in the Set registeredListeners
	 */
	public int getNumListeners() {
		return registeredListeners.size();
	}
}
