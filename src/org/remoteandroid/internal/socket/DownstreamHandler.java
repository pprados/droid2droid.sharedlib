package org.remoteandroid.internal.socket;

import org.remoteandroid.internal.Messages.Msg;

public interface DownstreamHandler
{
	public void messageReceived(Msg msg);
	public void channelDisconnected(Throwable e);
}

