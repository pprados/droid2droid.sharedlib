package org.remoteandroid.internal.socket.ip;

import static org.remoteandroid.internal.Constants.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.Tools;
import org.remoteandroid.internal.socket.BossSocketSender;
import org.remoteandroid.internal.socket.DownstreamHandler;

import com.google.protobuf.ByteString;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Process;
import android.util.Log;


public class NetworkSocketBossSender implements BossSocketSender
{
	private static AtomicInteger sId=new AtomicInteger();

	private int mId;
	private NetworkSocketChannel mChannel;
	private String mHost;
	private int mPort;

    private Thread mThreadW;
    private Thread mThreadR;
    
    private DownstreamHandler mHandler;

    private LinkedBlockingQueue<Msg> mMsgs=new LinkedBlockingQueue<Msg>();
   
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
    		mPort=RemoteAndroidManager.DEFAULT_PORT;
    	// TODO: ne pas tanter si moi en global network et target en local network. Mais en vérifier toutes les ip
    	//if (Trusted.isLocalNetwork(Appl))
    	
    	Socket socket=new Socket();
    	socket.connect(new InetSocketAddress(mHost,mPort),(int)TIMEOUT_CONNECT); // Note: for ipv6 linkLocalAddress, we must select the interface :-(
        //FIXME socket.setSoTimeout((int)TIMEOUT_CONNECT);
    	socket.setSoLinger(ETHERNET_SO_LINGER, ETHERNET_SO_LINGER_TIMEOUT);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setReuseAddress(true);
        socket.setPerformancePreferences(2, 3, 1);
        mChannel=new NetworkSocketChannel(socket);
		
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
    
    
    public void pushMessage(Msg msg)
    {
    	mMsgs.add(msg);
    }
    public boolean isConnected()
    {
    	return mChannel.isConnected();
    }
    
    @Override
    public void close()
    {
    	try
		{
			if (mChannel!=null)
				mChannel.close();
		}
		catch (IOException e)
		{
			// Ignore
		}
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
