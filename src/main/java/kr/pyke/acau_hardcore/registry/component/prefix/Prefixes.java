package kr.pyke.acau_hardcore.registry.component.prefix;

import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Prefixes implements IPrefixes {
    private final Player player;
    private final List<String> PREFIXES = new ArrayList<>();
    String selectedPrefix = "none";

    public Prefixes(Player player) {
        this.player = player;
    }

    @Override
    public List<String> getPrefixes() { return PREFIXES; }

    @Override
    public String getSelectedPrefix() { return selectedPrefix; }

    @Override
    public void addPrefix(String id) {
        if (!PREFIXES.contains(id)) {
            PREFIXES.add(id);
            ModComponents.PREFIXES.sync(player);
        }
    }

    @Override
    public void removePrefix(String id) {
        if (PREFIXES.contains(id)) {
            PREFIXES.remove(id);
            if (selectedPrefix.equals(id)) { selectedPrefix = "none"; }
            ModComponents.PREFIXES.sync(player);
            Utils.refreshTabList((ServerPlayer) player);
        }
    }

    @Override
    public void selectPrefix(String id) {
        if (PREFIXES.contains(id) || id.equals("none")) {
            selectedPrefix = id;
            ModComponents.PREFIXES.sync(player);
            Utils.refreshTabList((ServerPlayer) player);
        }
    }

    @Override
    public void clearAll() {
        PREFIXES.clear();
        selectedPrefix = "none";
        ModComponents.PREFIXES.sync(player);
    }

    @Override
    public void readData(ValueInput valueInput) {
        PREFIXES.clear();

        Optional<ValueInput.ValueInputList> prefixes = valueInput.childrenList("Prefixes");
        if (prefixes.isPresent()) {
            for (ValueInput prefix : prefixes.get()) {
                Optional<String> id = prefix.getString("ID");
                id.ifPresent(PREFIXES::add);
            }
        }

        selectedPrefix = valueInput.getStringOr("SelectedPrefix", "none");
    }

    @Override
    public void writeData(ValueOutput valueOutput) {
        ValueOutput.ValueOutputList prefixes = valueOutput.childrenList("Prefixes");

        for (String id : PREFIXES) {
            ValueOutput prefix = prefixes.addChild();
            prefix.putString("ID", id);
        }

        valueOutput.putString("SelectedPrefix", selectedPrefix);
    }
}
