package com.velentium.android.platformv.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.zip.CRC32

//region Properties / Constants

val defaultCharset = Charsets.US_ASCII

val ByteArray.asHex: String
    get() = this
        .toHexString(format = "%02X")
        .chunked(8)
        .joinToString(" ")
        .chunked(45)
        .joinToString("\n")

//endregion

//region Boolean extensions

/***
 * Converts a [Boolean] to byte. [bitIndex] is left to right
 * i.e. true, [bitIndex] 0 -> 0b10000000
 *      true, [bitIndex] 7 -> 0b00000001
 */
fun Boolean.toByte(bitIndex: Int = 7): Byte {
    return (if (this) 1 else 0).shl(7 - bitIndex).toByte()
}

//endregion

//region BooleanArray extensions

/***
 * Given a Boolean Array converts it to byte.
 * Assumes first index is correspondent to the 0 index of the byte
 * i.e.
 * ( true, false, false, true, true )
 * will be
 * 0b10011000
 */
fun BooleanArray.toByte(): Byte {
    if (this.size > 8) {
        throw Exception()
    }

    var byteValue = 0
    for (index in this.indices) {
        byteValue += this[index].toByte().toInt().shl(7 - index)
    }
    return byteValue.toByte()
}

/**
 * Converts [BooleanArray] to a [ByteArray] where each byte represents 8 [Boolean]s in the
 * [BooleanArray]
 *
 * i.e.
 * Booleans 0..7   -> 0th Byte
 * Booleans 8..15  -> 1st Byte
 * .
 * .
 * Booleans n..n+7 -> n/8th Byte
 */
fun BooleanArray.toByteArray(): ByteArray {
    val byteArray = MutableList<Byte>(size = 0) { 0 }
    var cur = 0
    while (cur + 8 < this.size) {
        byteArray.add(this.copyOfRange(fromIndex = cur, toIndex = cur + 8).toByte())
        cur += 8
    }

    if (cur < this.size) {
        byteArray.add(this.copyOfRange(fromIndex = cur, toIndex = this.size).toByte())
    }
    return byteArray.toByteArray()
}

/**
 * Converts [BooleanArray] to a [ByteArray] where each byte represents each [Boolean]
 * @param [bitIndex] checkout [toByte] for more details.
 */
fun BooleanArray.toSingleByteArray(bitIndex: Int = 7): ByteArray {
    return this.map { it.toByte(bitIndex = bitIndex) }.toByteArray()
}

//endregion

//region Byte extensions

fun Byte.toUnsignedInt() = toInt() and 0xFF

/**
 * Given a byte, returns the unsigned integer representation of the bits in the given range.
 *
 * i.e.
 * input: 10000000 -> range: 0,1 -> output: 0
 * input: 10000000 -> range: 2,7 -> output: 64
 * input: 00000001 -> range: 0,1 -> output: 1
 * input: 00000001 -> range: 7,8 -> output: 0
 * input: 10010111 -> range: 3,6 -> output: 2
 *
 *
 * TODO: check if this is correct per comment:
 * Byte 17 holds the battery and amplitude level like so:  0b000BBBAA, and is coming through as 40 or 0x28:
 * Following the formula you used we get:
 * (40 << 3) & 0xFF == 64
 * 64 >> (8 - (6 - 3) == 2
 * They're doing this:
 * (40 >> 3) & 0x7  == 5, or just (40 >> 3)
 * (0x7 == 0b00000111)
 */
fun Byte.toUnsignedInt(fromIndex: Int, toIndex: Int): Int {
    var bitMask = 0
    for (i in 7 downTo 0) {
        if (i in fromIndex until toIndex) {
            bitMask += 1
        }
        if (i != 0) {
            bitMask = bitMask.shl(1)
        }
    }

    return (this.toInt() and bitMask) shr fromIndex
}

/**
 * Given a byte, converts it to an unsigned integer, then to a long value.
 * For more details for the conversion to unsigned int, check [toUnsignedInt]
 */
