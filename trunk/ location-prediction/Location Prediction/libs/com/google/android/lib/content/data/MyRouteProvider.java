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
	  * @param routeId = id of the route 
	  * from where the route track point should be retrieved
	  * @return the RouteTrackPoint with the route_id = @routeId
	  */
	 RouteTrackPoint getRouteTrackPointById(long routeId);
	
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
	  * 
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
	 /**retrieve a cursor over a number of locations 
	 *@return cursor having the min point for a route
	 */
	 Cursor getLocationsCursor(long routeId, long minRoutePointId,int locationsNumber, boolean sort_type);
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
	 //iterato through a number of locations form a route , given a starting point
	 LocationIterator getLocationIterator(long routeId, long startRoutePointId, 
			                              boolean sort_type, CreateLocationFactory locationFactory);

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
