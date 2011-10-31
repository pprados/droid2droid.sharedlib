package org.remoteandroid.internal.socket.ip;

import static org.remoteandroid.internal.Constants.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.remoteandroid.RemoteAndroidManager;
import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.socket.BossSocketSender;
import org.remoteandroid.internal.socket.DownstreamHandler;

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
   
    NetworkSocketBossSender(Uri uri,DownstreamHandler handler) throws UnknownHostException, IOException
	{
    	mId=sId.incrementAndGet();
    	mHandler=handler;
    	mHost=uri.getHost();
    	if (mHost.startsWith("[")) // Detect IPV6 format
    	{
    		String uriv6=uri.toString();
    		int i=uriv6.indexOf('[');
    		int j=uriv6.indexOf(']');
    		mHost=uriv6.substring(i+1,j);
    		if (uriv6.charAt(j+1)==':')
    		{
    			String port;
        		port=uriv6.substring(j+2);
        		int k=port.indexOf('/');
    			if (k!=-1) port=port.substring(0,k);
    			mPort=Integer.parseInt(port);
    		}
    	}
    	else
    		mPort=uri.getPort();
    	if (mPort==-1)
    		mPort=RemoteAndroidManager.DEFAULT_PORT;
    	Socket socket=new Socket(mHost,mPort);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setReuseAddress(true);
        socket.setTrafficClass(4);
        socket.setPerformancePreferences(2, 3, 1);
        mChannel=new NetworkSocketChannel(socket);
		
	}
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
			mChannel.close();
		}
		catch (IOException e)
		{
			// Ignore
		}
       	mThreadW.interrupt();
       	mThreadW=null;
       	mThreadR.interrupt();
       	mThreadR=null;
    }
    
    @Override
    public void start()
    {
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
					if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
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
					if (E) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
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
					if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed",e);
					mHandler.channelDisconnected(e);
					return;
				}
				catch (IOException e)
				{
					if (D) Log.e(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
					return;
				}
    		}
    	}
    }
   
}
