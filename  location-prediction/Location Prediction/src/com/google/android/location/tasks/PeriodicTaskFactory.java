package com.google.android.location.tasks;

/**
 * interface for creating periodic updates for checking if an idle time threshold has been achieved or not.
 * @author Andrei
 *
 */
public interface PeriodicTaskFactory 
{
    /**
     * creating the periodict updates instance
     * @return an instance of {@link PeriodicTaskImpl}
     */
	PeriodicTask create();

}
