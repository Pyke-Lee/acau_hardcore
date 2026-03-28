package kr.pyke.acau_hardcore.boss.enderdragon.phase;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class ModEnderDragonPhases {
    public static final EnderDragonPhase<EarthQuakePhase> EARTHQUAKE_PHASE = register(EarthQuakePhase.class, "EarthQuake");

    @SuppressWarnings("unchecked")
    private static <T extends DragonPhaseInstance> EnderDragonPhase<T> register(Class<T> phaseClass, String name) {
        try {
            Field phasesField = null;
            for (Field f : EnderDragonPhase.class.getDeclaredFields()) {
                if (f.getType() == EnderDragonPhase[].class) {
                    phasesField = f;
                    break;
                }
            }
            if (phasesField == null) {
                throw new RuntimeException("Could not find phases array field in EnderDragonPhase");
            }
            phasesField.setAccessible(true);

            EnderDragonPhase<?>[] oldPhases = (EnderDragonPhase<?>[]) phasesField.get(null);

            int newId = oldPhases.length;
            Constructor<EnderDragonPhase<T>> constructor = (Constructor<EnderDragonPhase<T>>) (Constructor<?>)
                EnderDragonPhase.class.getDeclaredConstructor(int.class, Class.class, String.class);
            constructor.setAccessible(true);

            EnderDragonPhase<T> newPhase = constructor.newInstance(newId, phaseClass, name);

            EnderDragonPhase<?>[] newPhases = Arrays.copyOf(oldPhases, oldPhases.length + 1);
            newPhases[newId] = newPhase;
            phasesField.set(null, newPhases);

            return newPhase;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to register custom EnderDragonPhase", e);
        }
    }
}