fun Byte.toUnsignedLong() = toUnsignedInt().toLong()

/**
 * Given a byte, converts it to a boolean array
 * i.e. 0x98 -> 0b10011000
 * will be
 * ( true, false, false, true, true, false, false, false )
 */
fun Byte.toBooleanArray(): BooleanArray {
    val booleanArray = BooleanArray(8)
    for (index in 0 until 8) {
        booleanArray[index] = this.toInt().and(1.shl(7 - index)).shr(7 - index) == 1
    }
    return booleanArray
}

/**
 * Given a byte, converts it to a boolean.
 * The bit index is left to right (to be consistent with [Byte.toBoolean])
 *
 * i.e. 0x98 -> 0b10011000, so
 * 0x98.toBoolean(bitIndex = 0) -> 1
 * 0x98.toBoolean(bitIndex = 7) -> 0
 *
 * Default for [bitIndex] is 7.
 */
fun Byte.toBoolean(bitIndex: Int = 7): Boolean {
    return this.toBooleanArray()[bitIndex]
}

//endregion

//region ByteArray extensions

fun ByteArray.toHexString(format: String = "0x%02X,"): String {
    return if (!isEmpty()) {
        StringBuffer()
            .let { buffer ->
                this.forEach { b ->
                    buffer.append(String.format(format, b))
                }
                buffer
            }
            .toString()
            .trim(',')
    } else {
        ""
    }
}

fun ByteArray.withCrc32(): ByteArray {
    val crC32 = CRC32()
    crC32.update(this)
    val reducedCRC = ByteArray(4)
    reducedCRC[0] = crC32.value.toByte()
    reducedCRC[1] = (crC32.value.shr(8).toByte())
    reducedCRC[2] = (crC32.value.shr(16).toByte())
    reducedCRC[3] = (crC32.value.shr(24).toByte())
    return this + reducedCRC
}

fun ByteArray.calcCrc32(): ByteArray {
    val crC32 = CRC32()
    crC32.update(this)
    val reducedCRC = ByteArray(4)
    reducedCRC[0] = crC32.value.toByte()
    reducedCRC[1] = (crC32.value.shr(8).toByte())
    reducedCRC[2] = (crC32.value.shr(16).toByte())
    reducedCRC[3] = (crC32.value.shr(24).toByte())
    return reducedCRC
}

fun ByteArray.toInt(): Int {
    if (this.size > 4) {
        throw Exception()
    }

    return this.foldIndexed(initial = 0) { index, acc, byte ->
        acc + byte.toInt().shl(8 * index)
    }
}

fun ByteArray.toUInt16(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): Int {
    if (this.size > 2) {
        throw Exception()
    }

    return if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
        this.foldIndexed(initial = 0) { index, acc, byte ->
            acc + byte.toUnsignedInt().shl(8 * index)
        }
    } else {
        this.foldIndexed(initial = 0) { index, acc, byte ->
            acc + byte.toUnsignedInt().shl(8 * (lastIndex - index))
        }
    }
}

fun ByteArray.toUInt32(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): Long {
    if (this.size > 4) {
        throw Exception()
    }

    return if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
        this.foldIndexed(initial = 0L) { index, acc, byte ->
            acc + byte.toUnsignedLong().shl(8 * index)
        }
    } else {
        this.foldIndexed(initial = 0L) { index, acc, byte ->
            acc + byte.toUnsignedLong().shl(8 * (lastIndex - index))
        }
    }
}

fun ByteArray.toLong(): Long {
    if (this.size > 8) {
        throw Exception()
    }

    var longValue = 0L

    for (index in 0..this.lastIndex) {
        longValue += this[index].toUnsignedLong().shl(8 * index)
    }

    return longValue
}

