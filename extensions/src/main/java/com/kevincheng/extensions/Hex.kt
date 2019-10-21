package com.kevincheng.extensions

import java.math.BigInteger

val hexTable = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

val String.asciiToHex inline get() = this.toCharArray().joinToString("") { it.toInt().toHex }

val String.utf8ToHex inline get() = this.toByteArray(Charsets.UTF_8).joinToString("") { it.toHex }

val Int.toHex: String
    inline get() = this.toLong().toHex

val Long.toHex: String
    inline get() = BigInteger.valueOf(this).toByteArray().toHex

val ByteArray.toHex: String inline get() = this.joinToString("") { it.toHex }

val Byte.toHex: String
    inline get() {
        val value = this.toInt() and 0xFF
        return CharArray(2).apply {
            this[0] = hexTable[value.ushr(4)]
            this[1] = hexTable[value and 0x0F]
        }.let { String(it) }
    }

val Byte.toBinary inline get() = String.format("%08d", Integer.parseInt(BigInteger(this.toHex, 16).toString(2)))