package net.stonebound.hyircbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HyIrcBridgeConfig {

    private boolean IrcFormatting = true;
    private boolean GameFormatting = true;
    private String Nick = "bridgebot";
    private String Password = "";
    private String Hostname = "localhost";
    private int Port = 6667;
    private String Channel = "#general";
    private boolean Tls = false;
    private String Username = "bridgebot";
    private String Realname = "SimpleIRCBridge";
    private boolean Timestop = true;

    public static final BuilderCodec<HyIrcBridgeConfig> CODEC = BuilderCodec.builder(HyIrcBridgeConfig.class, HyIrcBridgeConfig::new)
            .append(new KeyedCodec<Boolean>("IrcFormatting", Codec.BOOLEAN),
                (config, value, extraInfo) -> config.IrcFormatting = value,
                (config, extraInfo) -> config.IrcFormatting).add()
            .append(new KeyedCodec<Boolean>("GameFormatting", Codec.BOOLEAN),
                (config, value, extraInfo) -> config.GameFormatting = value,
                (config, extraInfo) -> config.GameFormatting).add()
            .append(new KeyedCodec<String>("Nick", Codec.STRING),
                (config, value, extraInfo) -> config.Nick = value,
                (config, extraInfo) -> config.Nick).add()
            .append(new KeyedCodec<String>("Password", Codec.STRING),
                (config, value, extraInfo) -> config.Password = value,
                (config, extraInfo) -> config.Password).add()
            .append(new KeyedCodec<String>("Hostname", Codec.STRING),
                (config, value, extraInfo) -> config.Hostname = value,
                (config, extraInfo) -> config.Hostname).add()
            .append(new KeyedCodec<Integer>("Port", Codec.INTEGER),
                (config, value, extraInfo) -> config.Port = value,
                (config, extraInfo) -> config.Port).add()
            .append(new KeyedCodec<String>("Channel", Codec.STRING),
                (config, value, extraInfo) -> config.Channel = value,
                (config, extraInfo) -> config.Channel).add()
            .append(new KeyedCodec<Boolean>("Tls", Codec.BOOLEAN),
                (config, value, extraInfo) -> config.Tls = value,
                (config, extraInfo) -> config.Tls).add()
            .append(new KeyedCodec<String>("Username", Codec.STRING),
                (config, value, extraInfo) -> config.Username = value,
                (config, extraInfo) -> config.Username).add()
            .append(new KeyedCodec<String>("Realname", Codec.STRING),
                (config, value, extraInfo) -> config.Realname = value,
                (config, extraInfo) -> config.Realname).add()
            .append(new KeyedCodec<Boolean>("Timestop", Codec.BOOLEAN),
                (config, value, extraInfo) -> config.Timestop = value,
                (config, extraInfo) -> config.Timestop).add()
            .build();

    public HyIrcBridgeConfig() {

    }

    public boolean isIrcFormatting() {
        return IrcFormatting;
    }

    public boolean isGameFormatting() {
        return GameFormatting;
    }

    public String getNick() {
        return Nick;
    }

    public String getPassword() {
        return Password;
    }

    public String getHostname() {
        return Hostname;
    }

    public int getPort() {
        return Port;
    }

    public String getChannel() {
        return Channel;
    }

    public boolean isTls() {
        return Tls;
    }

    public String getUsername() {
        return Username;
    }

    public String getRealname() {
        return Realname;
    }

    public boolean isTimestop() {
        return Timestop;
    }
}