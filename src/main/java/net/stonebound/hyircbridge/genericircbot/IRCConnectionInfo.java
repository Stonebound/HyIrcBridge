package net.stonebound.hyircbridge.genericircbot;

/**
 * Holds the information that is passed to the IRC server for registration
 * (nickname, ident and gecos)
 */
public class IRCConnectionInfo {
    private final String nickname;
    private final String ident;
    private final String gecos;

    /**
     * @param nickname nickname to use on IRC, must be valid
     * @param ident    ident/username passed to the IRC server
     * @param gecos    gecos/realname passed to the IRC server
     */
    public IRCConnectionInfo(String nickname, String ident, String gecos) {
        if (nickname.contains(" "))
            throw new IllegalArgumentException("nickname must not contain spaces");
        if (ident.contains(" "))
            throw new IllegalArgumentException("username must not contain spaces");
        if (!nickname.matches("[a-zA-Z_\\[\\]\\\\^{}|`][a-zA-Z0-9_\\[\\]\\\\^{}|`-]*"))
            throw new IllegalArgumentException("nickname is not a valid IRC nickname");
        this.nickname = nickname;
        this.ident = ident;
        this.gecos = gecos;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getIdent() {
        return this.ident;
    }

    public String getGecos() {
        return this.gecos;
    }

}