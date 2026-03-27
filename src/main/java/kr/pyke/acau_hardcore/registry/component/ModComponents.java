package kr.pyke.acau_hardcore.registry.component;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.registry.component.hardcore.HardCoreInfo;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.registry.component.housing.HousingData;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import kr.pyke.acau_hardcore.registry.component.mailbox.IMailBoxComponent;
import kr.pyke.acau_hardcore.registry.component.mailbox.MailBoxComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class ModComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<IHardCoreInfo> HARDCORE_INFO = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "hardcore_info"), IHardCoreInfo.class);
    public static final ComponentKey<IMailBoxComponent> MAIL_BOX = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "mailbox"), IMailBoxComponent.class);

    public static final ComponentKey<IHousingData> HOUSING_DATA = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(AcauHardCore.MOD_ID, "housing_data"), IHousingData.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(HARDCORE_INFO, HardCoreInfo::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(MAIL_BOX, MailBoxComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(HOUSING_DATA, HousingData::new);
    }
}
