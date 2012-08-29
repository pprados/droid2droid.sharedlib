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
package org.droid2droid.internal.socket.ip;

import static org.droid2droid.Droid2DroidManager.DEFAULT_PORT;
import static org.droid2droid.internal.Constants.D;
import static org.droid2droid.internal.Constants.ETHERNET_SO_LINGER;
import static org.droid2droid.internal.Constants.ETHERNET_SO_LINGER_TIMEOUT;
import static org.droid2droid.internal.Constants.I;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.SECURE_RANDOM_ALGORITHM;
import static org.droid2droid.internal.Constants.TAG_CLIENT_BIND;
import static org.droid2droid.internal.Constants.TAG_SECURITY;
import static org.droid2droid.internal.Constants.TIMEOUT_CONNECT_WIFI;
import static org.droid2droid.internal.Constants.TLS_IMPLEMENTATION_ALGORITHM;
import static org.droid2droid.internal.Constants.V;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

import org.droid2droid.internal.Messages.Msg;
import org.droid2droid.internal.Tools;
import org.droid2droid.internal.socket.BossSocketSender;
import org.droid2droid.internal.socket.DownstreamHandler;

import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.util.Log;


public final class NetworkSocketBossSender implements BossSocketSender
{
	private static AtomicInteger sId=new AtomicInteger();
    private static SecureRandom sRandom;
    static
    {
    	try
		{
			sRandom=SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new InternalError(e.getMessage());
		}
    }

	private int mId;
	private NetworkSocketChannel mChannel;
	private String mHost;
	private int mPort;
    private PublicKey mPeerPublicKey;
    private String mPeerUUID;
    
    private Thread mThreadW;
    private Thread mThreadR;
    
    private DownstreamHandler mHandler;

    private final LinkedBlockingQueue<Msg> mMsgs=new LinkedBlockingQueue<Msg>();
    private static final Pattern sPatternDN=Pattern.compile("CN=([0-9a-f-]+)");
   
    NetworkSocketBossSender(Context context,Uri uri,DownstreamHandler handler) throws UnknownHostException, IOException
	{
    	mId=sId.incrementAndGet();
    	mHandler=handler;
    	mHost=Tools.uriGetHostIPV6(uri);
    	mPort=Tools.uriGetPortIPV6(uri);
    	// TODO: Detecter si je suis sur GSM, et dans ce cas, ne permettre que les connexions vers IP globale. Permet d'optimiser si BT+GSM mais pas WIFI et connexion sur WIFI+BT
    	if (InetAddress.getByName(mHost).isSiteLocalAddress() && ! Tools.isLocalNetwork(context))
    	{
    		// I try to connect to local address, but i'm not in local network.
    		return;
    	}
    	if (mPort==-1)
    		mPort=DEFAULT_PORT;
    	// TODO: ne pas tenter si moi en global network et target en local network. Mais en vérifier toutes les ip
    	//if (Trusted.isLocalNetwork(Appl))
    	
    	try
    	{
			SSLSocket socket = createSocket(InetAddress.getByName(mHost),mPort);
			try
			{
				Certificate[] certificatesChaine=socket.getSession().getPeerCertificates(); // FIXME: Bug with Jelly Bean
				String name=socket.getSession().getPeerPrincipal().getName();
				Matcher m = sPatternDN.matcher(name);
				if (m.find())
				{
					mPeerUUID=m.group(1);
				}
				mPeerPublicKey=certificatesChaine[0].getPublicKey();
			}
			catch (SSLPeerUnverifiedException e)
			{
				// Ignore. Client without authentification
			}
	    	socket.setSoLinger(ETHERNET_SO_LINGER, ETHERNET_SO_LINGER_TIMEOUT);
	        socket.setKeepAlive(true);
	        socket.setTcpNoDelay(true);
	        socket.setReuseAddress(true);
	        socket.setPerformancePreferences(2, 3, 1);
	        mChannel=new NetworkSocketChannel(socket);
    	}
    	catch (NoSuchAlgorithmException e)
    	{
    		throw new Error(e);
    	}
		catch (KeyManagementException e)
		{
    		throw new Error(e);
		}
	}

