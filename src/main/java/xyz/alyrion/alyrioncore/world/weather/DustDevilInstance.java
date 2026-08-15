package xyz.alyrion.alyrioncore.world.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.UUID;

public class DustDevilInstance {
    private final UUID id;
    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionZ;
    private final float radius;
    private final float height;
    private int age;
    private final int maxAge;

    public DustDevilInstance(UUID id, double x, double y, double z, double motionX, double motionZ, float radius, float height, int maxAge) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionZ = motionZ;
        this.radius = radius;
        this.height = height;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public static DustDevilInstance createRandom(double startX, double startZ, ServerLevel level, RandomSource random) {
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) startX, (int) startZ);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double speed = 0.08 + random.nextDouble() * 0.10;
        double mx = Math.cos(angle) * speed;
        double mz = Math.sin(angle) * speed;
        float r = 2.5F + random.nextFloat() * 2.0F; // 2.5 to 4.5 blocks radius
        float h = 18.0F + random.nextFloat() * 12.0F; // 18 to 30 blocks height
        int lifetime = 600 + random.nextInt(1200); // 30 to 90 seconds
        return new DustDevilInstance(UUID.randomUUID(), startX, groundY, startZ, mx, mz, r, h, lifetime);
    }

    public void tick(ServerLevel level) {
        this.age++;
        this.x += this.motionX;
        this.z += this.motionZ;

        // Subtle random wander drift
        if (this.age % 20 == 0) {
            double angleShift = (level.random.nextDouble() - 0.5) * 0.4;
            double currentSpeed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            double currentAngle = Math.atan2(this.motionZ, this.motionX) + angleShift;
            this.motionX = Math.cos(currentAngle) * currentSpeed;
            this.motionZ = Math.sin(currentAngle) * currentSpeed;
        }

        // Adjust Y to ground elevation smoothly
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) this.x, (int) this.z);
        if (groundY > 0) {
            this.y = this.y * 0.8 + groundY * 0.2;
        }
    }

    public boolean isAlive() {
        return this.age < this.maxAge;
    }

    public UUID getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getRadius() {
        return radius;
    }

    public float getHeight() {
        return height;
    }

    public int getAge() {
        return age;
    }

    public int getMaxAge() {
        return maxAge;
    }
}
