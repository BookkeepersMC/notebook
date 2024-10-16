/*
 * Copyright (c) 2023, 2024 BookkeepersMC under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.bookkeepersmc.notebook.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DeltaTracker;
import net.minecraft.client.render.FogParameters;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldFramebuffers;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import com.bookkeepersmc.notebook.api.client.rendering.v1.DimensionRenderingRegistry;
import com.bookkeepersmc.notebook.api.client.rendering.v1.InvalidateRenderStateCallback;
import com.bookkeepersmc.notebook.api.client.rendering.v1.WorldRenderEvents;
import com.bookkeepersmc.notebook.impl.client.rendering.WorldRenderContextImpl;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Final
	@Shadow
	private BufferBuilderStorage bufferBuilders;
	@Shadow private ClientWorld world;
	@Final
	@Shadow
	private Minecraft client;
	@Shadow
	@Final
	private WorldFramebuffers framebuffers;
	@Unique private final WorldRenderContextImpl context = new WorldRenderContextImpl();

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(GraphicsResourceAllocator objectAllocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		context.prepare((WorldRenderer) (Object) this, tickCounter, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix, positionMatrix, bufferBuilders.getEntityVertexConsumers(), Minecraft.isFabulousGraphicsOrBetter(), world);
		WorldRenderEvents.START.invoker().onStart(context);
	}

	@Inject(method = "setupTerrain", at = @At("RETURN"))
	private void afterTerrainSetup(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
		context.setFrustum(frustum);
		WorldRenderEvents.AFTER_SETUP.invoker().afterSetup(context);
	}

	@Inject(
			method = "method_62214",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/WorldRenderer;renderSectionLayer(Lnet/minecraft/client/render/RenderLayer;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
					ordinal = 2,
					shift = At.Shift.AFTER
			)
	)
	private void afterTerrainSolid(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(context);
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "NEW", target = "net/minecraft/client/util/math/MatrixStack"))
	private MatrixStack setMatrixStack(MatrixStack matrixStack) {
		context.setMatrixStack(matrixStack);
		return matrixStack;
	}

	@Inject(method = "method_62214", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
	private void afterEntities(CallbackInfo ci) {
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(context);
	}

	@Inject(
			method = "renderBlockOutline",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;crosshairTarget:Lnet/minecraft/util/hit/HitResult;",
					shift = At.Shift.AFTER,
					ordinal = 0
			)
	)
	private void beforeRenderOutline(CallbackInfo ci) {
		context.renderBlockOutline = WorldRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(context, client.crosshairTarget);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
	private void onDrawBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos blockPos, BlockState blockState, int i, CallbackInfo ci) {
		if (!context.renderBlockOutline) {
			// Was cancelled before we got here, so do not
			// fire the BLOCK_OUTLINE event per contract of the API.
			ci.cancel();
		} else {
			context.prepareBlockOutline(entity, cameraX, cameraY, cameraZ, blockPos, blockState);

			if (!WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(context, context)) {
				ci.cancel();
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"))
	private VertexConsumer resetBlockOutlineBuffer(VertexConsumer vertexConsumer) {
		// The original VertexConsumer may have been ended during the block outlines event, so we
		// have to re-request it to prevent a crash when the vanilla block overlay is submitted.
		return context.consumers().getBuffer(RenderLayer.getLines());
	}

	@Inject(
			method = "method_62214",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V",
					ordinal = 0
			)
	)
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(context);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudRenderMode()Lnet/minecraft/client/option/CloudRenderMode;"))
	private void beforeClouds(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
		FramePass afterTranslucentPass = frameGraphBuilder.addPass("afterTranslucent");
		framebuffers.main = afterTranslucentPass.readsAndWrites(framebuffers.main);
	}

	@Inject(method = "method_62214", at = @At("RETURN"))
	private void onFinishWritingFramebuffer(CallbackInfo ci) {
		WorldRenderEvents.LAST.invoker().onLast(context);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.END.invoker().onEnd(context);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}

	@Inject(at = @At("HEAD"), method = "addWeatherAndWorldBorderRenderPass", cancellable = true)
	private void renderWeather(FrameGraphBuilder frameGraphBuilder, LightmapTextureManager lightmapTextureManager, Vec3d vec3d, float f, FogParameters fog, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.WeatherRenderer renderer = DimensionRenderingRegistry.getWeatherRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "addCloudRenderPass", cancellable = true)
	private void renderCloud(FrameGraphBuilder frameGraphBuilder, Matrix4f matrix4f, Matrix4f matrix4f2, CloudRenderMode cloudRenderMode, Vec3d vec3d, float f, int i, float g, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.CloudRenderer renderer = DimensionRenderingRegistry.getCloudRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "addSkyRenderPass", cancellable = true)
	private void renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, FogParameters fog, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.SkyRenderer renderer = DimensionRenderingRegistry.getSkyRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}
}
