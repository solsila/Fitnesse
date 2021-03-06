// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import fitnesse.util.MockSocket;

import java.io.IOException;
import java.net.Socket;

public class SimpleSocketDoner implements SocketDoner {
    public Socket socket;
    public boolean finished = false;

    public SimpleSocketDoner() {
        socket = new MockSocket("SimpleSocketDoner");
    }

    public SimpleSocketDoner(Socket socket) {
        this.socket = socket;
    }

    public Socket donateSocket() {
        return socket;
    }

    public void finishedWithSocket() {
        finished = true;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
