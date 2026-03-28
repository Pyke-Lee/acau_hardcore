package kr.pyke.acau_hardcore.registry.component.hardcore;

import com.mojang.serialization.Codec;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.ServerSavedData;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import kr.pyke.acau_hardcore.util.Utils;
import kr.pyke.util.constants.COLOR;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HardCoreInfo implements IHardCoreInfo {
    private final Player player;

    private int deathCount;
    private long totalPlayTime;
    private long currentLiveTime;
    private boolean isStarted = false;

    private int thirstLevel = 20;
    private int hydrationTicks = 0;

    private boolean isJail = false;

    private ResourceKey<Level> prevDimension;
    private Vec3 prevPosition;
    private float prevYaw;
    private float prevPitch;

    private long currency = 0;
    private HARDCORE_TYPE hardcoreType = HARDCORE_TYPE.BEGINNER;

    private UUID housingID = null;

    private final Map<Integer, ItemStack> savedItems = new LinkedHashMap<>();

    private final List<TimerTask> timerTasks = new ArrayList<>();
    private final List<TimerTask> pendingTimerTasks = new ArrayList<>();

    public HardCoreInfo(Player player) {
        this.player = player;
    }

    @Override
    public void readData(ValueInput valueInput) {
        this.totalPlayTime = valueInput.getLongOr("TotalPlayTime", 0);
        this.currentLiveTime = valueInput.getLongOr("CurrentLiveTime", 0);
        this.deathCount = valueInput.getIntOr("DeathCount", 0);
        this.isStarted = valueInput.getBooleanOr("IsStarted", false);
        this.thirstLevel = valueInput.getIntOr("ThirstLevel", 20);
        this.hydrationTicks = valueInput.getIntOr("HydrationTicks", 0);
        this.currency = valueInput.getLongOr("Currency", 0);
        this.isJail = valueInput.getBooleanOr("IsJail", false);
        String dimKey = valueInput.getStringOr("PrevDimension", "");
        if (!dimKey.isEmpty()) {
            this.prevDimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimKey));
        }
        else {
            this.prevDimension = null;
        }
        double prevX = valueInput.getDoubleOr("PrevX", Double.NaN);
        if (!Double.isNaN(prevX)) {
            double prevY = valueInput.getDoubleOr("PrevY", 0.0);
            double prevZ = valueInput.getDoubleOr("PrevZ", 0.0);
            this.prevPosition = new Vec3(prevX, prevY, prevZ);
            this.prevYaw = valueInput.getFloatOr("PrevYaw", 0);
            this.prevPitch = valueInput.getFloatOr("PrevPitch", 0);
        }
        else {
            this.prevPosition = null;
        }

        if (this.isStarted) {
            this.hardcoreType = HARDCORE_TYPE.byKey(valueInput.getStringOr("HardCoreType", HARDCORE_TYPE.BEGINNER.getKey()));
        }

        String housingID = valueInput.getStringOr("HousingID", "none");
        if (!housingID.isEmpty() && !housingID.equals("none")) {
            this.housingID = UUID.fromString(housingID);
        }

        this.savedItems.clear();
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();

        valueInput.listOrEmpty("SavedSlots", Codec.INT).forEach(slots::add);
        valueInput.listOrEmpty("SavedItems", ItemStack.OPTIONAL_CODEC).forEach(items::add);

        for (int i = 0; i < Math.min(slots.size(), items.size()); i++) {
            this.savedItems.put(slots.get(i), items.get(i));
        }
    }

    @Override
    public void writeData(ValueOutput valueOutput) {
        valueOutput.putLong("TotalPlayTime", totalPlayTime);
        valueOutput.putLong("CurrentLiveTime", currentLiveTime);
        valueOutput.putInt("DeathCount", deathCount);
        valueOutput.putBoolean("IsStarted", isStarted);
        valueOutput.putInt("ThirstLevel", thirstLevel);
        valueOutput.putInt("HydrationTicks", hydrationTicks);
        valueOutput.putLong("Currency", currency);
        valueOutput.putBoolean("IsJail", isJail);
        if (this.prevDimension != null) {
            valueOutput.putString("PrevDimension", prevDimension.identifier().getPath());
        }
        else {
            valueOutput.putString("PrevDimension", "");
        }
        if (this.prevPosition != null) {
            valueOutput.putDouble("PrevX", this.prevPosition.x);
            valueOutput.putDouble("PrevY", this.prevPosition.y);
            valueOutput.putDouble("PrevZ", this.prevPosition.z);
            valueOutput.putFloat("PrevYaw", prevYaw);
            valueOutput.putFloat("PrevPitch", prevPitch);
        }
        else {
            valueOutput.putDouble("PrevX", Double.NaN);
        }

        if (this.isStarted) {
            valueOutput.putString("HardCoreType", this.hardcoreType.getKey());
        }

        if (this.housingID != null) {
            valueOutput.putString("HousingID", this.housingID.toString());
        }
        else {
            valueOutput.putString("HousingID", "none");
        }

        var slots = valueOutput.list("SavedSlots", Codec.INT);
        var items = valueOutput.list("SavedItems", ItemStack.OPTIONAL_CODEC);

        this.savedItems.forEach((slot, item) -> {
            slots.add(slot);
            items.add(item);
        });
    }

    @Override public long getTotalPlayTime() { return totalPlayTime; }
    @Override public long getCurrentLiveTime() { return currentLiveTime; }
    @Override public int getDeathCount() { return deathCount; }

    @Override public void setTotalPlayTime(long tick) {
        totalPlayTime = Math.max(0, tick);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override public void setCurrentLiveTime(long tick) {
        currentLiveTime = Math.max(0, tick);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override public void setDeathCount(int count) {
        deathCount = count;
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override public void addDeathCount() {
        deathCount++;
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override public boolean isStarted() { return isStarted; }

    @Override public void setStarted(boolean started) {
        isStarted = started;
        currentLiveTime = 0;
        ModComponents.HARDCORE_INFO.sync(player);

        MinecraftServer server = player.level().getServer();
        if (server != null) { server.getPlayerList().getPlayers().forEach(Utils::refreshTabList); }
    }

    @Override
    public void startHardCore(HARDCORE_TYPE hardcoreType) {
        setStarted(true);
        this.hardcoreType = hardcoreType;

        ResourceKey<Level> targetDimension = switch (hardcoreType) {
            case BEGINNER -> ModDimensions.BEGINNER_OVERWORLD;
            case EXPERT -> ModDimensions.EXPERT_OVERWORLD;
        };

        ServerLevel targetLevel = Objects.requireNonNull(player.level().getServer()).getLevel(targetDimension);
        var respawnData = Objects.requireNonNull(targetLevel).getRespawnData();
        Vec3 spawnPos = new Vec3(respawnData.pos().getX() + 0.5, respawnData.pos().getY(), respawnData.pos().getZ() + 0.5);

        TeleportTransition transition = new TeleportTransition(
            targetLevel,
            spawnPos,
            Vec3.ZERO,
            respawnData.yaw(),
            respawnData.pitch(),
            TeleportTransition.DO_NOTHING
        );
        Utils.refreshTabList((ServerPlayer) player);

        player.teleport(transition);
    }

    @Override
    public void stopHardCore() {
        setStarted(false);

        MinecraftServer server = player.level().getServer();
        if (server == null) { return; }

        ServerSavedData data = ServerSavedData.getServerState(server);
        TeleportTransition transition = data.createLobbyTransition(server, TeleportTransition.DO_NOTHING);

        if (transition == null) {
            PykeLib.sendSystemMessage((ServerPlayer) player, COLOR.RED.getColor(), "로비 위치가 설정되지 않았거나, 해당 월드를 불러올 수 없습니다.");
            return;
        }

        player.teleport(transition);
    }

    @Override
    public void clientTick() {
        totalPlayTime++;
    }

    @Override
    public void serverTick() {
        totalPlayTime++;
        if (isStarted && player.level().getDifficulty() == Difficulty.HARD && !isJail) {
            currentLiveTime++;
        }

        if (totalPlayTime % 20 == 0) {
            ModComponents.HARDCORE_INFO.sync(player);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            if (hydrationTicks > 0) {
                hydrationTicks--;
            }
            else if (thirstLevel > 0 && player.level().getDifficulty() == Difficulty.HARD) {
                int randomTickSpeed = serverLevel.getGameRules().get(GameRules.RANDOM_TICK_SPEED);
                if (randomTickSpeed > 0 && player.getRandom().nextInt(ModConfig.INSTANCE.thirstDecreaseChance) < randomTickSpeed) {
                    thirstLevel--;
                    ModComponents.HARDCORE_INFO.sync(player);
                }
            }

            if (thirstLevel <= 0) {
                player.causeFoodExhaustion(ModConfig.INSTANCE.thirstExhaustionRate);
            }

            if (player.level().getDifficulty() == Difficulty.PEACEFUL && thirstLevel < 20) {
                thirstLevel = 20;
                ModComponents.HARDCORE_INFO.sync(player);
            }
        }

        if (!this.pendingTimerTasks.isEmpty()) {
            this.timerTasks.addAll(this.pendingTimerTasks);
            this.pendingTimerTasks.clear();
        }

        List<TimerTask> completedTasks = null;
        for (TimerTask task : this.timerTasks) {
            task.remainingTicks--;

            if (task.remainingTicks <= 0) {
                if (task.runnable != null) {
                    task.runnable.run();
                }
                if (completedTasks == null) {
                    completedTasks = new ArrayList<>();
                }
                completedTasks.add(task);
            }
            else if (task.remainingTicks % 20 == 0) {
                if (task.message != null && this.player instanceof ServerPlayer serverPlayer) {
                    int secondsLeft = task.remainingTicks / 20;
                    serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.valueOf(secondsLeft))));
                    serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(task.message)));
                    serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 0.5f, 1.f, serverPlayer.getRandom().nextLong()));
                }
            }
        }
        if (completedTasks != null) {
            this.timerTasks.removeAll(completedTasks);
        }
    }

    @Override
    public void resetAll() {
        totalPlayTime = 0;
        currentLiveTime = 0;
        deathCount = 0;
        isStarted = false;
        thirstLevel = 20;
        hydrationTicks = 0;

        savedItems.clear();

        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void saveItem(int slot, ItemStack itemStack) {
        savedItems.put(slot, itemStack.copy());

        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void loadItem() {
        if (savedItems.isEmpty()) { return; }

        Inventory inventory = player.getInventory();

        savedItems.forEach((slot, itemStack) -> inventory.setItem(slot, itemStack.copy()));

        savedItems.clear();
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public int getThirstLevel() { return thirstLevel; }

    @Override
    public void setThirstLevel(int level) {
        thirstLevel = Math.clamp(level, 0, 20);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override public int getHydrationTicks() { return hydrationTicks; }

    @Override
    public void setHydrationTicks(int ticks) {
        hydrationTicks = Math.max(0, ticks);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void drinkWater(int seconds) {
        setThirstLevel(20);
        setHydrationTicks(seconds * 20);
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, seconds * 20, 0));
    }

    @Override
    public long getCurrency() { return currency; }

    @Override
    public void setCurrency(long currency) {
        this.currency = Math.max(0, currency);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void addCurrency(long currency) {
        this.currency = Math.max(0, this.currency + currency);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void subCurrency(long currency) {
        this.currency = Math.max(0, this.currency - currency);
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public HARDCORE_TYPE getHardcoreType() { return hardcoreType; }

    @Override
    public UUID getHousingID() {
        return this.housingID;
    }

    @Override
    public void setHousingID(UUID id) {
        this.housingID = id;
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public boolean isJail() {
        return isJail;
    }

    @Override
    public void setJail(boolean jail) {
        isJail = jail;
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public void enterJail() {
        MinecraftServer server = player.level().getServer();
        if (server == null) { return; }

        if (!isJail) {
            isJail = true;
            setPrevPosition();
        }
        ServerSavedData data = ServerSavedData.getServerState(server);
        TeleportTransition transition = data.createJailTransition(server, TeleportTransition.DO_NOTHING);
        player.teleport(transition);
    }

    @Override
    public void exitJail() {
        isJail = false;
        teleportPrevPosition();
    }


    @Override
    public void setPrevPosition() {
        this.prevDimension = player.level().dimension();
        this.prevPosition = player.position();
        this.prevYaw = player.getYRot();
        this.prevPitch = player.getXRot();
        ModComponents.HARDCORE_INFO.sync(player);
    }

    @Override
    public boolean teleportPrevPosition() {
        if (this.prevDimension == null || this.prevPosition == null) { return false; }
        MinecraftServer server = player.level().getServer();
        if (server == null) { return false; }
        ServerLevel level = server.getLevel(this.prevDimension);
        if (level == null) { return false; }

        TeleportTransition transition = new TeleportTransition(level, this.prevPosition, Vec3.ZERO, this.prevYaw, this.prevPitch, TeleportTransition.DO_NOTHING);
        player.teleport(transition);

        this.prevDimension = null;
        this.prevPosition = null;
        this.prevYaw = 0.f;
        this.prevPitch = 0.f;
        ModComponents.HARDCORE_INFO.sync(player);
        return true;
    }

    @Override
    public void addTimerTask(int seconds, Runnable runnable) {
        this.pendingTimerTasks.add(new TimerTask(seconds * 20, null, runnable));
    }

    @Override
    public void addTimerTaskMessage(int seconds, String message, Runnable runnable) {
        this.pendingTimerTasks.add(new TimerTask(seconds * 20, message, runnable));
    }

    @Override
    public void addTimerMessage(int seconds, String message) {
        this.pendingTimerTasks.add(new TimerTask(seconds * 20, message, null));
    }

    @Override
    public void randomTargetTeleport() {
        MinecraftServer server = player.level().getServer();
        if (server == null) { return; }

        List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
        players.remove((ServerPlayer) player);
        if (players.isEmpty()) { return; }

        ServerPlayer targetPlayer = players.get(player.getRandom().nextInt(players.size()));

        ModComponents.HARDCORE_INFO.get(targetPlayer).addTimerMessage(10, "잠시 후 §7???§r님이 이동됩니다.");

        UUID targetUUID = targetPlayer.getUUID();

        addTimerTaskMessage(10, "잠시 후 §7%s§r님에게 이동합니다.", () -> {
            MinecraftServer srv = player.level().getServer();
            ServerPlayer target = srv.getPlayerList().getPlayer(targetUUID);
            if (target == null) {
                if (player instanceof ServerPlayer sp) {
                    PykeLib.sendSystemMessage(sp, COLOR.RED.getColor(), "대상 플레이어가 오프라인입니다.");
                }
                return;
            }
            setPrevPosition();
            TeleportTransition transition = new TeleportTransition(target.level(), target.position(), Vec3.ZERO, target.getYRot(), target.getXRot(), TeleportTransition.DO_NOTHING);
            player.teleport(transition);
            addTimerTask(30, this::teleportPrevPosition);
        });
    }
}