    private static KeyManager[] sKeyManagers;
    private static final X509TrustManager[] sX509TrustManager=
    		new X509TrustManager[]
			{ 
				new X509TrustManager()
				{
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
					{
						if (V) Log.v(TAG_SECURITY,"check client trusted");
					}
		
					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
					{
						if (V) Log.v(TAG_SECURITY,"check server trusted");
					}
		
					@Override
					public X509Certificate[] getAcceptedIssuers()
					{
						return new X509Certificate[0];
					}
				} 
			};
    public static void setKeyManagers(KeyManager[] keymanagers)
    {
    	sKeyManagers=keymanagers;
    }
    public PublicKey getPeerPublicKey()
    {
    	return mPeerPublicKey;
    }
    public String getPeerUUID()
    {
    	return mPeerUUID;
    }
    
	public static SSLSocket createSocket(InetAddress host,int port) throws NoSuchAlgorithmException, KeyManagementException, IOException
	{
		SSLContext sslcontext = SSLContext.getInstance(TLS_IMPLEMENTATION_ALGORITHM);
		sslcontext.init(
			sKeyManagers, 
			sX509TrustManager, 
			sRandom);
		
		SSLSocket socket=(SSLSocket)sslcontext.getSocketFactory().createSocket();
		socket.connect(new InetSocketAddress(host,port),(int)TIMEOUT_CONNECT_WIFI); // Note: for ipv6 linkLocalAddress, we must select the interface :-(
		return socket;
	}
    
    // For ipv6 local address. Trouver l'interface
//    InetAddress getMyInterface()
//    {
//    	try
//    	{
//			for (Enumeration<NetworkInterface> networks=NetworkInterface.getNetworkInterfaces();networks.hasMoreElements();)
//			{
//				NetworkInterface network=networks.nextElement();
//				for (Enumeration<InetAddress> addrs=network.getInetAddresses();addrs.hasMoreElements();)
//				{
//					InetAddress add=(InetAddress)addrs.nextElement();
//					if (network.getName().startsWith("sit")) // vpn ?
//						continue;
//					if (network.getName().startsWith("dummy")) // ipv6 in ipv4
//						continue;
//					if (add.isLoopbackAddress())
//						continue;
//					return add;
//				}
//			}
//    	} catch (Exception e)
//    	{
//    		// FIXME
//    	}
//    	return null; // FIXME
//    }
    
    
    @Override
	public void pushMessage(Msg msg)
    {
    	mMsgs.add(msg);
    }
    @Override
	public boolean isConnected()
    {
    	return mChannel.isConnected();
    }
    
    @Override
    public void close()
    {
    	// FIXME: unsuported operation with SSL
//    	try
//		{
//			if (mChannel!=null)
//				mChannel.close();
//		}
//		catch (IOException e)
//		{
//			// Ignore
//		}
       	if (mThreadW!=null)
       	{
       		mThreadW.interrupt();
       		mThreadW=null;
       	}
       	if (mThreadR!=null)
       	{
       		mThreadR.interrupt();
       		mThreadR=null;
       	}
    }
    
    @Override
    public void start()
    {
    	if (mChannel==null)
    		return;
    	if (mThreadW!=null)
    	{
    		Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Allready started");
    		return;
    	}
    	mThreadW=new Thread(new WriteThread(),"Write Net Socket #"+mId);
    	mThreadW.setDaemon(true);
    	mThreadW.start();
    	mThreadR=new Thread(new ReadThread(),"Read Net Socket #"+mId);
    	mThreadR.setDaemon(true);
    	mThreadR.start();
    }
    
    class WriteThread implements Runnable
    {
	    @Override
	    public void run()
	    {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	    	for (;;)
	    	{
	    		if (Thread.interrupted())
	    			return;
	    		try
				{
	        		Msg msg=mMsgs.take();
					mChannel.write(msg);
				}
				catch (EOFException e)
				{
					if (I && !V) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed",e);
					mHandler.channelDisconnected(e);
				}
				catch (IOException e)
				{
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
				}
				catch (InterruptedException e)
				{
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
					return;
				}
	    	}
	    }
    }
    class ReadThread implements Runnable
    {
    	@Override
    	public void run()
    	{
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    		for (;;)
    		{
				try
				{
		    		if (Thread.interrupted())
		    			return;
	    			Msg msg = mChannel.read();
	    			mHandler.messageReceived(msg);
				}
				catch (EOFException e)
				{
					if (I && !V) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
					//if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed",e);
					mHandler.channelDisconnected(e);
					return;
				}
				catch (IOException e)
				{
					if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
					return;
				}
    		}
    	}
    }
   
}
