package com.google.android.lib.content.data;

import java.util.Iterator;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;


import com.google.android.lib.content.RouteTrackPoint;
import com.google.android.lib.content.Route;


public interface MyRouteProvider 
{
	 public static final String AUTHORITY = "com.google.android.maps.myroutes";
	 /**
	  * delete all the routes and tracks
	  */
	 void deleteAllRoutes();
	 
	 /**
	  * gets a location by name or by its properties
	  * @param locationToSearch the location to be searched
	  * @param routeId the id of the route the locations belongs to
	  * @param locationFactory CreateLocationFactory 
	  * @return the location from db or null if the location doesn't exist.
	  */
	 Location getLocationByName(Location locationToSearch, long routeId, CreateLocationFactory locationFactory);
	 /**
	  * updates the current location times count from the database;
	  * @param location the location to be updated
	  * @param routeId the id of the route from where the location should be retrieved
	  * @param locationFactory the location factory.
	  * @param cursor - the cursor over a all the locations from the given routeId
	  */
	 void updateLocationTimesCount(Location location, long routeId, CreateLocationFactory locationFactory,Cursor cursor);
	 /**
	  * gets the number of times a user has been at the specific location from a route
	  * if the location hasn't been found the method returns -1
	  * @param location the location to be
	  * @param routeId the id of the route where the search takes place
	  * @param locationFactory the location factory type
	  * @param cursor - a cursor over all the locations that belongs to the route that has the id routeId
	  * @return the nr of times that the user has visited the location.
	  */
	 int getLocationTimesCount(Location location, long routeId, CreateLocationFactory locationFactory, Cursor cursor);
	 /**delete a route based id
	 * deletes a route based on its id
	 * @param routeId = the id of the route to be deleted
	 */
	 void deleteRouteById(long routeId);
	 /** delete a route track point with a description and id
	 * @param routeId = id of the route from where the track should be deleted
	 */
	 void deleteRouteTrackPoint(long endPointId, DescriptionGenerator descriptionGenerator);
	 /**
	  * 
	  * @param track after we search the next point
	  * @return the next statistics track
	  */
	 RouteTrackPoint getNextStatisticRouteTrackPointAfter(RouteTrackPoint track);
	 /**
	  * updates a route track point
	  * @param route track ppoint to update
	  * @return true if successful , false if not
	  */
	 boolean updateRouteTrackPoint(RouteTrackPoint track);
	 /**
	  * get last recorded location
	  * @return last location
	  */
	 Location getLastRecordedLocation();
	 /**
	  * get first route track point
	  * @param routeId = id of the route
	  * @return first route track point from the route with
	  * @routeId = routeId
	  */
	 long getFirstRouteTrackPointId(long routeId);
	 /**
	  * 
	  * @param routeId id of the route
	  * @return the last route track point id from route
	  */
	 long getLastRouteTrackPointId(long routeId);
	 /**
	  * 
	  * @param routeTrackPointId = id of the route track point 
	  * from where the route track point should be retrieved
	  * @return the RouteTrackPoint with the @_id = routeTrackPointId
	  */
	 RouteTrackPoint getRouteTrackPointById(long routeTrackPointId);
	
