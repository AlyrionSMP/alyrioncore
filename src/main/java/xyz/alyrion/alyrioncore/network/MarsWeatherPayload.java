package xyz.alyrion.alyrioncore.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.alyrion.alyrioncore.AlyrionCore;

import java.util.ArrayList;
import java.util.List;

public record MarsWeatherPayload(
        int weatherStateOrdinal,
        float stormIntensity,
        float windAngle,
        float windSpeed,
        int seasonSol,
        List<DustDevilData> dustDevils
) implements CustomPacketPayload {

    public static final Type<MarsWeatherPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AlyrionCore.MODID, "s2c_mars_weather"));

    public record DustDevilData(double x, double y, double z, float radius, float height) {
        public static final StreamCodec<ByteBuf, DustDevilData> STREAM_CODEC = StreamCodec.of(
                (buf, data) -> {
                    buf.writeDouble(data.x());
                    buf.writeDouble(data.y());
                    buf.writeDouble(data.z());
                    buf.writeFloat(data.radius());
                    buf.writeFloat(data.height());
                },
                buf -> new DustDevilData(
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readDouble(),
                        buf.readFloat(),
                        buf.readFloat()
                )
        );
    }

    public static final StreamCodec<ByteBuf, MarsWeatherPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.weatherStateOrdinal());
                buf.writeFloat(payload.stormIntensity());
                buf.writeFloat(payload.windAngle());
                buf.writeFloat(payload.windSpeed());
                buf.writeInt(payload.seasonSol());
                buf.writeInt(payload.dustDevils().size());
                for (DustDevilData dd : payload.dustDevils()) {
                    DustDevilData.STREAM_CODEC.encode(buf, dd);
                }
            },
            buf -> {
                int stateOrd = buf.readInt();
                float intensity = buf.readFloat();
                float windAngle = buf.readFloat();
                float windSpeed = buf.readFloat();
                int sol = buf.readInt();
                int count = buf.readInt();
                List<DustDevilData> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(DustDevilData.STREAM_CODEC.decode(buf));
                }
                return new MarsWeatherPayload(stateOrd, intensity, windAngle, windSpeed, sol, list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
