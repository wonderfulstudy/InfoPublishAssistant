package com.jyf.infopublishassistant.data.local

/**
 * [com.jyf.infopublishassistant.domain.model.DrawResult.numbers] 与数据库文本列之间的编解码。
 *
 * 采用「区间用 | 分隔、号码用 , 分隔」的紧凑格式（例如 `3,11,19,27,34|5,12`），
 * 避免为嵌套 List 引入 Room TypeConverter，也便于直接读库排查问题。
 */
object DrawNumbersCodec {

    private const val ZONE_SEPARATOR = "|"
    private const val NUMBER_SEPARATOR = ","

    fun encode(numbers: List<List<Int>>): String =
        numbers.joinToString(ZONE_SEPARATOR) { zone -> zone.joinToString(NUMBER_SEPARATOR) }

    fun decode(encoded: String): List<List<Int>> =
        if (encoded.isEmpty()) {
            emptyList()
        } else {
            encoded.split(ZONE_SEPARATOR).map { zone ->
                zone.split(NUMBER_SEPARATOR).map { number -> number.trim().toInt() }
            }
        }
}
