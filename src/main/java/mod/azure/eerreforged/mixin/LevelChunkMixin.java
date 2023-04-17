package mod.azure.eerreforged.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import mod.azure.eerreforged.ConditionalRunnable;
import mod.azure.eerreforged.EERRMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/*
 * Based off https://github.com/Benjamin-Norton/Neruina/blob/main/src/main/java/com/bawnorton/neruina/mixin/WorldChunkMixin.java
 * Credit to: https://github.com/Benjamin-Norton
 * Licence: https://github.com/Benjamin-Norton/Neruina/blob/main/LICENSE
 */
@Mixin(LevelChunk.class)
public class LevelChunkMixin {

	private static final List<BlockEntity> ERRORED_BLOCK_ENTITIES = new ArrayList<>();

	@Inject(method = "removeBlockEntity", at = @At("HEAD"))
	private void removeErrored(BlockPos pos, CallbackInfo ci) {
		BlockEntity blockEntity = ((LevelChunk) (Object) this).getBlockEntity(pos);
		if (blockEntity != null && ERRORED_BLOCK_ENTITIES.contains(blockEntity))
			ERRORED_BLOCK_ENTITIES.remove(blockEntity);
	}

	@Mixin(LevelChunk.BoundTickingBlockEntity.class)
	private abstract static class BoundTickingBlockEntityMixin {

		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet.minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
		private void catchTickingBlockEntity(BlockEntityTicker<? extends BlockEntity> instance, Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
			try {
				if (ERRORED_BLOCK_ENTITIES.contains(blockEntity)) {
					if (world.isClientSide())
						return;

					world.getChunkAt(pos).removeBlockEntityTicker(pos);
					return;
				}
				original.call(instance, world, pos, state, blockEntity);
			} catch (RuntimeException e) {
				EERRMod.LOGGER.warn(String.format("Caught Ticking Block Entity [%s] at position x=%s, y=%s, z=%s", state.getBlock().getName().getString(), pos.getX(), pos.getY(), pos.getZ()), e);
				ERRORED_BLOCK_ENTITIES.add(blockEntity);
				if (world instanceof ServerLevel serverWorld)
					ConditionalRunnable.create(() -> serverWorld.getServer().getPlayerList().broadcastSystemMessage(Component.nullToEmpty(String.format("Caught Ticking Block Entity [%s] at position x=%s, y=%s, z=%s", state.getBlock().getName().getString(), pos.getX(), pos.getY(), pos.getZ())), false), () -> serverWorld.getServer().getPlayerList().getPlayerCount() > 0);
			}
		}
	}
}
