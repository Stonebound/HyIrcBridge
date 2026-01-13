package net.stonebound.hyircbridge.genericircbot;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/* package */ final class IRCThread extends Thread {
    private final AbstractIRCBot bot;

    IRCThread(AbstractIRCBot genericIRCBot) {
        this.bot = genericIRCBot;
    }

    private static final String ACT_START = "\u0001ACTION ";

    @Override
    public void run() {
        try {
            this.bot.logMessage("GenericIRCBot IRC thread starting up");
            innerRun();
        } finally {
            this.bot.logMessage("GenericIRCBot IRC thread exiting");
        }
    }

    private void innerRun() {
        while (this.bot.isConnecting()) {
            this.bot.logMessage("GenericIRCBot connecting...");
            try (Socket s = this.bot.makeSocket(this.bot.socketAddr, this.bot.useSsl);
                 InputStream is = s.getInputStream();
                 OutputStream os = s.getOutputStream();) {

                if (os == null)
                    throw new IllegalStateException("OutputStream from socket must not be null");
                this.bot.setSocket(s, os);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    new RuntimeException("interrupted sleep", ie).printStackTrace();
                    break;
                }
                this.bot.logMessage("GenericIRCBot Sending pass-nick-user");
                final String pw = this.bot.servPassword;
                if (pw != null && pw.length() > 0)
                    os.write(("PASS " + this.bot.servPassword + "\r\n").getBytes(StandardCharsets.UTF_8));
                os.write(("USER " + this.bot.info.getIdent() + " * * :" + this.bot.info.getGecos() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
                os.write(("NICK " + this.bot.info.getNickname() + "\r\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
                this.bot.logMessage("GenericIRCBot sent pass-nick-user");
                synchronized (this.bot.lock) {
                    this.bot.setConnected(true);
                }
                this.bot.logMessage("GenericIRCBot connected.");
                this.bot.onConnect();

                BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

                String line;
                while (null != (line = in.readLine())) {
                    try {
                        if (line.matches("^:[^ ]+ 372 .*")) {
                            // MOTD, ignore
                        } else {
                            this.bot.logMessage("[RAW] < " + line);
                        }
                        handleLine(line);
                    } catch (IndexOutOfBoundsException | FormatFieldNotFoundException e) {
                        this.bot.logMessage("GenericIRCBot had trouble parsing a message: " + line);
                        e.printStackTrace();
                    }

                }
                this.bot.connectionLost();

            } catch (IOException e) {
                if (isConnclosedException(e)) {
                    this.bot.logMessage("GenericIRCBot lost connection: " + e);
                    this.bot.connectionLost();
                    // socket is now closed, ignore and retry
                } else {
                    // something went wrong
                    this.bot.logMessage("GenericIRCBot encountered IO exception " + e);
                    // e.printStackTrace();
                    throw new RuntimeException("unexpected IO exception", e);
                }
            }
            try {
                if (this.bot.isConnecting()) {
                    this.bot.logMessage("GenericIRCBot reconnecting in 5 seconds...");
                    Thread.sleep(5000);
                    this.bot.logMessage("GenericIRCBot reconnecting.");
                } else {
                    break;
                }
            } catch (InterruptedException ie) {
                this.bot.logMessage("GenericIRCBot interrupted. Quitting main loop.");
                break;
            }
        }
    }

    private void handleLine(String line) throws FormatFieldNotFoundException {
        if (line.matches("^:[^ ]+ PRIVMSG #[^ ]* :.*")) {
            String[] parts = line.split(" ", 4);
            String nick = parts[0].substring(1).split("!")[0];
            String chan = parts[2];
            String msg = parts[3].substring(1);
            if (chan == null || nick == null || msg == null)
                throw new FormatFieldNotFoundException(line);
            if (msg.startsWith(ACT_START)) {
                final String action = msg.substring(ACT_START.length(), msg.length() - 1);
                if (action != null)
                    this.bot.onAction(chan, nick, action);
            } else {
                this.bot.onMessage(chan, nick, msg);
            }
        } else if (line.matches("^:[^ ]+ KICK #[^ ]* [^ ]+ :.*")) {
            String[] parts = line.split(" ", 5);
            String nick = parts[0].substring(1).split("!")[0];
            String chan = parts[2];
            String victim = parts[3];
            String msg = parts[4].substring(1);
            if (chan == null || nick == null || victim == null || msg == null)
                throw new FormatFieldNotFoundException(line);
            this.bot.onKick(chan, nick, victim, msg);
        } else if (line.matches("^:[^ ]+ JOIN #[^ ]*")) {
            String[] parts = line.split(" ", 3);
            String nick = parts[0].substring(1).split("!")[0];
            String chan = parts[2];
            if (chan == null || nick == null)
                throw new FormatFieldNotFoundException(line);
            this.bot.onJoin(chan, nick);
        } else if (line.matches("^:[^ ]+ NICK [^ ]+")) {
            String[] parts = line.split(" ", 3);
            String nick = parts[0].substring(1).split("!")[0];
            String newnick = stripColons(parts[2]);
            if (newnick == null || nick == null)
                throw new FormatFieldNotFoundException(line);
            this.bot.onNickChange(nick, newnick);
        } else if (line.matches("^:[^ ]+ PART #[^ ]*( :.*)?")) {
            String[] parts = line.split(" ", 4);
            String nick = parts[0].substring(1).split("!")[0];
            String chan = parts[2];
            String reason = parts.length == 4 ? parts[3] : "";
            if (chan == null || nick == null)
                throw new FormatFieldNotFoundException(line);
            this.bot.onPart(chan, nick, reason);
        } else if (line.matches("^:[^ ]+ QUIT :.*")) {
            String[] parts = line.split(" ", 3);
            String nick = parts[0].substring(1).split("!")[0];
            String msg = parts.length == 3 ? parts[2].substring(1) : "";
            if (nick == null || msg == null)
                throw new FormatFieldNotFoundException(line);
            this.bot.onQuit(nick, msg);
        } else if (line.matches("^PING.*")) {
            char[] pingpong = line.toCharArray();
            if (pingpong[1] != 'I') {
                this.bot.logMessage("Something, somewhere, went terribly wrong when replying to a PING message.");
            } else {
                pingpong[1] = 'O';
                this.bot.sendRawLine(new String(pingpong));
            }
        } else if (line.matches("^:[^ ]+ 001 .*")) {
            this.bot.onNumeric001();
        }
    }

    private static boolean isConnclosedException(IOException e) {
        if (e instanceof SocketException || e instanceof EOFException)
            return true;
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof IOException)
            return isConnclosedException((IOException) cause); // recurse
        return false;
    }

    private static String stripColons(String value) {
        return value == null ? null : value.replace(":", "");
    }

    private static class FormatFieldNotFoundException extends Exception {
        private static final long serialVersionUID = -4094934855639142270L;

        public FormatFieldNotFoundException(String line) {
            super("Parsing line: " + line);
        }
    }
}