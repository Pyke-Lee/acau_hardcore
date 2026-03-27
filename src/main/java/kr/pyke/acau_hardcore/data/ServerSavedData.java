package kr.pyke.acau_hardcore.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ServerSavedData extends SavedData {
    private static final String FILE_NAME = "server_data";

    private ResourceKey<Level> lobbyDimension;
    private Vec3 lobbyPosition;
    private float lobbyYaw;

    private ResourceKey<Level> jailDimension;
    private Vec3 jailPosition;
    private float jailYaw;

    public static final Codec<ServerSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Level.RESOURCE_KEY_CODEC.optionalFieldOf("lobby_dimension", Level.OVERWORLD)
            .forGetter(data -> data.lobbyDimension),
        Vec3.CODEC.optionalFieldOf("lobby_pos", new Vec3(0.5d, 100d, 0.5d))
            .forGetter(data -> data.lobbyPosition),
        Codec.FLOAT.optionalFieldOf("lobby_yaw", 0.f)
            .forGetter(data -> data.lobbyYaw),
        Level.RESOURCE_KEY_CODEC.optionalFieldOf("jail_dimension", Level.OVERWORLD)
            .forGetter(data -> data.jailDimension),
        Vec3.CODEC.optionalFieldOf("jail_pos", new Vec3(0.5d, 100d, 0.5d))
            .forGetter(data -> data.jailPosition),
        Codec.FLOAT.optionalFieldOf("jail_yaw", 0.f)
            .forGetter(data -> data.jailYaw)
    ).apply(instance, ServerSavedData::new));

    public static final SavedDataType<ServerSavedData> TYPE = new SavedDataType<>(FILE_NAME, ServerSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    public ServerSavedData() {
        this.lobbyDimension = Level.OVERWORLD;
        this.lobbyPosition = new Vec3(0.5d, 100d, 0.5d);
        this.lobbyYaw = 0.f;

        this.jailDimension = Level.OVERWORLD;
        this.jailPosition = new Vec3(0.5d, 100d, 0.5d);
        this.jailYaw = 0.f;
    }

    private ServerSavedData(ResourceKey<Level> lobbyDim, Vec3 lobbyPos, float lobbyYaw, ResourceKey<Level> jailDim, Vec3 jailPos, float jailYaw) {
        this.lobbyDimension = lobbyDim;
        this.lobbyPosition = lobbyPos;
        this.lobbyYaw = lobbyYaw;

        this.jailDimension = jailDim;
        this.jailPosition = jailPos;
        this.jailYaw = jailYaw;
    }

    public static ServerSavedData getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public ResourceKey<Level> getLobbyDimension() { return lobbyDimension; }
    public Vec3 getLobbyPos() { return lobbyPosition; }
    public float getLobbyYaw() { return lobbyYaw; }

    public ResourceKey<Level> getJailDimension() { return jailDimension; }
    public Vec3 getJailPos() { return jailPosition; }
    public float getJailYaw() { return jailYaw; }

    public void setLobbyPosition(ResourceKey<Level> dimension, Vec3 pos, float yaw) {
        this.lobbyDimension = dimension;
        this.lobbyPosition = pos;
        this.lobbyYaw = yaw;
        this.setDirty();
    }

    public void setJailPosition(ResourceKey<Level> dimension, Vec3 pos, float yaw) {
        this.jailDimension = dimension;
        this.jailPosition = pos;
        this.jailYaw = yaw;
        this.setDirty();
    }

    public void setLobbyPosition(ServerLevel level, Vec3 pos, float yaw) {
        setLobbyPosition(level.dimension(), pos, yaw);
    }

    public void setJailPosition(ServerLevel level, Vec3 pos, float yaw) {
        setJailPosition(level.dimension(), pos, yaw);
    }

    public TeleportTransition createLobbyTransition(MinecraftServer server, TeleportTransition.PostTeleportTransition postTransition) {
        ServerLevel targetLevel = server.getLevel(lobbyDimension);
        if (targetLevel == null) { return null; }

        return new TeleportTransition(targetLevel, lobbyPosition, Vec3.ZERO, lobbyYaw, 0.f, postTransition);
    }

    public TeleportTransition createJailTransition(MinecraftServer server, TeleportTransition.PostTeleportTransition postTransition) {
        ServerLevel targetLevel = server.getLevel(jailDimension);
        if (targetLevel == null) { return null; }

        return new TeleportTransition(targetLevel, jailPosition, Vec3.ZERO, jailYaw, 0.f, postTransition);
    }
}
