package kr.pyke.acau_hardcore.registry.component.hardcore;

import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.UUID;

public interface IHardCoreInfo extends ComponentV3, AutoSyncedComponent {
    long getTotalPlayTime();
    long getCurrentLiveTime();
    int getDeathCount();

    void setTotalPlayTime(long tick);
    void setCurrentLiveTime(long tick);
    void setDeathCount(int count);

    int addDeathCount();

    boolean isStarted();

    boolean setStarted(boolean started);
    void startHardCore(HARDCORE_TYPE hardcoreType);
    void stopHardCore();

    void clientTick();
    void serverTick();
    void resetAll();

    void saveItem(int slot, ItemStack itemStack);
    void loadItem();

    int getThirstLevel();
    void setThirstLevel(int level);

    int getHydrationTicks();
    void setHydrationTicks(int ticks);

    void drinkWater(int seconds);

    long getCurrency();

    void setCurrency(long currency);
    void addCurrency(long currency);
    void subCurrency(long currency);

    HARDCORE_TYPE getHardcoreType();

    UUID getHousingID();
    void setHousingID(UUID id);

    boolean isJail();
    void setJail(boolean jail);
    void enterJail();
    void exitJail();

    void setPrevPosition();
    boolean teleportPrevPosition();

    void addTimerTask(int seconds, Runnable runnable);
    void addTimerMessage(int seconds, String message);
    void addTimerTaskMessage(int seconds, String message, Runnable runnable);

    void randomTargetTeleport();

    class TimerTask {
        int remainingTicks;
        final String message;
        final Runnable runnable;

        TimerTask(int ticks, String message, Runnable runnable) {
            this.remainingTicks = ticks;
            this.message = message;
            this.runnable = runnable;
        }
    }
}
