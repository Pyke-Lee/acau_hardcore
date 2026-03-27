package kr.pyke.acau_hardcore.network.payload.s2c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.randombox.BoxDefinition;
import kr.pyke.acau_hardcore.data.randombox.BoxRegistry;
import kr.pyke.acau_hardcore.data.randombox.BoxReward;
import kr.pyke.acau_hardcore.data.randombox.ClientBoxRegistry;
import kr.pyke.acau_hardcore.type.BOX_MESSAGE_TYPE;
import kr.pyke.acau_hardcore.type.BOX_RARITY;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record S2C_SyncBoxRegistryPayload(String json) implements CustomPacketPayload {
    public static final Type<S2C_SyncBoxRegistryPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "s2c_sync_box_registry"));

    public static final StreamCodec<FriendlyByteBuf, S2C_SyncBoxRegistryPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, S2C_SyncBoxRegistryPayload::json,
        S2C_SyncBoxRegistryPayload::new
    );

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return ID; }

    public static S2C_SyncBoxRegistryPayload fromRegistry() {
        JsonArray array = new JsonArray();

        for (BoxDefinition def : BoxRegistry.getAll()) {
            JsonObject boxJson = new JsonObject();
            boxJson.addProperty("id", def.id());
            boxJson.addProperty("display_name", def.displayName());
            boxJson.addProperty("model_data", def.modelData());

            JsonArray rewardsJson = getJsonElements(def);
            boxJson.add("rewards", rewardsJson);
            array.add(boxJson);
        }

        return new S2C_SyncBoxRegistryPayload(GSON.toJson(array));
    }

    private static @NonNull JsonArray getJsonElements(BoxDefinition def) {
        JsonArray rewardsJson = new JsonArray();
        for (BoxReward reward : def.rewards()) {
            JsonObject rewardJson = new JsonObject();
            rewardJson.addProperty("item", reward.item());
            rewardJson.addProperty("count", reward.count());
            if (reward.nbt() != null) {
                rewardJson.addProperty("nbt", reward.nbt());
            }
            if (reward.customName() != null) {
                rewardJson.addProperty("custom_name", reward.customName());
            }
            rewardJson.addProperty("weight", reward.weight());
            rewardJson.addProperty("rarity", reward.rarity().getKey());
            rewardsJson.add(rewardJson);
        }
        return rewardsJson;
    }

    public static List<BoxDefinition> parseJson(String json) {
        List<BoxDefinition> result = new ArrayList<>();
        JsonArray array = GSON.fromJson(json, JsonArray.class);

        for (var element : array) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            String displayName = obj.has("display_name") ? obj.get("display_name").getAsString() : id;
            int modelData = obj.has("model_data") ? obj.get("model_data").getAsInt() : 0;

            List<BoxReward> rewards = new ArrayList<>();
            for (var re : obj.getAsJsonArray("rewards")) {
                JsonObject rewardJson = re.getAsJsonObject();
                rewards.add(new BoxReward(
                    rewardJson.get("item").getAsString(),
                    rewardJson.has("count") ? rewardJson.get("count").getAsInt() : 1,
                    rewardJson.has("nbt") ? rewardJson.get("nbt").getAsString() : null,
                    rewardJson.has("custom_name") ? rewardJson.get("custom_name").getAsString() : null,
                    rewardJson.has("weight") ? rewardJson.get("weight").getAsInt() : 1,
                    rewardJson.has("rarity") ? BOX_RARITY.byKey(rewardJson.get("rarity").getAsString()) : BOX_RARITY.COMMON, null, BOX_MESSAGE_TYPE.PRIVATE, ""
                ));
            }

            result.add(new BoxDefinition(id, displayName, modelData, null, List.copyOf(rewards)));
        }

        return result;
    }
}