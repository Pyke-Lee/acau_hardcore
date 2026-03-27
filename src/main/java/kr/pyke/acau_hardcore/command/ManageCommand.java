package kr.pyke.acau_hardcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import kr.pyke.PykeLib;
import kr.pyke.acau_hardcore.AcauHardCore;
import kr.pyke.acau_hardcore.boss.raid.BossRaidManager;
import kr.pyke.acau_hardcore.config.ModConfig;
import kr.pyke.acau_hardcore.data.housing.HousingStructureManager;
import kr.pyke.acau_hardcore.data.housing.HousingZone;
import kr.pyke.acau_hardcore.data.randombox.BoxRegistry;
import kr.pyke.acau_hardcore.data.rune.RuneInstance;
import kr.pyke.acau_hardcore.data.rune.RuneRoller;
import kr.pyke.acau_hardcore.handler.DonationEventHandler;
import kr.pyke.acau_hardcore.registry.component.ModComponents;
import kr.pyke.acau_hardcore.registry.component.hardcore.IHardCoreInfo;
import kr.pyke.acau_hardcore.registry.component.housing.IHousingData;
import kr.pyke.acau_hardcore.registry.dimension.ModDimensions;
import kr.pyke.acau_hardcore.registry.item.rune.RuneItemHelper;
import kr.pyke.acau_hardcore.type.BOSS_RAID_TYPE;
import kr.pyke.acau_hardcore.type.HARDCORE_TYPE;
import kr.pyke.acau_hardcore.type.RUNE_EFFECT;
import kr.pyke.util.constants.COLOR;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ManageCommand {
    private ManageCommand() { }

    private static CompletableFuture<Suggestions> hardCoreSuggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestionsBuilder) {
        for (HARDCORE_TYPE type : HARDCORE_TYPE.values()) {
            suggestionsBuilder.suggest(type.getKey());
        }

        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> levelSuggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestionsBuilder) {
        suggestionsBuilder.suggest("overworld");
        suggestionsBuilder.suggest("nether");
        suggestionsBuilder.suggest("end");

        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> runeSuggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestionsBuilder) {
        for (RUNE_EFFECT effect : RUNE_EFFECT.values()) {
            suggestionsBuilder.suggest(effect.getKey());
        }

        return suggestionsBuilder.buildFuture();
    }

    private static CompletableFuture<Suggestions> raidTypeSuggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder suggestionsBuilder) {
        for (BOSS_RAID_TYPE type : BOSS_RAID_TYPE.values()) {
            suggestionsBuilder.suggest(type.getKey());
        }

        return suggestionsBuilder.buildFuture();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("관리")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
            .then(Commands.literal("초기화")
                .then(Commands.literal("전체")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::resetAll)
                    )
                )
            )
            .then(Commands.literal("설정")
                .then(Commands.literal("지구")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                            .executes(ManageCommand::setDeathCount)
                        )
                    )
                )
                .then(Commands.literal("생존시간")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("ticks", LongArgumentType.longArg(0))
                            .executes(ManageCommand::setLiveTime)
                        )
                    )
                )
                .then(Commands.literal("돈")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                            .executes(ManageCommand::setCurrency)
                        )
                    )
                )
            )
            .then(Commands.literal("지급")
                .then(Commands.literal("돈")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                            .executes(ManageCommand::addCurrency)
                        )
                    )
                )
            )
            .then(Commands.literal("회수")
                .then(Commands.literal("돈")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                            .executes(ManageCommand::subCurrency)
                        )
                    )
                )
            )
            .then(Commands.literal("정보")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ManageCommand::targetInfo)
                )
            )
            .then(Commands.literal("시작")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("type", StringArgumentType.greedyString())
                        .suggests(ManageCommand::hardCoreSuggest)
                        .executes(ManageCommand::startHardCore)
                    )
                )
            )
            .then(Commands.literal("종료")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ManageCommand::stopHardCore)
                )
            )
            .then(Commands.literal("귀속")
                .executes(ManageCommand::bindItem)
            )
            .then(Commands.literal("리로드")
                .executes(ManageCommand::reloadConfig)
            )
            .then(Commands.literal("월드이동")
                .then(Commands.argument("type", StringArgumentType.string())
                    .suggests(ManageCommand::hardCoreSuggest)
                    .then(Commands.argument("level", StringArgumentType.string())
                        .suggests(ManageCommand::levelSuggest)
                        .executes(ManageCommand::teleportWorldSelf)

                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(ManageCommand::teleportWorldTarget)
                        )
                    )
                )
            )
            .then(Commands.literal("감옥")
                .then(Commands.literal("입장")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::enterJail)
                    )
                )
                .then(Commands.literal("퇴장")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::exitJail)
                    )
                )
            )
            .then(Commands.literal("섭주")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ManageCommand::setServerOwner)
                )
            )
            .then(Commands.literal("구역")
                .then(Commands.literal("변경")
                    .then(Commands.argument("tier", IntegerArgumentType.integer(1, 3))
                        .executes(ManageCommand::changeZoneTier)
                    )
                )
                .then(Commands.literal("정리")
                    .executes(ManageCommand::clearZone)
                )
                .then(Commands.literal("이동")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::teleportToZone)
                    )
                )
                .then(Commands.literal("제거")
                    .executes(ManageCommand::removeZone)
                )
                .then(Commands.literal("목록")
                    .executes(ManageCommand::listZones)
                )
                .then(Commands.literal("정보")
                    .executes(ManageCommand::zoneInfoCurrent)
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::zoneInfoTarget)
                    )
                )
            )
            .then(Commands.literal("관리자")
                .then(Commands.literal("설정")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::givePermission)
                    )
                )
                .then(Commands.literal("해제")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::removePermission)
                    )
                )
            )
            .then(Commands.literal("룬")
                .then(Commands.argument("effect", StringArgumentType.greedyString())
                    .suggests(ManageCommand::runeSuggest)
                    .executes(ManageCommand::setRuneEffect)
                )
            )
            .then(Commands.literal("레이드")
                .then(Commands.literal("강제종료")
                    .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(ManageCommand::raidTypeSuggest)
                        .executes(ManageCommand::forceEndRaid)
                    )
                )
                .then(Commands.literal("쿨타임초기화")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ManageCommand::clearRaidCooldown)
                    )
                )
            )
        );
    }

    private static int setRuneEffect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        String effectKey = StringArgumentType.getString(context, "effect");
        RUNE_EFFECT targetEffect = RUNE_EFFECT.byKey(effectKey);

        if (targetEffect == null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "존재하지 않는 룬 효과입니다: " + effectKey);
            return 0;
        }

        ItemStack mainHandItem = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.isEmpty()) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "손에 아이템을 들고 있어야 합니다.");
            return 0;
        }
        if (!targetEffect.getTarget().canApplyTo(mainHandItem)) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "이 아이템에는 해당 룬(" + targetEffect.getKey() + ")을 적용할 수 없습니다.");
            return 0;
        }

        RuneInstance rune = RuneRoller.rollSpecific(targetEffect, serverPlayer.getRandom());
        if (rune != null) {
            RuneItemHelper.setRune(mainHandItem, rune);

            CustomData customData = mainHandItem.get(DataComponents.CUSTOM_DATA);
            boolean hasTag = customData != null && customData.copyTag().contains("KeepOnDeath");
            if (!hasTag) {
                CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
                tag.putBoolean("KeepOnDeath", true);
                mainHandItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            PykeLib.sendSystemMessage(serverPlayer, COLOR.GREEN.getColor(), "성공적으로 룬을 부여했습니다: (RUNE) " + rune.formatDescription());
            return 1;
        }
        else {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "룬 수치 생성에 실패했습니다.");
            return 0;
        }
    }

    private static int addCurrency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        long amount = LongArgumentType.getLong(context, "amount");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).addCurrency(amount);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("&e%,d&f원을 지급받았습니다.", amount));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명에게 &e%,d&f원을 지급했습니다.", targets.size(), amount));

        return 1;
    }

    private static int subCurrency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        long amount = LongArgumentType.getLong(context, "amount");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).subCurrency(amount);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("&e%,d&f원을 잃었습니다.", amount));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명에게 &e%,d&f원을 회수했습니다.", targets.size(), amount));

        return 1;
    }

    private static int setCurrency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        long amount = LongArgumentType.getLong(context, "amount");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).setCurrency(amount);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("소지금이 &e%,d&f원으로 변경되었습니다.", amount));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명의 소지금을 &e%,d&f원으로 변경했습니다.", targets.size(), amount));

        return 1;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).resetAll();
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "데이터가 초기화되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명의 플레이어 데이터를 초기화했습니다.", targets.size()));

        return 1;
    }

    private static int setDeathCount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        int count = IntegerArgumentType.getInteger(context, "count");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).setDeathCount(count);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "지구(사망 횟수)가 " + count + "으로 설정되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명의 지구(사망 횟수)를 &e%s&f으로 설정했습니다.", targets.size(), count));

        return targets.size();
    }

    private static int setLiveTime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        long ticks = LongArgumentType.getLong(context, "ticks");

        String timeText = formatTime(ticks);

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).setCurrentLiveTime(ticks);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "생존 시간이 " + timeText + "으로 설정되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명의 생존 시간을 &7%s (%s틱)&f으로 설정했습니다.", targets.size(), timeText, ticks));

        return targets.size();
    }

    private static int targetInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        IHardCoreInfo info = ModComponents.HARDCORE_INFO.get(target);

        int currentEarth = info.getDeathCount() + 1;
        String totalPlayTime = formatTime(info.getTotalPlayTime());
        String liveTime = info.isStarted() ? formatTime(info.getCurrentLiveTime()) : "--:--:--";

        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, "");
        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("대상: &7%s", target.getDisplayName().getString()));
        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("전체 플레이 타임: &7%s", totalPlayTime));
        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("생존 시간: &7%s (%s 지구)", liveTime, currentEarth));
        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("소지금: &7%,d원", info.getCurrency()));
        PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, "");

        return 1;
    }

    private static int startHardCore(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");
        String typeKey = StringArgumentType.getString(context, "type");
        HARDCORE_TYPE hardcoreType = HARDCORE_TYPE.byKey(typeKey);

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).startHardCore(hardcoreType);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "하드코어 생존이 시작되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명이 하드코어 생존을 시작하였습니다.", targets.size()));

        return targets.size();
    }

    private static int stopHardCore(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
            ModComponents.HARDCORE_INFO.get(target).stopHardCore();
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "관리자에 의해 하드코어 생존이 종료되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&f명의 하드코어 생존을 종료하였습니다.", targets.size()));

        return targets.size();
    }

    private static int bindItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ItemStack itemStack = serverPlayer.getMainHandItem();

        if (itemStack.isEmpty()) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "손에 아이템을 들고 있어야 합니다.");
            return 0;
        }

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        boolean hasTag = customData != null && customData.copyTag().contains("KeepOnDeath");

        if (hasTag) {
            CompoundTag tag = customData.copyTag();
            tag.remove("KeepOnDeath");

            if (tag.isEmpty()) {
                itemStack.remove(DataComponents.CUSTOM_DATA);
            }
            else {
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }

            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "귀속이 해제되었습니다.");
        }
        else {
            CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
            tag.putBoolean("KeepOnDeath", true);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "귀속이 설정되었습니다.");
        }

        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();

        if (ModConfig.reload(serverPlayer.level().getServer())) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "Mod Config 리로드가 완료되었습니다.");
        }

        if (BoxRegistry.load(AcauHardCore.SERVER_INSTANCE, true)) {
            AcauHardCore.LOGGER.info("랜덤 박스 {}개를 모든 플레이어에게 동기화했습니다.", BoxRegistry.size());
            PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "랜덤 상자 로드가 완료되었습니다.");
        }

        return 1;
    }

    private static int teleportWorldSelf(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();
        String typeKey = StringArgumentType.getString(context, "type");
        HARDCORE_TYPE hardcoreType = HARDCORE_TYPE.byKey(typeKey);
        String levelKey = StringArgumentType.getString(context, "level");

        ResourceKey<Level> targetDimension = switch (hardcoreType) {
            case BEGINNER -> switch (levelKey) {
                case "overworld" -> ModDimensions.BEGINNER_OVERWORLD;
                case "nether" -> Level.NETHER;
                case "end" -> Level.END;
                default -> Level.OVERWORLD;
            };
            case EXPERT -> switch (levelKey) {
                case "overworld" -> ModDimensions.EXPERT_OVERWORLD;
                case "nether" -> ModDimensions.EXPERT_NETHER;
                case "end" -> ModDimensions.EXPERT_END;
                default -> Level.OVERWORLD;
            };
        };

        ServerLevel targetLevel = Objects.requireNonNull(server).getLevel(targetDimension);
        var respawnData = Objects.requireNonNull(targetLevel).getRespawnData();
        Vec3 spawnPos = new Vec3(respawnData.pos().getX() + 0.5, respawnData.pos().getY(), respawnData.pos().getZ() + 0.5);

        TeleportTransition transition = new TeleportTransition(targetLevel, spawnPos, Vec3.ZERO, respawnData.yaw(), respawnData.pitch(), TeleportTransition.DO_NOTHING);
        serverPlayer.teleport(transition);

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f로 이동하였습니다.", targetDimension.identifier().getPath()));

        return 1;
    }

    private static int teleportWorldTarget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        MinecraftServer server = context.getSource().getServer();
        var targets = EntityArgument.getPlayers(context, "target");
        String typeKey = StringArgumentType.getString(context, "type");
        HARDCORE_TYPE hardcoreType = HARDCORE_TYPE.byKey(typeKey);
        String levelKey = StringArgumentType.getString(context, "level");

        ResourceKey<Level> targetDimension = switch (hardcoreType) {
            case BEGINNER -> switch (levelKey) {
                case "overworld" -> ModDimensions.BEGINNER_OVERWORLD;
                case "nether" -> Level.NETHER;
                case "end" -> Level.END;
                default -> Level.OVERWORLD;
            };
            case EXPERT -> switch (levelKey) {
                case "overworld" -> ModDimensions.EXPERT_OVERWORLD;
                case "nether" -> ModDimensions.EXPERT_NETHER;
                case "end" -> ModDimensions.EXPERT_END;
                default -> Level.OVERWORLD;
            };
        };

        ServerLevel targetLevel = Objects.requireNonNull(server).getLevel(targetDimension);
        var respawnData = Objects.requireNonNull(targetLevel).getRespawnData();
        Vec3 spawnPos = new Vec3(respawnData.pos().getX() + 0.5, respawnData.pos().getY(), respawnData.pos().getZ() + 0.5);

        TeleportTransition transition = new TeleportTransition(targetLevel, spawnPos, Vec3.ZERO, respawnData.yaw(), respawnData.pitch(), TeleportTransition.DO_NOTHING);

        targets.forEach(target -> {
            target.teleport(transition);
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), String.format("&7%s&f로 이동하였습니다.", targetDimension.identifier().getPath()));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f명을 &7%s&f로 이동시켰습니다.", targets.size(), targetDimension.identifier().getPath()));

        return 1;
    }

    private static int enterJail(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
            var info = ModComponents.HARDCORE_INFO.get(target);
            if (!info.isJail()) {
                info.enterJail();
                PykeLib.sendSystemMessage(serverPlayer, COLOR.DARK_RED.getColor(), "감옥에 수감되었습니다.");
            }
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f명을 감옥에 수감시켰습니다.", targets.size()));

        return 1;
    }

    private static int exitJail(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
            var info = ModComponents.HARDCORE_INFO.get(target);
            if (info.isJail()) {
                info.exitJail();
                PykeLib.sendSystemMessage(serverPlayer, COLOR.DARK_RED.getColor(), "감옥에서 석방되었습니다.");
            }
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&f명을 감옥에서 석방시켰습니다.", targets.size()));

        return 1;
    }

    private static int setServerOwner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        DonationEventHandler.ownedPlayer = target;
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), target.getDisplayName().getString() + "님을 서버 주인으로 설정했습니다.");

        return 1;
    }

    private static int changeZoneTier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        int tier = IntegerArgumentType.getInteger(context, "tier");
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());
        BlockPos pos = serverPlayer.blockPosition();

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(pos)) {
                zone.setTier(tier);
                housingData.addZone(zone);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("현재 위치한 구역의 단계를 %d단계로 변경했습니다.", tier));
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "현재 거주 구역 안에 있지 않습니다.");

        return 0;
    }

    private static int clearZone(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());
        BlockPos pos = serverPlayer.blockPosition();

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(pos)) {
                zone.setOwnerID(null);
                zone.setTier(0);
                housingData.addZone(zone);
                HousingStructureManager.changeTier(serverPlayer.level(), zone, 0, Identifier.fromNamespaceAndPath("acau_hardcore", "empty"), true);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "현재 위치한 구역을 초기화하고 블록을 정리했습니다.");
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "현재 거주 구역 안에 있지 않습니다.");

        return 0;
    }

    private static int teleportToZone(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.getOwnerID() != null && zone.getOwnerID().equals(target.getUUID())) {
                BlockPos min = zone.getMinPos();
                BlockPos max = zone.getMaxPos();
                double centerX = (min.getX() + max.getX()) / 2.0;
                double centerZ = (min.getZ() + max.getZ()) / 2.0;

                serverPlayer.teleportTo(centerX, min.getY() + 1, centerZ);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), target.getDisplayName().getString() + "님이 점유한 구역으로 이동했습니다.");
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "대상이 점유한 구역이 없습니다.");

        return 0;
    }

    private static int removeZone(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());
        BlockPos pos = serverPlayer.blockPosition();

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(pos)) {
                housingData.removeZone(zone);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "현재 위치한 구역을 서버에서 제거했습니다.");
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "현재 거주 구역 안에 있지 않습니다.");

        return 0;
    }

    private static String getOwnerName(MinecraftServer server, UUID uuid) {
        if (uuid == null) { return "없음"; }

        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) { return player.getDisplayName().getString(); }

        return uuid.toString().substring(0, 8);
    }

    private static int listZones(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());
        MinecraftServer server = context.getSource().getServer();

        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("서버 구역 목록 (총 %d개)", housingData.getHousingZones().size()));
        for (HousingZone zone : housingData.getHousingZones()) {
            String owner = getOwnerName(server, zone.getOwnerID());
            String zoneId = zone.getZoneID().toString().substring(0, 8);
            PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("- 구역 ID: %s | 주인: %s | %d단계", zoneId, owner, zone.getTier()));
        }

        return 1;
    }

    private static int zoneInfoCurrent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());
        BlockPos pos = serverPlayer.blockPosition();
        MinecraftServer server = context.getSource().getServer();

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.isInsideZone(pos)) {
                String owner = getOwnerName(server, zone.getOwnerID());
                String zoneId = zone.getZoneID().toString().substring(0, 8);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), "현재 구역 정보");
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("ID: %s", zoneId));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("주인: %s", owner));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("단계: %d", zone.getTier()));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("좌표: %s ~ %s", zone.getMinPos().toShortString(), zone.getMaxPos().toShortString()));
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "현재 거주 구역 안에 있지 않습니다.");

        return 0;
    }

    private static int zoneInfoTarget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        IHousingData housingData = ModComponents.HOUSING_DATA.get(serverPlayer.level());

        for (HousingZone zone : housingData.getHousingZones()) {
            if (zone.getOwnerID() != null && zone.getOwnerID().equals(target.getUUID())) {
                String zoneId = zone.getZoneID().toString().substring(0, 8);
                PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("구역 정보 [%s님]", target.getDisplayName().getString()));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("ID: %s", zoneId));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("단계: %d", zone.getTier()));
                PykeLib.sendSystemMessage(serverPlayer, 0xFFFFFF, String.format("좌표: %s ~ %s", zone.getMinPos().toShortString(), zone.getMaxPos().toShortString()));
                return 1;
            }
        }
        PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "대상이 점유한 구역이 없습니다.");

        return 0;
    }

    private static int givePermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

        MinecraftServer server = context.getSource().getServer();

        targets.forEach(target -> {
            NameAndId nameAndId = target.nameAndId();
            PermissionLevel level = PermissionLevel.byId(2);
            LevelBasedPermissionSet permissions = LevelBasedPermissionSet.forLevel(level);
            ServerOpListEntry entry = new ServerOpListEntry(nameAndId, permissions, true);

            server.getPlayerList().getOps().add(entry);
            server.getPlayerList().sendPlayerPermissionLevel(target);
            server.getCommands().sendCommands(target);
            PykeLib.sendSystemMessage(AcauHardCore.getOps(server), COLOR.AQUA.getColor(), String.format("&7%s&r님이 관리자 권한을 받았습니다.", target.getDisplayName().getString()));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&r명에게 관리자 권한을 지급 하였습니다.", targets.size()));

        return 1;
    }

    private static int removePermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "target");

        MinecraftServer server = context.getSource().getServer();

        targets.forEach(target -> {
            NameAndId nameAndId = target.nameAndId();

            server.getPlayerList().getOps().remove(nameAndId);
            server.getPlayerList().sendPlayerPermissionLevel(target);
            server.getCommands().sendCommands(target);
            PykeLib.sendSystemMessage(target, COLOR.AQUA.getColor(), "관리자 권한이 회수 되었습니다.");
            PykeLib.sendSystemMessage(AcauHardCore.getOps(server), COLOR.AQUA.getColor(), String.format("&7%s&r님의 관리자 권한이 회수 되었습니다.", target.getDisplayName().getString()));
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&7%s&r명의 관리자 권한을 회수 하였습니다.", targets.size()));

        return 1;
    }

    private static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }

    private static int forceEndRaid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        String typeKey = StringArgumentType.getString(context, "type");
        BOSS_RAID_TYPE raidType = BOSS_RAID_TYPE.byKey(typeKey);

        if (raidType == null) {
            PykeLib.sendSystemMessage(serverPlayer, COLOR.RED.getColor(), "존재하지 않는 레이드 유형입니다: " + typeKey);
            return 0;
        }

        BossRaidManager.forceEndRaid(context.getSource().getServer(), raidType, serverPlayer);
        return 1;
    }

    private static int clearRaidCooldown(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
        var targets = EntityArgument.getPlayers(context, "target");

        targets.forEach(target -> {
            BossRaidManager.clearCooldown(target.getUUID());
            PykeLib.sendSystemMessage(target, COLOR.LIME.getColor(), "레이드 쿨타임이 초기화되었습니다.");
        });
        PykeLib.sendSystemMessage(serverPlayer, COLOR.LIME.getColor(), String.format("&e%s&r명의 레이드 쿨타임을 초기화했습니다.", targets.size()));

        return 1;
    }
}