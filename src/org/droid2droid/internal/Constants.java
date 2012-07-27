/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.internal;

import org.droid2droid.Friend;
import org.droid2droid.sharedlib.BuildConfig;

import android.os.Build;
import android.os.Debug;

public final class Constants
{
	public static /*final*/ boolean E							=BuildConfig.DEBUG;
	public static /*final*/ boolean W							=E;
	public static /*final*/ boolean I							=W;
	public static /*final*/ boolean D							=Debug.isDebuggerConnected();
	public static /*final*/ boolean V							=D;

	public static final String TAG_D2D						="D2D";

	public static final int VERSION_D2D						=0; // FIXME: Version
	
	public static final String TAG_INSTALL 					= "Install";
	public static final String TAG_PAIRING 					= "Pairing";
	public static final String TAG_SECURITY					= "Security";
	public static final String TAG_PREFERENCE 				= "Preference";
	public static final String TAG_CLIENT_BIND 				= "Client";
	public static final String TAG_CANDIDATE 					= "Candidate";
	public static final String TAG_PROVIDER 					= "Provider";
	public static final String TAG_NFC 						= "Nfc";

	public static final String SCHEME_TCP						="ips";
	
	public static final boolean USE_SHAREDLIB				=Friend.USE_SHAREDLIB;
	public static final String SHARED_LIB						=Friend.SHARED_LIB;
		
	public static final int BINDING_TIMEOUT_WAIT				=10000; // FIXME: remove
	public static final int BINDING_NB_RETRY					=3;
	
	public static final long COOKIE_NO						=0;
	public static final long COOKIE_EXCEPTION					=-1;
	public static final long COOKIE_SECURITY					=-2;
	
	/** Buffer size for down load */
	public static final int BUFFER_SIZE_FOR_DOWNLOAD 			= 1024 * 10;

	/** Patch parcel for compatibility between different versions of Android */
	public static final boolean UPDATE_PARCEL 				=true;

    /** Delay to discover others remote androids. */
	public static final long TIME_MAX_TO_DISCOVER				=20000L;

	/** Use ethernet */
	public static final boolean ETHERNET						=true;
    /** Use only IP v4 */
    public static final boolean ETHERNET_ONLY_IPV4 			=true; // FIXME 

    /** Order uris with IPV4 before IPV6 */
	public static final boolean ETHERNET_IPV4_FIRST 			=false;

	/** Use Mobile network */
	public static final boolean WAN 							=false;

	/** Check security. */
	public static final boolean SECURITY 					=true;
	/** Implementation name for TLS */
	public static final String TLS_IMPLEMENTATION_ALGORITHM	="TLS";
	/** Hask algorithm. */
    public static final String HASH_ALGORITHM					="SHA-256";
    /** Secure random algorithm. */
    public static final String SECURE_RANDOM_ALGORITHM		="SHA1PRNG";
    /** Signature algorithm. */
    public static final String SIGNATURE_ALGORITHM			="SHA256WithRSAEncryption";
    /** Keypair algorithm. */
    public static final String KEYPAIR_ALGORITHM				="RSA";
    /** Cipher algorithm. */
    public static final String CIPHER_ALGORITHM				="RSA/ECB/PKCS1Padding";

	/** Prefix for log */
	public static final String PREFIX_LOG						='['+Build.MODEL+"] ";

	/** Timeout for ping remote binder. */
	public static final long TIMEOUT_PING_BINDER				=5000L; // second

	/** Timeout for connect to remote android. */
	public static final long TIMEOUT_CONNECT_WIFI				=5000L; // FIXME second
    /** Flush current data before close the socket. */
    public static final boolean ETHERNET_SO_LINGER			=true; 	// Flush last paquet before close socket
    /** Timeout to flush the last datas. */
    public static final int ETHERNET_SO_LINGER_TIMEOUT		=2000; // Delay for flush last packets
	/** Timeout to detect if binder is alive. */
	public static final long TIMEOUT_IS_BINDER_ALIVE			=1000L; // FIXME second
	/** Timeout to inform the finalize of binder. */
	public static final long TIMEOUT_FINALIZE					=2000L; // FIXME second
	/** TrafficsStats tag. */
	public static final int SOCKET_TAG						=0xCAFE;
	
	/** Timeout for accept a pairing chalenge. */
	public static final long TIMEOUT_PAIRING_ASK_CHALENGE 	=30000L; // 30s

	/** Frequency of probe messages. */
	public static final int PROBE_INTERVAL_MS 				=6000; // FIXME: Probe interval

	/** Number of probe sent */
	public static final int PROBE_SENT 						=2; // FIXME: Probe number

	public static final byte[] NDEF_MIME_TYPE					="application/org.droid2droid".getBytes();//Charset.forName("US-ASCII"));
	
	public static final boolean HACK_DEAD_LOCK				=false; // TODO: detect dead lock ?
	
    /** Timeout before accept anonymouse (after expose NFC or QRCode) */
	public static final long 	TIMEOUT_ACCEPT_ANONYMOUS		=((D) ? 3 : 1)*60L*1000L; // 1m

}