	 /**get lastLocationid
		 * @param routeId = id of the route where the location is
		 */
	 long getLastLocationId(long routeId);
	 /**
	  * 
	  * @return the id of the last route
	  */
	 long getLastRouteId();
	 /**
	  * finds a location in a route knowing the id of the location.
	  * @param id of the location
	  * @return the location
	  */
	 Location getLocationById(long id);
	 //get the last route
	 Route getLastRoute();
	 /**
	  * 
	  * @param id
	  * @return the Route with the id=@id
	  */
	 Route getRouteById(long id);
	 /**
	  * 
	  * @return a list of the routes available
 	  */
	 List<Route>getAllRoutes();
	 /**
	 * @param routeid - the id of the current route
	 * @param minRoutePointId - the minimum point from where the cursor it's retrieved
	 * @param locationsNumber - the number of locations
	 * @param descending - true if the sorting is descending , false if not
	 */
	 Cursor getLocationsCursor(long routeId, long minRoutePointId,int locationsNumber, boolean descending);
	 /**@param id = the id of the route
	 *@param minTrackId the minimum trackid,
	 *@param maxTraks = the max nr of endpoints
	 */
	 Cursor getRouteTrackPointsCursor(long routeId, long minTrackId, int maxTraks);
	 /**get a cursor over some selected routes
	 * @param selection = selection for the cursor
	 */
	 Cursor getSelectedRoutesCursor(String selection);
	 //insert a new route
	 Uri insertRoute(Route newRoute);
	 /**
	  * insert a new Route Point into a route
	  * @param location the location where the insertion 
	  * takes place
	  * @param routeId the id of the route
	  * @return the Uri for cursor to insert
	  * in the db
	  */
	 Uri insertRoutePoint(Location location, long routeId);
	 /**
	  * insert an array of locations with a length and a route that belongs to
	  * @param locations the array of locations to be inserted
	  * @param length the length of the track
	  * @param routeId and the route id
	  * @return the nr inserted
	  */
	 int insertManyRouteTrackPoints(Location locations[], int length,long routeId);
	 /**insert an new RouteTrackPoint in the provider
	  * returns the Uri for the new track
	  * @param track to be inserted
	  * @return
	  */
	 Uri insertRouteTrackPoint(RouteTrackPoint track);
	 /**
	  * check if a route exists or not
	  * @param routeId
	  * @return true if found false otherwise
	  */
	 boolean checkRoute(long routeId);
	 /**
	  * update the current route
	  * @param route to be updated
	  */
	 void updateRoute(Route route);
	 /**
	  * checks if a given location in a given route exits or not.
	  * @param location  the location to search for
	  * @param routeId the id of the route where to search the location
	  * @return false if the location doesn't exist and true if it does.
	  */
	 boolean checkLocation(Location location, long routeId, CreateLocationFactory locationFactory);
	 /**
	  * update a route using a location and an endPoint
	  * @param RouteTrackPoint that is to be update and
	  * @pram location from the route
	  */
	 void updateRoute(RouteTrackPoint endpoint);
	 /**
	  * create a new route in the db
	  * @param cursor that is required for the Route
	  * @return a new route from a cursor
	  */
	 Route createRoute(Cursor cursor);
	 /**
	   * Creates the ContentValues for a given RouteTrackPoint object.
	   *
	   * Note: If the track has an id<0 the id column will not be filled.
	   *
	   * @param track a given track object
	   * @return a filled in ContentValues object
	   */
	 ContentValues createContentValues(Route route);
	 /**creating a location for a given cursor 
	 **and after an endpoint
	 */
	 Location createLocation(Cursor cursor);
	 /**
	  * create a track from a cursor
	  * @param cursor
	  * @return a new track
	  */
	 RouteTrackPoint createRouteTrackPoint(Cursor cursor);
	 /**
	  * sets the current batch size
	  */
	 void setBatchSize(int value);
	 /**
	  * Updates a location by a give cursor
	  * @param location to update
	  * @param cursor that updates the location in the db
	  */
	 
	 void updateLocation(Cursor cursor,Location location);
	 interface LocationIterator extends Iterator<Location>
	 {
		 long getLocationid();
		 void close();
	 }
	 
	 interface RouteIterator extends Iterator<Location>
	 {
		 long getRoute();
		 String getName();
		 String getDescription();
		 String getCategory();
		 void close();
	 }
	 interface LocationFactory
	 {
		 Location createGPSLocation();
		 Location createNetworkLocation();
		 
	 }
	 LocationFactory MyFactoryLocation = new LocationFactory()
	 {

		public Location createGPSLocation() 
		{
			return new Location("gps");
		}

		public Location createNetworkLocation() 
		{
		   return new Location("Network");	
		}
	 };
	 CreateLocationFactory MyCreateLocationFactory = new CreateLocationFactory() 
	 {	
		 public Location createGPSLocation() 
			{
				return new Location("gps");
			}

			public Location createNetworkLocation() 
			{
			   return new Location("Network");	
			}
	};
	/**
	   * Creates a new read-only iterator over all track points for the given route.  It provides
	   * a lightweight way of iterating over long routes without failing due to the underlying cursor
	   * limitations. Since it's a read-only iterator, {@link Iterator#remove()} always throws
	   * {@class UnsupportedOperationException}.
	   * 
	   * Each call to {@link LocationIterator#next()} may advance to the next DB record, and if so,
	   * the iterator calls {@link LocationFactory#createLocation()} and populates it with information
	   * retrieved from the record.
	   * 
	   * When done with iteration, you must call {@link LocationIterator#close()} to make sure that all
	   * resources are properly deallocated.
	   * 
	   * Example use:
	   * <code>
	   *   ...
	   *   LocationIterator it = providerUtils.getLocationIterator(
	   *       1, MyTracksProviderUtils.DEFAULT_LOCATION_FACTORY);
	   *   try {
	   *     for (Location loc : it) {
	   *       ...  // Do something useful with the location.
	   *     }
	   *   } finally {
	   *     it.close();
	   *   }
	   *   ...
	   * </code>
	   * 
	   * @param routeId the ID of a track to retrieve locations for.
	   * @param startRoutePointId the ID of the first track point to load, or -1 to start from
	   *        the first point.
	   * @param sort_type if true the results will be returned in descending ID
	   *        order (latest location first).
	   * @param locationFactory the factory for creating new locations.
	   * 
	   * @return the read-only iterator over the given route's points.
	   */
	 LocationIterator getLocationIterator(long routeId, long startRoutePointId, 
			                              boolean descending, CreateLocationFactory locationFactory);

	 public static class Factory
	 {
		 public static Factory instance  = new Factory();
		 public static Factory getInstance()
		 {
			 return instance;
		 }
		 public static MyRouteProvider getRouteProvider(Context context)
		 {
			 return instance.newInstance(context);
		 }
		protected MyRouteProvider newInstance(Context context) 
		{
           return new MyRouteProviderImpl(context.getContentResolver());
		}
	
	 }
}
