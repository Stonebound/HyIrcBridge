package net.stonebound.hyircbridge;

import java.net.InetSocketAddress;

import com.hypixel.hytale.server.core.Message;

import net.stonebound.hyircbridge.genericircbot.AbstractIRCBot;
import net.stonebound.hyircbridge.genericircbot.IRCConnectionInfo;

public class BridgeIrcBot extends AbstractIRCBot {
    private static final Message FORMAT_IRC_TO_GAME = Message.translation("commands.hyircbridge.ircToGame");
    private static final String MIRC_COLOR_CODE_REGEX = "(?i)\u0003([0-9]{1,2}(,[0-9]{1,2})?)?";

    private HyIrcBridge bridge;

    BridgeIrcBot(HyIrcBridge bridge) {
        super(
                new InetSocketAddress(HyIrcBridge.CONFIG.get().getHostname(), HyIrcBridge.CONFIG.get().getPort()),
                HyIrcBridge.CONFIG.get().isTls(),
                new IRCConnectionInfo(
                        !HyIrcBridge.CONFIG.get().getNick().contains("(rnd)") ? HyIrcBridge.CONFIG.get().getNick() : HyIrcBridge.CONFIG.get().getNick().replace("(rnd)", String.valueOf((int) (Math.random() * 100000))),
                        HyIrcBridge.CONFIG.get().getUsername(),
                        HyIrcBridge.CONFIG.get().getRealname()
                ),
                HyIrcBridge.CONFIG.get().getPassword()
        );
        this.bridge = bridge;
    }

    @Override
    protected void logMessage(String msg) {
        this.bridge.getLogger().atInfo().log(msg);
    }

    @Override
    protected void onMessage(String channel, String sender, String message) {

        if (HyIrcBridge.CONFIG.get().isGameFormatting()) {
            message = message.replaceAll(MIRC_COLOR_CODE_REGEX, "");
        }
        if (sender.equals("bungee")) {
            toGame(Message.raw(message));
        } else if (sender.equals("discord")) {
            toGame(FORMAT_IRC_TO_GAME.param("username", sender).param("message", message));
        } else {
            toGame(FORMAT_IRC_TO_GAME.param("username", sender).param("message", message));
        }
    }

    @Override
    protected void onNumeric001() {
        joinChannel(HyIrcBridge.CONFIG.get().getChannel());
    }

    /**
     * {@inheritDoc}
     */ // re-declare protected, publish method for package
    @Override
    protected void disconnect() {
        super.disconnect();
    }

    /**
     * {@inheritDoc}
     */ // re-declare protected, publish method for package
    @Override
    protected void kill() {
        super.kill();
    }

    /**
     * {@inheritDoc}
     */ // re-declare protected, publish method for package
    @Override
    protected void sendMessage(String channel, String message) {
        super.sendMessage(channel, message);
    }

    private void toGame(Message s) {
        this.bridge.sendToGame(s);
    }
}
