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
	public static final String TAG_CANDIDATE 	= "Candidate";

	public static final String SCHEME_TCP						="ip";
	
	public static final boolean USE_SHAREDLIB					=Friend.USE_SHAREDLIB;
	public static final String SHARED_LIB						=Friend.SHARED_LIB;
		
	public static final int BINDING_TIMEOUT_WAIT				=10000; // FIXME: remove
	public static final int BINDING_NB_RETRY					=3;
	
	/** Buffer size for down load */
	public static final int BUFFER_SIZE_FOR_DOWNLOAD = 1024 * 10;

	/** Patch parcel for compatibility between different versions of Android */
	public static final boolean UPDATE_PARCEL = true;

    /** Delay to discover others remote androids. */
	public static final long TIME_MAX_TO_DISCOVER				=20000L;

	/** Use ethernet */
	public static final boolean ETHERNET						=true;
    /** Use only IP v4 */
    public static final boolean ETHERNET_ONLY_IPV4 				=false; 

    /** Order uris with IPV4 before IPV6 */
	public static final boolean ETHERNET_IPV4_FIRST = false;

	/** Use Mobile network */
	public static final boolean WAN = false;

	/** Check security. */
	public static final boolean SECURITY = true;

	/** Prefix for log */
	public static final String PREFIX_LOG='['/*+Build.DEVICE+' '*/+Build.MODEL+"] ";

	/** Timeout for ping remote binder. */
	public static final long TIMEOUT_PING_BINDER				=5000L; // second

	/** Timeout for connect to remote android. */
	public static final long TIMEOUT_CONNECT					=10000L; // FIXME second
    /** Flush current data before close the socket. */
    public static final boolean ETHERNET_SO_LINGER				=true; 	// Vide les derniers paquets avant la fermeture du socket
    /** Timeout to flush the last datas. */
    public static final int ETHERNET_SO_LINGER_TIMEOUT			=2000; // FIXME Delay for flush last packets
	/** Timeout to detect if binder is alive. */
	public static final long TIMEOUT_IS_BINDER_ALIVE			=200L; // FIXME second
	/** Timeout to inform the finalize of binder. */
	public static final long TIMEOUT_FINALIZE					=2000L; // FIXME second
	
	/** Timeout for accept a pairing chalenge. */
	public static final long TIMEOUT_PAIRING_ASK_CHALENGE = 30000L; // 30s

	/** Frequency of probe messages. */
	public static final int PROBE_INTERVAL_MS = 6000;

	/** Number of probe sent */
	public static final int PROBE_SENT = 2;

	public static final boolean HACK_DEAD_LOCK=false;
}
