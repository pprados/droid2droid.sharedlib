package org.remoteandroid.internal.socket;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.remoteandroid.internal.Messages.Msg;

import com.google.protobuf.MessageLite;

public abstract class Channel implements Closeable
{
    public static final MessageLite sPrototype=Msg.getDefaultInstance();
    public abstract void write(Msg msg) throws IOException;
    public abstract Msg read() throws IOException;
	public abstract void close() throws IOException;
	public abstract boolean isConnected();
	public abstract boolean isBluetoothSecure();

	public static final int INITIAL_BUFSIZE=1024;
	private WeakReference<byte[]> mBuf=new WeakReference<byte[]>(new byte[INITIAL_BUFSIZE]);
	
	public static void writeMsg(MessageLite msg, OutputStream out) throws IOException
	{
		byte[] data=msg.toByteArray();
		final int l=data.length;
		byte[] len=new byte[] 
		        {
	                (byte)(l >>> 24),
	                (byte)(l >>> 16),
	                (byte)(l >>> 8),
	                (byte)l
	            };
		out.write(len);
		out.write(data);
		out.flush();
	}
	
	public Msg readMsg(InputStream in) throws IOException, EOFException
	{
		byte[] buf=mBuf.get();
		if (buf==null)
		{
			mBuf=new WeakReference<byte[]>(buf=new byte[INITIAL_BUFSIZE]);
		}
		int ss=in.read(buf,0,4);
		if (ss!=4)
		{
			EOFException e=new EOFException("Communication closed");
			throw e;
		}
		int length=(buf[0] << 24)
        	+ ((buf[1] & 0xFF) << 16)
        	+ ((buf[2] & 0xFF) << 8)
        	+ (buf[3] & 0xFF);
		if (length+4>buf.length)
		{
			mBuf=new WeakReference<byte[]>(buf=new byte[length+4]);
		}
		int pos=4;
		int l=length;
		do
		{
			int s=in.read(buf,pos,l);
			if (s==-1)
				throw new EOFException();
			pos+=s;
			l-=s;
		} while (l>0);
		// Read protobuf message
	    Msg msg=(Msg)Channel.sPrototype.newBuilderForType().mergeFrom(buf, 4, length).build();
	    return msg;
	}
	
}

