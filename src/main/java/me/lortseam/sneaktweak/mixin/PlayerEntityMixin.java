package me.lortseam.sneaktweak.mixin;

import me.lortseam.sneaktweak.config.ModConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {

    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getBaseDimensions", at = @At("RETURN"), cancellable = true)
    private void onGetEyeHeight(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        EntityDimensions dims = cir.getReturnValue();
        if ((Entity) this instanceof ClientPlayerEntity pl) {
            if (pose == EntityPose.CROUCHING && pl.canChangeIntoPose(EntityPose.STANDING)) {
                dims = dims.withEyeHeight(ModConfig.modifySneakingEyeHeight(dims.eyeHeight()));
            }
        }
        cir.setReturnValue(dims);
    }
}
