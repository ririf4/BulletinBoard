package net.ririfa.bulletinboard.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.text.SimpleDateFormat
import java.util.*

object ShortUUIDSerializer : KSerializer<ShortUUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ShortUUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ShortUUID) {
        encoder.encodeString(value.toShortString())
    }

    override fun deserialize(decoder: Decoder): ShortUUID {
        return ShortUUID.fromShortString(decoder.decodeString())
    }
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

object ComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Component) {
        val json = GsonComponentSerializer.gson().serialize(value)
        encoder.encodeString(json)
    }

    override fun deserialize(decoder: Decoder): Component {
        val json = decoder.decodeString()
        return GsonComponentSerializer.gson().deserialize(json)
    }
}

object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy:MM:dd_HH:mm:ss")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        val string = dateFormat.format(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Date {
        val string = decoder.decodeString()
        return dateFormat.parse(string)
    }
}