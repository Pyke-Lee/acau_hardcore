package kr.pyke.acau_hardcore.mixin.server.entity;

import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.data.displayname.DisplayNameData;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntitySelectorParser.class)
public class EntitySelectorParserMixin {
    @ModifyVariable(method = "parseNameOrUUID", at = @At("STORE"))
    private String replaceDisplayNameWithRealName(String name) {
        if (null == name || name.isEmpty()) { return ""; }
        if (AcauHardCore.SERVER_INSTANCE == null) { return name; }

        return DisplayNameData.getServerState(AcauHardCore.SERVER_INSTANCE).getRealName(name);
    }
}