package kr.pyke.acau_hardcore.registry.component.housing;

import kr.pyke.acau_hardcore.data.housing.HousingZone;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.Collection;
import java.util.UUID;

public interface IHousingData extends ComponentV3, AutoSyncedComponent {
    Collection<HousingZone> getHousingZones();
    HousingZone getHousingZone(UUID zoneID);
    void addZone(HousingZone zone);
    void removeZone(HousingZone zone);
}
