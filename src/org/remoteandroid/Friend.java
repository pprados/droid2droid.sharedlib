package org.remoteandroid;

// Hack to transform package access to public access
public final class Friend
{
	public static final boolean USE_SHAREDLIB=RemoteAndroidManager.USE_SHAREDLIB;
	public static final String SHARED_LIB=RemoteAndroidManager.SHARED_LIB;
	public static abstract class Factories extends org.remoteandroid.Factories
	{
		
	}
}
