package com.example.smartlist.extend_functions

import java.util.Locale

fun convertStringToNumber(input: String): Int {
    val numberMap = mapOf(
        "ноль" to 0,
        "один" to 1,
        "два" to 2,
        "три" to 3,
        "четыре" to 4,
        "пять" to 5,
        "шесть" to 6,
        "семь" to 7,
        "восемь" to 8,
        "девять" to 9,
        "десять" to 10,
        "одиннадцать" to 11,
        "двенадцать" to 12,
        "тринадцать" to 13,
        "четырнадцать" to 14,
        "пятнадцать" to 15,
        "шестнадцать" to 16,
        "семнадцать" to 17,
        "восемнадцать" to 18,
        "девятнадцать" to 19,
        "двадцать" to 20,
        "тридцать" to 30,
        "сорок" to 40,
        "пятьдесят" to 50,
        "шестьдесят" to 60,
        "семьдесят" to 70,
        "восемьдесят" to 80,
        "девяносто" to 90,
        "сто" to 100,
        "двести" to 200,
        "триста" to 300,
        "четыреста" to 400,
        "пятьсот" to 500,
        "шестьсот" to 600,
        "семьсот" to 700,
        "восемьсот" to 800,
        "девятьсот" to 900,
        "тысяча" to 1000,
        "тысячи" to 1000,
        "тысяч" to 1000,
        "миллион" to 1000000,
        "миллиона" to 1000000,
        "миллионов" to 1000000,
        "миллиард" to 1000000000,
        "миллиарда" to 1000000000,
        "миллиардов" to 1000000000
    )

    val numberString = input.replace(",", ".").lowercase(Locale.getDefault())
    var total = 0
    var currentNumber = 0

    val parts = numberString.split(" ")

    for (part in parts) {
        val numberValue = numberMap[part]
        if (numberValue != null) {
            currentNumber += numberValue
        } else if (part == "и" || part == "ноль") {
            continue
        } else if (part == "тысяч" || part == "миллионов" || part == "миллиардов") {
            total += currentNumber * numberMap[part]!!
            currentNumber = 0
        } else if (part == "десять" || part == "сто" || part == "тысяча" || part == "миллион" || part == "миллиард") {
            currentNumber *= numberMap[part]!!
        }
    }

    return total + currentNumber
}