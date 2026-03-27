package kr.pyke.acau_hardcore.registry.component.housing;

import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class HousingData implements IHousingData {
    private final Level level;
    private final Map<UUID, HousingZone> zones = new HashMap<>();

    public HousingData(Level level) {
        this.level = level;
    }

    @Override
    public Collection<HousingZone> getHousingZones() {
        return this.zones.values();
    }

    @Override
    public HousingZone getHousingZone(UUID zoneID) {
        return this.zones.get(zoneID);
    }

    @Override
    public void addZone(HousingZone zone) {
        this.zones.put(zone.getZoneID(), zone);
        ModComponents.HOUSING_DATA.sync(level);
    }

    @Override
    public void removeZone(HousingZone zone) {
        if (zone.getOwnerID() != null) {
            MinecraftServer server = level.getServer();
            if (server != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(zone.getOwnerID());
                if (player != null) {
                    ModComponents.HARDCORE_INFO.get(player).setHousingID(null);
                }
            }
        }

        this.zones.remove(zone.getZoneID());
        ModComponents.HOUSING_DATA.sync(level);
    }

    @Override
    public void readData(ValueInput valueInput) {
        this.zones.clear();
        ValueInput.ValueInputList zoneList = valueInput.childrenListOrEmpty("HousingZones");
        for (ValueInput childInput : zoneList) {
            try {
                HousingZone zone = HousingZone.load(childInput);
                if (zone.getZoneID() != null) {
                    this.zones.put(zone.getZoneID(), zone);
                }
            }
            catch (IllegalArgumentException ignored) { }
        }
    }

    @Override
    public void writeData(ValueOutput valueOutput) {
        ValueOutput.ValueOutputList zoneList = valueOutput.childrenList("HousingZones");
        for (HousingZone zone : this.zones.values()) {
            ValueOutput childOutput = zoneList.addChild();
            zone.save(childOutput);
        }
    }
}