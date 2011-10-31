package org.remoteandroid.internal;

import org.remoteandroid.Friend;

import android.os.Build;

public class Constants
{
	public static /*final*/ boolean E=true; // FIXME: Must be false by default for public version.
	public static /*final*/ boolean W=true;
	public static /*final*/ boolean I=true;
	public static /*final*/ boolean D=true;
	public static /*final*/ boolean V=true;

	public static final int VERSION=0;
	
	public static final String TAG_INSTALL 		= "Install";
	public static final String TAG_PAIRING 		= "Pairing";
	public static final String TAG_SECURITY		= "Security";
	public static final String TAG_PREFERENCE 	= "Preference";
	public static final String TAG_CLIENT_BIND 	= "Client";

	
	public static final boolean USE_SHAREDLIB=Friend.USE_SHAREDLIB;
	public static final String SHARED_LIB=Friend.SHARED_LIB;
		
	/** Buffer size for down load */
	public static final int BUFFER_SIZE_FOR_DOWNLOAD=1024*10;
	
	/** Patch parcel for compatibility between different versions of Android */
	public static final boolean UPDATE_PARCEL=true;
	
	/** Use blue tooth */
	public static final boolean BLUETOOTH=false;
	
	public static final int BT_HACK_WAIT_AFTER_CREATE_RF_COMM=0; // Zero for good bluetooth stack.
    /** Delay to discover others remote androids. */
	public static final long TIME_MAX_TO_DISCOVER				=20000L;

	/** Use ethernet */
	public static final boolean ETHERNET=true;
	
	/** Use Mobile network */
	public static final boolean WAN=false;

	/** Check security. */
	public static final boolean SECURITY=true;

	/** Number of UUID key used. */
	public static final int BT_NB_UUID=5; // FIXME: 15 ?

	/** Prefix for log */
	public static final String PREFIX_LOG='['+Build.DEVICE+' '+Build.MODEL+"] ";

	/** Timeout for ping remote binder. */
	public static final long TIMEOUT_PING_BINDER				=5000L; // second

	/** Timeout for connect to remote android. */
	public static final long TIMEOUT_CONNECT					=45000L; // TODO 10; // second
	/** Timeout to detect if binder is alive. */
	public static final long TIMEOUT_IS_BINDER_ALIVE			=50000L; // second
	/** Timeout to inform the finalize of binder. */
	public static final long TIMEOUT_FINALIZE					=10000L; // second
	
	/** Timeout for accept a pairing chalenge. */
	public static final long TIMEOUT_PAIRING_ASK_CHALENGE		=30000L; // 30s

	
	/** Frequency of probe messages. */
	public static final int PROBE_INTERVAL_MS 					= 6000;
	/** Number of probe sent */
	public static final int PROBE_SENT 							= 2;

	
}
