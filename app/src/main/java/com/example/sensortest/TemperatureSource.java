package com.example.sensortest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TemperatureSource {

    Process temp;
    String lineTemp = "";
    float tempCPU,tempBattery;

    public float tempCPU(){

        try {

            /*type = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/type");
            type.waitFor();
            BufferedReader readType = new BufferedReader(new InputStreamReader(type.getInputStream()));
            lineType = readType.readLine();*/


            temp = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            temp.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
            lineTemp = reader.readLine();

            if (lineTemp != null) {
                tempCPU = Float.parseFloat(lineTemp);
                return tempCPU / 1000.0f;
            } else {
                return 0.2f;
            }

        }
        catch (Exception e) {
            e.printStackTrace();

            return 0.1f;
        }

    }

    public float tempBattery(){

        try {

            /*type = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone63/type");
            type.waitFor();
            BufferedReader readType = new BufferedReader(new InputStreamReader(type.getInputStream()));
            lineType = readType.readLine();    */

            temp = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone63/temp");
            temp.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
            lineTemp = reader.readLine();

            if (lineTemp != null) {
                tempBattery = Float.parseFloat(lineTemp);
                return tempBattery / 1000.0f;
            } else {
                return 200.f;
            }

        }
        catch (Exception e) {
            e.printStackTrace();

            return 100.1f;
        }
    }

    public float tempGPU(){

        try {

            /*type = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone63/type");
            type.waitFor();
            BufferedReader readType = new BufferedReader(new InputStreamReader(type.getInputStream()));
            lineType = readType.readLine();    */

            temp = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone63/temp");
            temp.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
            lineTemp = reader.readLine();

            if (lineTemp != null) {
                tempBattery = Float.parseFloat(lineTemp);
                return tempBattery / 1000.0f;
            } else {
                return 200.f;
            }

        }
        catch (Exception e) {
            e.printStackTrace();

            return 100.1f;
        }
    }
}