fun ByteArray.toIntArray(size: Int = 4): IntArray {
    if (size < 1 || size > 4) {
        throw RuntimeException("Size must be 1 to 4")
    }

    if (this.size % size != 0) {
        throw RuntimeException("Not enough bytes")
    }

    if (this.isEmpty()) {
        return IntArray(0)
    }

    val intList = ArrayList<Int>()

    for (i in 0 until this.size / size) {
        intList.add(this.copyOfRange(fromIndex = i * size, toIndex = (i + 1) * size).toInt())
    }
    return intList.toIntArray()
}

fun ByteArray.toUInt16Array(): IntArray {
    if (this.size % 2 != 0) {
        throw RuntimeException("Not enough bytes")
    }

    if (this.isEmpty()) {
        return IntArray(0)
    }

    val intList = ArrayList<Int>()

    for (i in 0 until this.size / 2) {
        intList.add(this.copyOfRange(fromIndex = i * 2, toIndex = (i + 1) * 2).toUInt16())
    }
    return intList.toIntArray()
}

fun ByteArray.toUInt32Array(): LongArray {
    if (this.size % 4 != 0) {
        throw RuntimeException("Not enough bytes")
    }

    if (this.isEmpty()) {
        return LongArray(0)
    }

    val intList = ArrayList<Long>()

    for (i in 0 until this.size / 4) {
        intList.add(this.copyOfRange(fromIndex = i * 4, toIndex = (i + 1) * 4).toUInt32())
    }
    return intList.toLongArray()
}

/**
 * Compute the SHA-1 hash of the given byte array
 * @return byte[]
 */
fun ByteArray.hash(): ByteArray {
    try {
        val hash: ByteArray
        val md = MessageDigest.getInstance("SHA-1")

        hash = md.digest(this)
        return hash
    } catch (nsae: NoSuchAlgorithmException) {
        System.err.println("SHA-1 algorithm is not available...")
        System.exit(2)
    }
    return ByteArray(20)
}

fun ByteArray.copyOfRange(fromIndex: Int, size: Int): ByteArray {
    return this.copyOfRange(fromIndex = fromIndex, toIndex = fromIndex + size)
}

fun ByteArray.toDoubleWithBitPattern(): Double {
    return Double.fromBits(this.toLong())
}

fun ByteArray.toDoubleArrayWithBitPattern(): DoubleArray {
    if (this.size % 8 != 0) {
        throw Exception()
    } else {
        val doubleList = mutableListOf<Double>()
        for (index in 0 until this.size / 8) {
            doubleList.add(
                this.copyOfRange(fromIndex = index * 8, size = 8)
                    .toDoubleWithBitPattern()
            )
        }
        return doubleList.toDoubleArray()
    }
}

fun ByteArray.toPaddedString(
    charset: Charset = defaultCharset,
    size: Int = 12,
    reverse: Boolean = true
): String {
    if (reverse) {
        this.reverse()
    }

    return String(bytes = this.copyOfRange(fromIndex = 0, size = size), charset = charset)
}

/**
 * Given a byte array converts it to a string.
 *
 * If [padByte] is specified, it will truncate at the start of the padByte.
 */
fun ByteArray.toNonPaddedString(
    charset: Charset = defaultCharset,
    size: Int = 12,
    reverse: Boolean = true,
    padByte: Byte? = null
): String {
    if (reverse) {
        this.reverse()
    }

    var nonPadIndex = size
    padByte?.run {
        for (index in size - 1 downTo 0) {
            if (this@toNonPaddedString[index] != padByte) {
                break
            }
            nonPadIndex = index
        }
    }

    val nonPadded = this.copyOfRange(fromIndex = 0, size = nonPadIndex)
    return String(bytes = nonPadded, charset = charset)
}

//TODO: Add Endianness?
/**
 * Creates a [BooleanArray] by converting each [Byte] in the [ByteArray] to 8 [Boolean]s
 */
