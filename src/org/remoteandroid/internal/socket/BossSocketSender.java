package org.remoteandroid.internal.socket;

import java.io.Closeable;
import java.io.IOException;

import org.remoteandroid.internal.Messages.Msg;


public interface BossSocketSender extends Closeable
{
    void pushMessage(Msg msg);
    boolean isConnected();
    void start();
    void close() throws IOException;	
}

