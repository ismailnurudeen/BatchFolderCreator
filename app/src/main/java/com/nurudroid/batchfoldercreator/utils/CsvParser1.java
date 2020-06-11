package com.nurudroid.batchfoldercreator.utils;

import android.util.Log;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CsvParser1 {

    public static ArrayList<String> readCsv(String csv_path) {

        ArrayList<String> data = new ArrayList<>();
        CSVReader reader;
        String[] nextLine;

        try {
            reader = new CSVReader(new FileReader(csv_path));
            int index = 0;
            while ((nextLine = reader.readNext()) != null) {
                //Skips CSV first rows (Column Headers/Titles)
//                if (index != 0)
                    data.add(nextLine[0]);
//                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}

