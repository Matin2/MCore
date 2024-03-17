package me.matin.core.function.adventure

class AdventureLegacy {

    fun convert(legacyText: String, colorCodes: String): String {
        val stringBuilder = StringBuilder()
        val chars = legacyText.toCharArray()
        val codes = colorCodes.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (isColorCode(chars[i], codes)) {
                if (i + 1 < chars.size) {
                    when (chars[i + 1]) {
                        '0' -> stringBuilder.append("<black>")
                        '1' -> stringBuilder.append("<dark_blue>")
                        '2' -> stringBuilder.append("<dark_green>")
                        '3' -> stringBuilder.append("<dark_aqua>")
                        '4' -> stringBuilder.append("<dark_red>")
                        '5' -> stringBuilder.append("<dark_purple>")
                        '6' -> stringBuilder.append("<gold>")
                        '7' -> stringBuilder.append("<gray>")
                        '8' -> stringBuilder.append("<dark_gray>")
                        '9' -> stringBuilder.append("<blue>")
                        'a' -> stringBuilder.append("<green>")
                        'b' -> stringBuilder.append("<aqua>")
                        'c' -> stringBuilder.append("<red>")
                        'd' -> stringBuilder.append("<light_purple>")
                        'e' -> stringBuilder.append("<yellow>")
                        'f' -> stringBuilder.append("<white>")
                        'r' -> stringBuilder.append("<reset><!italic>")
                        'l' -> stringBuilder.append("<bold>")
                        'm' -> stringBuilder.append("<strikethrough>")
                        'o' -> stringBuilder.append("<italic>")
                        'n' -> stringBuilder.append("<underlined>")
                        'k' -> stringBuilder.append("<obfuscated>")
                        'x' -> {
                            if (i + 13 >= chars.size || !isColorCode(chars[i + 2], codes)
                                || !isColorCode(chars[i + 4], codes)
                                || !isColorCode(chars[i + 6], codes)
                                || !isColorCode(chars[i + 8], codes)
                                || !isColorCode(chars[i + 10], codes)
                                || !isColorCode(chars[i + 12], codes)
                            ) {
                                stringBuilder.append(chars[i])
                                i++
                                continue
                            }
                            stringBuilder
                                .append("<#")
                                .append(chars[i + 3])
                                .append(chars[i + 5])
                                .append(chars[i + 7])
                                .append(chars[i + 9])
                                .append(chars[i + 11])
                                .append(chars[i + 13])
                                .append(">")
                            i += 13
                        }

                        else -> {
                            stringBuilder.append(chars[i])
                            i++
                            continue
                        }
                    }
                    i++
                } else {
                    stringBuilder.append(chars[i])
                }
            } else {
                stringBuilder.append(chars[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun convertColor(legacyText: String, colorCodes: String): String {
        val stringBuilder = StringBuilder()
        val chars = legacyText.toCharArray()
        val codes = colorCodes.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (isColorCode(chars[i], codes)) {
                if (i + 1 < chars.size) {
                    when (chars[i + 1]) {
                        '0' -> stringBuilder.append("<black>")
                        '1' -> stringBuilder.append("<dark_blue>")
                        '2' -> stringBuilder.append("<dark_green>")
                        '3' -> stringBuilder.append("<dark_aqua>")
                        '4' -> stringBuilder.append("<dark_red>")
                        '5' -> stringBuilder.append("<dark_purple>")
                        '6' -> stringBuilder.append("<gold>")
                        '7' -> stringBuilder.append("<gray>")
                        '8' -> stringBuilder.append("<dark_gray>")
                        '9' -> stringBuilder.append("<blue>")
                        'a' -> stringBuilder.append("<green>")
                        'b' -> stringBuilder.append("<aqua>")
                        'c' -> stringBuilder.append("<red>")
                        'd' -> stringBuilder.append("<light_purple>")
                        'e' -> stringBuilder.append("<yellow>")
                        'f' -> stringBuilder.append("<white>")
                        'x' -> {
                            if (i + 13 >= chars.size || !isColorCode(chars[i + 2], codes)
                                || !isColorCode(chars[i + 4], codes)
                                || !isColorCode(chars[i + 6], codes)
                                || !isColorCode(chars[i + 8], codes)
                                || !isColorCode(chars[i + 10], codes)
                                || !isColorCode(chars[i + 12], codes)
                            ) {
                                stringBuilder.append(chars[i])
                                i++
                                continue
                            }
                            stringBuilder
                                .append("<#")
                                .append(chars[i + 3])
                                .append(chars[i + 5])
                                .append(chars[i + 7])
                                .append(chars[i + 9])
                                .append(chars[i + 11])
                                .append(chars[i + 13])
                                .append(">")
                            i += 13
                        }

                        else -> {
                            stringBuilder.append(chars[i])
                            i++
                            continue
                        }
                    }
                    i++
                } else {
                    stringBuilder.append(chars[i])
                }
            } else {
                stringBuilder.append(chars[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun convertFormatting(legacyText: String, colorCodes: String): String {
        val stringBuilder = StringBuilder()
        val chars = legacyText.toCharArray()
        val codes = colorCodes.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (isColorCode(chars[i], codes)) {
                if (i + 1 < chars.size) {
                    when (chars[i + 1]) {
                        'l' -> stringBuilder.append("<bold>")
                        'm' -> stringBuilder.append("<strikethrough>")
                        'o' -> stringBuilder.append("<italic>")
                        'n' -> stringBuilder.append("<underlined>")
                        else -> {
                            stringBuilder.append(chars[i])
                            i++
                            continue
                        }
                    }
                    i++
                } else {
                    stringBuilder.append(chars[i])
                }
            } else {
                stringBuilder.append(chars[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun convertMagic(legacyText: String, colorCodes: String): String {
        val stringBuilder = StringBuilder()
        val chars = legacyText.toCharArray()
        val codes = colorCodes.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (isColorCode(chars[i], codes)) {
                if (i + 1 < chars.size) {
                    if (chars[i + 1] == 'k') {
                        stringBuilder.append("<obfuscated>")
                    } else {
                        stringBuilder.append(chars[i])
                        i++
                        continue
                    }
                    i++
                } else {
                    stringBuilder.append(chars[i])
                }
            } else {
                stringBuilder.append(chars[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    fun convertReset(legacyText: String, colorCodes: String): String {
        val stringBuilder = StringBuilder()
        val chars = legacyText.toCharArray()
        val codes = colorCodes.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (isColorCode(chars[i], codes)) {
                if (i + 1 < chars.size) {
                    if (chars[i + 1] == 'r') {
                        stringBuilder.append("<reset><!italic>")
                    } else {
                        stringBuilder.append(chars[i])
                        i++
                        continue
                    }
                    i++
                } else {
                    stringBuilder.append(chars[i])
                }
            } else {
                stringBuilder.append(chars[i])
            }
            i++
        }
        return stringBuilder.toString()
    }

    private fun isColorCode(character: Char, colorCodes: CharArray): Boolean {
        var result = false
        for (c in colorCodes) {
            if (character == c) {
                result = true
                break
            }
        }
        return result
    }
}