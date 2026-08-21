package xyz.alyrion.alyrioncore.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticDefinition;
import xyz.alyrion.alyrioncore.cosmetics.CosmeticType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Demo renderer for the TRAIL cosmetic type: spawns a fiery rocket-exhaust
 * particle trail behind the player — full rate while running, half rate while
 * standing still, plus a small boost explosion at the moment of a jump.
 *
 * <p>In the world it queues the real vanilla {@link ParticleTypes#FLAME} and
 * {@link ParticleTypes#SMOKE} particles. In the store's wardrobe preview the
 * same particles are drawn live onto the 3D character as camera-facing quads
 * using the real particle sprites (so the preview matches the in-game trail).
 */
public class TrailCosmeticRenderer implements CosmeticRenderer {

    /** Ticks between trail bursts while moving / while standing still. */
    private static final int TRAIL_INTERVAL_MOVING = 3;
    private static final int TRAIL_INTERVAL_IDLE = 6;
    /** Upward speed that marks the first moments of a jump. */
    private static final float JUMP_ASCENT_SPEED = 0.3F;
    /** Minimum ticks between two boost explosions for one player. */
    private static final int BOOST_COOLDOWN = 5;

    /** Fullbright so the preview flames glow exactly like the real ones. */
    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;

    /** Preview plume: idle burst rate and flame lifetime, matching the world. */
    private static final int PLUME_INTERVAL = 6;
    private static final int PLUME_LIFETIME = 26;
    private static final int SMOKE_INTERVAL = 18;
    private static final int SMOKE_LIFETIME = 34;

    private TextureAtlasSprite flameSprite;
    private TextureAtlasSprite[] smokeSprites;

    /** Per-player tick of the last trail burst / boost; render runs per frame. */
    private final Map<UUID, Integer> lastTrailTick = new HashMap<>();
    private final Map<UUID, Integer> lastBoostTick = new HashMap<>();

    @Override
    public void render(CosmeticRenderContext ctx, CosmeticDefinition cosmetic) {
        var player = ctx.player();
        if (player.isInvisible()) {
            return;
        }
        // In the wardrobe the store renders the live player through this same
        // layer, so when a TRAIL preview override is active we draw the real
        // flame/smoke particles directly onto the 3D character instead of
        // queuing world particles that the GUI can't show.
        if (CosmeticRenderLayer.isPreviewing(CosmeticType.TRAIL)) {
            renderPreviewPlume(ctx);
            return;
        }
        if (!(player.level() instanceof ClientLevel level)) {
            return;
        }
        if (lastTrailTick.size() > 256) {
            lastTrailTick.clear();
            lastBoostTick.clear();
        }

        int tick = player.tickCount;
        UUID id = player.getUUID();
        if (lastTrailTick.getOrDefault(id, -1) == tick) {
            return; // already emitted this tick
        }

        RandomSource random = player.getRandom();
        Vec3 motion = player.getDeltaMovement();
        double speed = motion.horizontalDistance();
        boolean moving = speed > 0.05;

        // Boost explosion: fire once, right as a jump starts lifting off.
        if (!player.onGround() && motion.y > JUMP_ASCENT_SPEED
                && tick - lastBoostTick.getOrDefault(id, -BOOST_COOLDOWN) >= BOOST_COOLDOWN) {
            lastBoostTick.put(id, tick);
            spawnBoostExplosion(level, player, random);
        }

        // Rate limit: full burst rate while moving, half while standing still.
        int interval = moving ? TRAIL_INTERVAL_MOVING : TRAIL_INTERVAL_IDLE;
        if (tick - lastTrailTick.getOrDefault(id, -interval) < interval) {
            return;
        }
        lastTrailTick.put(id, tick);

        // Trails push back opposite to movement, gently.
        Vec3 back = moving
                ? new Vec3(-motion.x * 0.25, 0.0, -motion.z * 0.25)
                : Vec3.ZERO;

        for (int i = 0; i < 3; i++) {
            double px = player.getX() + (random.nextDouble() - 0.5) * 0.4 + back.x;
            double py = player.getY() + 0.3 + random.nextDouble() * 0.7;
            double pz = player.getZ() + (random.nextDouble() - 0.5) * 0.4 + back.z;
            level.addParticle(ParticleTypes.FLAME, px, py, pz,
                    back.x + (random.nextDouble() - 0.5) * 0.05, 0.02, back.z + (random.nextDouble() - 0.5) * 0.05);
        }
        if (random.nextInt(3) == 0) {
            double px = player.getX() + back.x;
            double py = player.getY() + 0.8;
            double pz = player.getZ() + back.z;
            level.addParticle(ParticleTypes.SMOKE, px, py, pz, back.x * 0.5, 0.05, back.z * 0.5);
        }
    }

    /** Small ring-shaped exhaust burst under the feet when a jump starts. */
    private void spawnBoostExplosion(ClientLevel level, Player player, RandomSource random) {
        double x = player.getX();
        double y = player.getY() + 0.2;
        double z = player.getZ();

        level.addParticle(ParticleTypes.POOF, x, y, z, 0.0, 0.0, 0.0);
        for (int i = 0; i < 8; i++) {
            float angle = i / 8.0F * Mth.TWO_PI;
            level.addParticle(ParticleTypes.FLAME, x, y, z,
                    Mth.cos(angle) * 0.25, -0.05, Mth.sin(angle) * 0.25);
        }
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.SMOKE,
                    x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3,
                    0.0, -0.02, 0.0);
        }
    }

    /** Three flame dots for the store list row. */
    @Override
    public void drawStoreIcon(GuiGraphics guiGraphics, CosmeticDefinition cosmetic, int x, int y, int size, long tick) {
        guiGraphics.fill(x + size / 2 - 2, y + 1, x + size / 2 + 2, y + size - 1, 0xFFFF7733);
        guiGraphics.fill(x + size / 2 - 1, y - 1, x + size / 2 + 1, y + 2, 0xFFFFB84D);
        guiGraphics.fill(x + size / 2 - 3, y + size - 4, x + size / 2 - 1, y + size - 1, 0xFFCC4E00);
        guiGraphics.fill(x + size / 2 + 1, y + size - 4, x + size / 2 + 3, y + size - 1, 0xFFCC4E00);
    }

    /**
     * Draws the real in-game trail particles (flame + smoke sprites, fullbright
     * flame look) as camera-facing quads attached to the 3D character. Runs
     * only while the wardrobe renders the local player with a TRAIL preview.
     *
     * <p>The layer poseStack is in camera space with the entity transforms
     * baked in, so the camera/GUI-space basis is mapped back into model space
     * via the inverse pose matrix; that keeps the quads facing the viewer in
     * both the world render and the store's inventory-screen render. Emission
     * mirrors the in-world idle behaviour (bursts every 6 ticks at body height)
     * and is stateless — rebuilt deterministically from the age each frame.
     */
    private void renderPreviewPlume(CosmeticRenderContext ctx) {
        float time = ctx.ageInTicks();
        int tick = Mth.floor(time);

        PoseStack poseStack = ctx.poseStack();
        poseStack.pushPose();

        Matrix4f inv = new Matrix4f(poseStack.last().pose()).invert();
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
        inv.transformDirection(right);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        inv.transformDirection(up);
        if (up.y() < 0.0F) {
            up.mul(-1.0F); // keep the flame upright toward the character's head
        }
        right.normalize();
        up.normalize();

        VertexConsumer buffer = ctx.buffer().getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_PARTICLES));

        // Exhaust bursts at the idle in-world rate: 3 flames every 6 ticks,
        // hugging the body at chest-to-waist height, drifting up and jittering.
        int firstFlame = (tick - PLUME_LIFETIME + 1 + PLUME_INTERVAL - 1) / PLUME_INTERVAL * PLUME_INTERVAL;
        for (int emitTick = firstFlame; emitTick <= tick; emitTick += PLUME_INTERVAL) {
            int age = tick - emitTick;
            float t = age / (float) PLUME_LIFETIME;
            for (int i = 0; i < 3; i++) {
                long seed = emitTick * 31L + i;
                float x = (hash01(seed) - 0.5F) * 0.8F + (hash01(seed + 3) - 0.5F) * 0.06F * age;
                float y = 0.3F + hash01(seed + 2) * 0.7F + (0.02F + hash01(seed + 4) * 0.04F) * age;
                float z = (hash01(seed + 1) - 0.5F) * 0.8F + (hash01(seed + 5) - 0.5F) * 0.06F * age;
                float size = (0.12F + hash01(seed + 6) * 0.08F) * (1.0F - t * t * 0.5F);
                drawBillboard(buffer, inv, right, up, x, y, z, size, flameSprite(), 1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        // Occasional smoke (mirrors the in-world 1-in-3 chance), drifting up
        // slower while growing and cycling through the smoke animation frames.
        int firstSmoke = (tick - SMOKE_LIFETIME + 1 + SMOKE_INTERVAL - 1) / SMOKE_INTERVAL * SMOKE_INTERVAL;
        for (int emitTick = firstSmoke; emitTick <= tick; emitTick += SMOKE_INTERVAL) {
            int age = tick - emitTick;
            long seed = emitTick * 31L + 9;
            float x = (hash01(seed) - 0.5F) * 0.5F + (hash01(seed + 1) - 0.5F) * 0.04F * age;
            float y = 0.55F + 0.02F * age;
            float z = (hash01(seed + 2) - 0.5F) * 0.5F + (hash01(seed + 3) - 0.5F) * 0.04F * age;
            float size = 0.08F + 0.2F * Math.min(1.0F, age / 20.0F);
            drawBillboard(buffer, inv, right, up, x, y, z, size,
                    smokeSprite(age / 4), 0.85F, 0.85F, 0.85F, 1.0F);
        }

        // Small foot-level poof every 60 ticks to mirror the jump boost.
        int boostAge = tick % 60;
        if (boostAge < 10) {
            float t = boostAge / 10.0F;
            float alpha = 1.0F - t;
            for (int i = 0; i < 8; i++) {
                float ang = i / 8.0F * Mth.TWO_PI;
                float rad = 0.05F + t * 0.35F;
                drawBillboard(buffer, inv, right, up,
                        Mth.cos(ang) * rad, 0.15F + t * 0.3F, Mth.sin(ang) * rad,
                        0.12F, flameSprite(), 1.0F, 1.0F, 1.0F, alpha);
            }
        }

        poseStack.popPose();
    }

    /** One camera-facing flame/smoke quad at a world-space offset from the feet. */
    private void drawBillboard(VertexConsumer buffer, Matrix4f inv, Vector3f right, Vector3f up,
                               float x, float y, float z, float size,
                               TextureAtlasSprite sprite, float r, float g, float b, float a) {
        Vector3f c = new Vector3f(x, y, z);
        inv.transformPosition(c);
        float rx = right.x * size, ry = right.y * size, rz = right.z * size;
        float ux = up.x * size, uy = up.y * size, uz = up.z * size;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        buffer.addVertex(c.x + rx - ux, c.y + ry - uy, c.z + rz - uz).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, -1.0F);
        buffer.addVertex(c.x + rx + ux, c.y + ry + uy, c.z + rz + uz).setColor(r, g, b, a).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, -1.0F);
        buffer.addVertex(c.x - rx + ux, c.y - ry + uy, c.z - rz + uz).setColor(r, g, b, a).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, -1.0F);
        buffer.addVertex(c.x - rx - ux, c.y - ry - uy, c.z - rz - uz).setColor(r, g, b, a).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, -1.0F);
    }

    private TextureAtlasSprite flameSprite() {
        if (flameSprite == null) {
            flameSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_PARTICLES)
                    .apply(ResourceLocation.withDefaultNamespace("flame"));
        }
        return flameSprite;
    }

    private TextureAtlasSprite smokeSprite(int frame) {
        if (smokeSprites == null) {
            smokeSprites = new TextureAtlasSprite[8];
            var sprites = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_PARTICLES);
            for (int i = 0; i < 8; i++) {
                smokeSprites[i] = sprites.apply(ResourceLocation.withDefaultNamespace("generic_" + i));
            }
        }
        return smokeSprites[frame & 7];
    }

    /** Deterministic [0,1) hash so the preview is stateless but stable per tick. */
    private static float hash01(long n) {
        long h = n * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 32)) * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 29;
        return (h & 0xFFFF) / 65536.0F;
    }
}
