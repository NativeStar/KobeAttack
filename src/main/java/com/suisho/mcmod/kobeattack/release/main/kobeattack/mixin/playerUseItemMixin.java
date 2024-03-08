package com.suisho.mcmod.kobeattack.release.main.kobeattack.mixin;

import com.suisho.mcmod.kobeattack.release.main.kobeattack.KobeAttack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Timer;
import java.util.TimerTask;

@Mixin(MinecraftClient.class)
public class playerUseItemMixin {
    @Unique
    private static Timer onButtonRelease = new Timer();
    private static boolean isAttackCharging = false;

    @Inject(at = @At("HEAD"), method = "doItemUse()V", cancellable = true)
    public void doItemUse(CallbackInfo ci) {
        HitResult target = MinecraftClient.getInstance().crosshairTarget;
        if (target != null && (target.getType() == HitResult.Type.MISS || isAttackCharging)) {
            if (MinecraftClient.getInstance().player != null) {
                if (MinecraftClient.getInstance().player.getMainHandStack().isEmpty()) {
                    if (MinecraftClient.getInstance().options.useKey.isPressed()) {
                        isAttackCharging = true;
                        if (KobeAttack.kobe_attackLevel < 24) KobeAttack.kobe_attackLevel += 1;
                        MinecraftClient.getInstance().player.sendMessage(Text.of((KobeAttack.kobe_attackLevel == 24 ? "§c" : "") + KobeAttack.kobe_attackLevel + "/24"), true);
                        onButtonRelease.cancel();
                        onButtonRelease = new Timer();
                        onButtonRelease.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (KobeAttack.kobe_attackLevel != 0) {
                                    HitResult newTarget = MinecraftClient.getInstance().crosshairTarget;
                                    if (newTarget.getType() == HitResult.Type.ENTITY) {
                                        MinecraftClient.getInstance().interactionManager.attackEntity(MinecraftClient.getInstance().player, ((EntityHitResult) newTarget).getEntity());
                                        ci.cancel();
                                    } else if (newTarget.getType() == HitResult.Type.BLOCK) {
                                        Block block = MinecraftClient.getInstance().world.getBlockState(((BlockHitResult) newTarget).getBlockPos()).getBlock();
                                        String blockId = Registries.BLOCK.getId(block).toString();
                                        //判断门
                                        if (blockId.startsWith("minecraft:") && blockId.endsWith("_door")) {
                                            //10以上触发
                                            if (KobeAttack.kobe_attackLevel >= 10) {
                                                ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
                                                BlockPos blockPos = ((BlockHitResult) newTarget).getBlockPos();
                                                ServerWorld serverWorld = MinecraftClient.getInstance().getServer().getWorld(clientPlayerEntity.getWorld().getRegistryKey());
                                                clientPlayerEntity.playSound(SoundEvent.of(new Identifier("minecraft:entity.zombie.break_wooden_door")), 1.0f, 1.0f);
                                                serverWorld.createExplosion(MinecraftClient.getInstance().player, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.5f, World.ExplosionSourceType.MOB);
                                                if (!blockId.equals("minecraft:iron_door")) serverWorld.breakBlock(blockPos, true);
                                            }
                                        }
                                    }
                                    MinecraftClient.getInstance().player.sendMessage(Text.empty(), true);
                                    KobeAttack.kobe_attackLevel = 0;
                                    isAttackCharging = false;
                                }
                                if (isAttackCharging) ci.cancel();
                            }
                        }, 250L);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
