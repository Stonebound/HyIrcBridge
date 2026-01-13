package net.stonebound.hyircbridge;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.MessageUtil;

import java.util.concurrent.CompletableFuture;

public class GameEventHandler {

    private static final Message FORMAT_CHAT_TO_IRC = Message.translation("commands.hyircbridge.chatToIrc");
    public static final Message FORMAT_GAME_LOGIN = Message.translation("commands.hyircbridge.gameLogin");
    public static final Message FORMAT_GAME_LOGOUT = Message.translation("commands.hyircbridge.gameLogout");
    public static final Message FORMAT_GAME_DEATH = Message.translation("commands.hyircbridge.death");
    public static final Message FORMAT_GAME_LOGOUT_STOP = Message.translation("commands.hyircbridge.gameLogoutStop");
    private final HyIrcBridge bridge;

    public GameEventHandler(HyIrcBridge bridge) {
        this.bridge = bridge;
    }

    public CompletableFuture<PlayerChatEvent> serverChat(CompletableFuture<PlayerChatEvent> future) {
        return future.thenApply(event -> {
            if (event.isCancelled()) {
                return event;
            }

            String playerName = event.getSender().getUsername();
            String message = event.getContent();
            this.bridge.sendToIrc(MessageUtil.toAnsiString(FORMAT_CHAT_TO_IRC.param("username", playerName).param("message", message)).toString());
            return event;
        });
    }

    public void playerConnect(PlayerConnectEvent event) {
        bridge.sendToIrc(MessageUtil.toAnsiString(FORMAT_GAME_LOGIN.param("username", event.getPlayerRef().getUsername())).toString());
    }

    public void playerDisconnect(PlayerDisconnectEvent event) {
        bridge.sendToIrc(MessageUtil.toAnsiString(FORMAT_GAME_LOGOUT.param("username", event.getPlayerRef().getUsername())).toString());
    }

    public void onKillFeed(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, KillFeedEvent.Display event) {
        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
        if (playerRef != null) {
            Message deathMessage = event.getDamage().getDeathMessage(victimRef, store);
            String formattedDeath = MessageUtil.toAnsiString(
                FORMAT_GAME_DEATH.param("username", playerRef.getUsername())
                    .param("reason", MessageUtil.toAnsiString(deathMessage).toString())
            ).toString();
            bridge.sendToIrc(formattedDeath);
            this.bridge.sendToIrc(formattedDeath);
        }
    }

    public static class KillFeedSystem extends EntityEventSystem<EntityStore, KillFeedEvent.Display> {
        private final GameEventHandler handler;

        public KillFeedSystem(GameEventHandler handler) {
            super(KillFeedEvent.Display.class);
            this.handler = handler;
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, KillFeedEvent.Display event) {
            handler.onKillFeed(index, archetypeChunk, store, event);
        }

        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
