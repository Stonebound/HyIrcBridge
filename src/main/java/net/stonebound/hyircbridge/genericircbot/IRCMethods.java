package net.stonebound.hyircbridge.genericircbot;

/**
 * Holds the methods that an IRC bot will call when receiving messages
 * <p>
 * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
 * safety.
 */
public abstract class IRCMethods {
    /**
     * Represents a <code>PRIVMSG</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param channel channel message was sent to
     * @param sender  user who sent the message
     * @param message content of the message
     */
    protected void onMessage(String channel, String sender, String message) {
        /* can be overridden */
    }

    /**
     * Represents a <code>CTCP ACTION</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param channel channel message was sent to
     * @param sender  user who sent the message
     * @param action  content of the action
     */
    protected void onAction(String channel, String sender, String action) {
        /* can be overridden */
    }

    /**
     * Represents a <code>PART</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param channel channel sender parted from
     * @param sender  user who parted
     * @param reason  part message
     */
    protected void onPart(String channel, String sender, String reason) {
        /* can be overridden */
    }

    /**
     * Represents a <code>JOIN</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param channel channel sender joined to
     * @param sender  user who joined
     */
    protected void onJoin(String channel, String sender) {
        /* can be overridden */
    }

    /**
     * Represents a <code>QUIT</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param sender user who quit
     * @param reason quit reason
     */
    protected void onQuit(String sender, String reason) {
        /* can be overridden */
    }

    /**
     * Represents a <code>KICK</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param channel  channel where it happened
     * @param opsender OP who issued the kick
     * @param victim   user who was kicked
     * @param reason   kick reason
     */
    protected void onKick(String channel, String opsender, String victim, String reason) {
        /* can be overridden */
    }

    /**
     * Represents a <code>NICK</code> received on IRC
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     *
     * @param sender  old nickname
     * @param newnick new nickname
     */
    protected void onNickChange(String sender, String newnick) {
        /* can be overridden */
    }

    /**
     * Called when the bot has established a connection to IRC and can send
     * commands.
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     */
    protected void onConnect() {
        /* can be overridden */
    }

    /**
     * Called when the bot has established a connection to IRC and registration has
     * finished (a 001 numeric is received).
     * <p>
     * It makes sense to {@link AbstractIRCBot#joinChannel(String) join channels} in
     * implementations of this method.
     * <p>
     * <strong>Note:</strong> Refer to {@link AbstractIRCBot} for notes on thread
     * safety.
     */
    protected void onNumeric001() {
        /* can be overridden */
    }
}