fun ByteArray.toBooleanArray(): BooleanArray {
    return this.fold(BooleanArray(0)) { acc: BooleanArray, byte: Byte ->
        acc + byte.toBooleanArray()
    }
}

/**
 * Creates a [BooleanArray] by converting each [Byte] to a single [Boolean].
 * @param [bitIndex] checkout [toByte] for more details.
 */
fun ByteArray.toSingleBooleanArray(bitIndex: Int): BooleanArray {
    return this.map { it.toBoolean(bitIndex = bitIndex) }.toBooleanArray()
}

/**
 * Trims the [remove] from the end of the [original] bytes.
 */
fun ByteArray.trimBytes(remove: ByteArray): ByteArray {
    return if (this.count() > remove.count()
        && this.copyOfRange(
            fromIndex = this.count() - remove.count(),
            toIndex = this.count()
        ).contentEquals(remove)
    ) {
        // strip off the bogus bytes at the end
        this.copyOfRange(
            fromIndex = 0,
            toIndex = this.count() - remove.count()
        )
    } else {
        this
    }
}

//endregion

//region Int extensions

/**
 * @param size - size of the resulting array
 * @param byteOrder - endianness when putting it into the byte array
 */
fun Int.toByteArray(size: Int = 4, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
    val byteArray = ByteBuffer.allocate(4)
        .order(byteOrder)
        .putInt(this)
        .array()

    return if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
        byteArray.copyOfRange(fromIndex = 0, toIndex = size)
    } else {
        byteArray.copyOfRange(fromIndex = 4 - size, toIndex = 4)
    }

}

//endregion

//region IntArray/Array<Int> extensions

fun Array<Int>.toByteArray(): ByteArray {
    return this.foldIndexed(ByteArray(this.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
}

/**
 * Use this when the originating int array consists of ints that represent a byte or more
 * @param size - number of bytes to be represented by each int
 * @param byteOrder - endianness when putting it into the byte array
 */
fun IntArray.toByteArray(
    size: Int = 4,
    byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
): ByteArray {
    return if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
        this.foldRight(
            initial = ByteArray(0),
            operation = { i: Int, acc: ByteArray -> acc + i.toByteArray(size = size) })
    } else {
        this.fold(
            initial = ByteArray(0),
            operation = { acc: ByteArray, i: Int -> acc + i.toByteArray(size = size) })
    }
}

/**
 * Use this when the originating int array consists of ints that represent a part of a byte
 * @param size - number of ints represented by each byte
 * @param byteOrder - endianness when putting it into the byte array
 */
@Suppress("UNUSED_PARAMETER")
fun IntArray.toCondensedByteArray(
    size: Int = 4,
    byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
): ByteArray {
    val byteArray = ByteArray(this.size / size)

    for (i in 0 until byteArray.size) {
        var newByte = 0
        for (j in 0 until size) {
            newByte += this[i * size + j].shl(2 * j)
        }
        byteArray[i] = newByte.toByte()
    }

    return byteArray
}

//endregion

//region Long extensions

/**
 * @param size - size of the resulting array
 * @param byteOrder - endianness when putting it into the byte array
 */
fun Long.toByteArray(size: Int = 4, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
    val byteArray = ByteBuffer.allocate(8)
        .order(byteOrder)
        .putLong(this)
        .array()

    if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
        return byteArray.copyOfRange(fromIndex = 0, toIndex = size)
    } else {
        return byteArray.copyOfRange(fromIndex = 8 - size, toIndex = 8)
    }

}

//endregion

//region Double extensions

fun Double.toByteArrayWithBitPattern(byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
    return this.toRawBits().toByteArray(size = 8, byteOrder = byteOrder)
}

//endregion

//region DoubleArray extensions

//TODO: Add Endianness?
fun DoubleArray.toByteArrayWithBitPattern(): ByteArray {
    return this.fold(initial = ByteArray(0)) { acc: ByteArray, d: Double ->
        acc + d.toByteArrayWithBitPattern()
    }
}

//endregion