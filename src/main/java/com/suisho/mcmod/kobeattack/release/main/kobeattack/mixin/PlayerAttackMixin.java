package com.suisho.mcmod.kobeattack.release.main.kobeattack.mixin;

import com.suisho.mcmod.kobeattack.release.main.kobeattack.KobeAttack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerAttackMixin {
    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    public void attackInject(Entity target, CallbackInfo ci){
        if (!target.getWorld().isClient) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null){
                target.damage(player.getDamageSources().playerAttack(player), (float) KobeAttack.kobe_attackLevel);
                KobeAttack.kobe_attackLevel = 0;
                //去掉残留文本
                MinecraftClient.getInstance().player.sendMessage(Text.empty(), true);
            }
        }
    }
}
