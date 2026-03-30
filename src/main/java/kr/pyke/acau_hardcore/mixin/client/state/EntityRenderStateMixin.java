package kr.pyke.acau_hardcore.mixin.client.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IPrefixRenderState {
    @Unique private String prefixID = "none";

    @Override public String acau_hardcore$getPrefixID() { return this.prefixID; }

    @Override public void acau_hardcore$setPrefixID(String id) { this.prefixID = id; }
}
