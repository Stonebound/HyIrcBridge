package net.stonebound.hyircbridge.genericircbot;

import javax.net.SocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * IRC bot. Implementations can override the methods from {@link IRCMethods} to
 * get notified of IRC events.
 * <p>
 * <strong>Note on thread safety:</strong> This IRC bot uses its own
 * {@link Thread} for incoming events. The implementing class may have to
 * synchronize overridden methods.
 */
public abstract class AbstractIRCBot extends IRCMethods implements Runnable {

    /* == Settings == */
    /* package */ final InetSocketAddress socketAddr;
    /* package */ final boolean useSsl;

    /* package */ final IRCConnectionInfo info;
    /* package */ final String servPassword;

    /* == State == */
    private volatile boolean connecting = true;
    private volatile Socket clientSocket = null;
    private volatile OutputStream out = null;
    private volatile boolean connected = false;
    private Thread ircThread;
    final Object lock = new Object();

    protected AbstractIRCBot(InetSocketAddress socketAddr, boolean useSsl, IRCConnectionInfo info,
                             String servPassword) {
        this.socketAddr = socketAddr;
        this.useSsl = useSsl;
        this.info = info;
        this.servPassword = servPassword;
    }

    /* == API METHODS == */

    /**
     * joins the given channel, channel should probably start with "#"
     */
    protected void joinChannel(String c) {
        sendRawLine("JOIN " + c);
    }

    /**
     * Sends a given <code>message</code> to a <code>channel</code> (PRIVMSG)
     *
     * @param channel IRC chnanel, should probably start with "#"
     * @param message IRC message, the caller needs to ensure this is not too long.
     */
    protected void sendMessage(String channel, String message) {
        sendRawLine("PRIVMSG " + channel + " :" + message);
    }

    /**
     * Sends a single raw line to IRC. The caller needs to ensure the line is not
     * too long.
     */
    protected void sendRawLine(String message) {
        String msg = message;
        // normalize line endings
        while (msg.endsWith("\n") || msg.endsWith("\r") || msg.endsWith("\0")) {
            msg = msg.substring(0, msg.length() - 1);
        }
        msg = msg.replace('\n', '?');
        msg = msg.replace('\r', '?');
        msg = msg.replace('\0', '?');
        logMessage("[RAW] > " + msg);
        msg += "\r\n";
        synchronized (this.lock) {
            try {
                final OutputStream os = this.out;
                if (os != null) {
                    os.write(msg.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            } catch (IOException e) {
                new RuntimeException("Sending raw line", e).printStackTrace();
            }
        }
    }

    /**
     * Override this to receive info-level log messages. The note on thread safety
     * applies. This message may be called from more than one thread.
     */
    protected void logMessage(String msg) {
        // NOOP
    }

    /**
     * gracefully disconnect the bot
     */
    protected void disconnect() {
        logMessage("GenericIRCBot shutting down");
        synchronized (this.lock) {
            this.connecting = false;
        }
        try {
            final Socket sock = this.clientSocket;
            if (sock != null) {
                sock.close();
            }
        } catch (IOException e) {
            new RuntimeException("Error closing socket!", e).printStackTrace();
        } finally {
            // make sure to lose all references to the socket and its streams
            this.clientSocket = null;
            this.out = null;
        }
    }

    /**
     * forcibly disconnect the bot
     */
    protected void kill() {
        if (this.connecting) {
            disconnect();
        }
        if (this.ircThread != null && this.ircThread.isAlive()) {
            logMessage("interrupting IRC thread");
            this.ircThread.interrupt();
        }
    }

    /* == END API METHODS == */

    @Override
    public final void run() {
        logMessage("GenericIRCBot starting...");
        start();
        logMessage("GenericIRCBot started.");
    }

    private void start() {
        this.ircThread = new IRCThread(this);
        this.ircThread.start();
    }

    /* package */ void connectionLost() {
        synchronized (this.lock) {
            try {
                this.out = null;
                final Socket sock = this.clientSocket;
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException e) {
                new RuntimeException("Could not close socket", e).printStackTrace();
            } finally {
                // make sure to lose all references to the socket and its streams
                this.clientSocket = null;
                this.out = null;
            }
        }
    }

    /* package */ Socket makeSocket(InetSocketAddress addr, boolean ssl) throws IOException {
        Socket sock;
        if (ssl) {
            SocketFactory sslsf = SSLSocketFactory.getDefault();
            SSLSocket sslsock = (SSLSocket) sslsf.createSocket(addr.getHostString(), addr.getPort());
            SSLParameters params = new SSLParameters();
            params.setEndpointIdentificationAlgorithm("HTTPS");
            sslsock.setSSLParameters(params);
            sock = sslsock;
        } else {
            sock = new Socket(addr.getHostString(), addr.getPort());
        }
        return sock;
    }

    /* package */ boolean isConnecting() {
        synchronized (this.lock) {
            return this.connecting;
        }
    }

    /* package */ void setSocket(Socket s, OutputStream os) {
        synchronized (this.lock) {
            this.clientSocket = s;
            this.out = os;
        }
    }

    /* package */ boolean isConnected() {
        return this.connected;
    }

    /* package */ void setConnected(boolean connected) {
        this.connected = connected;
    }

}