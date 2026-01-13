package net.stonebound.hyircbridge;

import net.stonebound.hyircbridge.config.HyIrcBridgeConfig;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.Message;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.HashMap;


public class HyIrcBridge extends JavaPlugin {

    public static Config<HyIrcBridgeConfig> CONFIG;
    public static HashMap<String, World> WORLDS = new HashMap<>();
    public static GameEventHandler eventHandler;
    private BridgeIrcBot bot;

    public HyIrcBridge(@NonNullDecl JavaPluginInit init) {
        super(init);
        CONFIG = this.withConfig("HyIrcBridge", HyIrcBridgeConfig.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();
        CONFIG.save();

        eventHandler = new GameEventHandler(this);
        bot = new BridgeIrcBot(this);
        bot.run();

        this.getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, eventHandler::serverChat);
        this.getEventRegistry().register(PlayerConnectEvent.class, eventHandler::playerConnect);
        this.getEventRegistry().register(PlayerDisconnectEvent.class, eventHandler::playerDisconnect);

        this.getEntityStoreRegistry().registerSystem(new GameEventHandler.KillFeedSystem(eventHandler));
    }

    void sendToGame(Message line) {
        Universe universe = Universe.get();
        if (universe != null) {
            universe.sendMessage(line);
        }
    }

    void sendToIrc(String line) {
        if (bot != null) {
            bot.sendMessage(CONFIG.get().getChannel(), line);
        }
    }
}