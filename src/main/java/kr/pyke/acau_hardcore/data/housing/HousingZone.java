package kr.pyke.acau_hardcore.data.housing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class HousingZone {
    private final UUID zoneID;
    private UUID ownerID;
    private final BlockPos minPos;
    private final BlockPos maxPos;
    private int tier;

    public HousingZone(UUID zoneID, UUID ownerID, BlockPos minPos, BlockPos maxPos, int tier) {
        this.zoneID = zoneID;
        this.ownerID = ownerID;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.tier = tier;
    }

    public UUID getZoneID() { return this.zoneID; }

    public UUID getOwnerID() { return this.ownerID; }
    public void setOwnerID(UUID ownerID) { this.ownerID = ownerID; }

    public BlockPos getMinPos() { return this.minPos; }

    public BlockPos getMaxPos() { return this.maxPos; }

    public int getTier() { return this.tier; }
    public void setTier(int tier) { this.tier = tier; }

    public void save(ValueOutput valueOutput) {
        valueOutput.putString("zoneID", this.zoneID.toString());
        if (this.ownerID != null) {
            valueOutput.putString("ownerID", this.ownerID.toString());
        }
        else {
            valueOutput.putString("ownerID", "none");
        }
        valueOutput.putLong("minPos", this.minPos.asLong());
        valueOutput.putLong("maxPos", this.maxPos.asLong());
        valueOutput.putInt("tier", this.tier);
    }

    public static HousingZone load(ValueInput valueInput) {
        UUID zoneID = UUID.fromString(valueInput.getStringOr("zoneID", ""));
        String ownerIDStr = valueInput.getStringOr("ownerID", "none");
        UUID ownerID = null;
        if (!ownerIDStr.equals("none") && !ownerIDStr.isEmpty()) {
            ownerID = UUID.fromString(ownerIDStr);
        }
        BlockPos minPos = BlockPos.of(valueInput.getLongOr("minPos", 0L));
        BlockPos maxPos = BlockPos.of(valueInput.getLongOr("maxPos", 0L));
        int tier = valueInput.getIntOr("tier", 0);

        return new HousingZone(zoneID, ownerID, minPos, maxPos, tier);
    }

    public boolean isInsideZone(BlockPos blockPos) {
        return blockPos.getX() >= getMinPos().getX() && blockPos.getX() <= getMaxPos().getX()
            && blockPos.getY() >= getMinPos().getY() && blockPos.getY() <= getMaxPos().getY()
            && blockPos.getZ() >= getMinPos().getZ() && blockPos.getZ() <= getMaxPos().getZ();
    }
}
