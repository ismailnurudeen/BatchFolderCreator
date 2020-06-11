package com.nurudroid.batchfoldercreator.utils

import com.opencsv.CSVReader
import java.io.FileReader
import java.io.IOException

object CsvParser {
    fun readCsv(csvPath: String?): ArrayList<String> {
        val data: ArrayList<String> = ArrayList()
        val reader: CSVReader
        var nextLine: Array<String?>
        try {
            reader = CSVReader(FileReader(csvPath ?: ""))
            var index = 0
            while (reader.readNext().also {
                    nextLine = it
                } != null) {

                //Skips CSV first rows (Column Headers/Titles)
                if (index != 0) data.add(nextLine[1]!!)
                index++
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }
}