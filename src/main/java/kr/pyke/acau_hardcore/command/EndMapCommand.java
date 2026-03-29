package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;

public class EndMapCommand {
    private static final Identifier STRUCTURE_ID = Identifier.fromNamespaceAndPath("acau_hardcore", "end_island");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("endmap")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.literal("save")
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                    .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                        .executes(EndMapCommand::executeSave))))
            .then(Commands.literal("place")
                .executes(EndMapCommand::executePlace))
        );
    }

    private static int executeSave(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BlockPos p1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
            BlockPos p2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");

            BlockPos min = new BlockPos(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
            );
            Vec3i size = new Vec3i(
                Math.abs(p1.getX() - p2.getX()) + 1,
                Math.abs(p1.getY() - p2.getY()) + 1,
                Math.abs(p1.getZ() - p2.getZ()) + 1
            );

            StructureTemplateManager manager = level.getStructureManager();
            StructureTemplate template = manager.getOrCreate(STRUCTURE_ID);
            template.fillFromWorld(level, min, size, false, List.of(Blocks.STRUCTURE_VOID));
            manager.save(STRUCTURE_ID);

            context.getSource().sendSuccess(() -> Component.literal("엔드 섬 구조물 저장 완료 (" + size.getX() + "×" + size.getY() + "×" + size.getZ() + ")"), true);
            return 1;
        }
        catch (Exception e) {
            context.getSource().sendFailure(Component.literal("저장 실패: " + e.getMessage()));
            return 0;
        }
    }

    private static int executePlace(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> opt = manager.get(STRUCTURE_ID);

        if (opt.isEmpty()) {
            context.getSource().sendFailure(
                Component.literal("구조물 파일을 찾을 수 없습니다 (end_island.nbt)")
            );
            return 0;
        }

        StructureTemplate template = opt.get();
        int cx = -template.getSize().getX() / 2;
        int cz = -template.getSize().getZ() / 2;
        BlockPos placePos = new BlockPos(cx, 48, cz);

        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(false);
        template.placeInWorld(level, placePos, placePos, settings, level.random, 2);

        context.getSource().sendSuccess(() -> Component.literal("엔드 섬 배치 완료"), true);
        return 1;
    }
}