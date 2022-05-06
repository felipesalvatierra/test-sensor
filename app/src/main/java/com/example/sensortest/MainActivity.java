package com.example.sensortest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private TextView textTempCPU;
    private TextView textTempBattery;
    TemperatureSource temperatureSource;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        textTempCPU = findViewById(R.id.text_view_cpu);
        textTempBattery = findViewById(R.id.text_view_cpu2);


        Timer();

    }

    /** THERMAL QUE UTILIZAREMOS PARA CPU / GPU E BATTERY
     * THERMAL 0 - TEMPERATURA TOTAL DA CPU aoss0-usr
     * THERMAL 63 - TEMPERATURA DA BATERIA battery
     */






    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by one or more digits
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles((FileFilter) new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    // for multi core value
    private float readCore(int i) {
        /*
         * how to calculate multicore this function reads the bytes from a
         * logging file in the android system (/proc/stat for cpu values) then
         * puts the line into a string then spilts up each individual part into
         * an array then(since he know which part represents what) we are able
         * to determine each cpu total and work then combine it together to get
         * a single float for overall cpu usage
         */
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            // skip to the line we need
            for (int ii = 0; ii < i + 1; ++ii) {
                String line = reader.readLine();
            }
            String load = reader.readLine();

            // cores will eventually go offline, and if it does, then it is at
            // 0% because it is not being
            // used. so we need to do check if the line we got contains cpu, if
            // not, then this core = 0
            if (load.contains("cpu")) {
                String[] toks = load.split(" ");

                // we are recording the work being used by the user and
                // system(work) and the total info
                // of cpu stuff (total)
                // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                long work1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]);
                long total1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                        + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                try {
                    // short sleep time = less accurate. But android devices
                    // typically don't have more than
                    // 4 cores, and I'n my app, I run this all in a second. So,
                    // I need it a bit shorter
                    Thread.sleep(300);
                } catch (Exception e) {
                }

                reader.seek(0);
                // skip to the line we need
                for (int ii = 0; ii < i + 1; ++ii) {
                    reader.readLine();
                }
                load = reader.readLine();

                // cores will eventually go offline, and if it does, then it is
                // at 0% because it is not being
                // used. so we need to do check if the line we got contains cpu,
                // if not, then this core = 0%
                if (load.contains("cpu")) {
                    reader.close();
                    toks = load.split(" ");

                    long work2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]);
                    long total2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                            + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                            + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                    // here we find the change in user work and total info, and
                    // divide by one another to get our total
                    // seems to be accurate need to test on quad core
                    // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                    if ((total2 - total1) == 0)
                        return 0;
                    else
                        return (float) (work2 - work1) / ((total2 - total1));

                } else {
                    reader.close();
                    return 0;
                }

            } else {
                reader.close();
                return 0;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static float batteryTemperature()
    {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone1/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            } else {
                return 45.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static float cpuTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                float temp = Float.parseFloat(line);
                return temp / 1000.0f;
            } else {
                return 45.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static String cpuTemperatureType() {
        int i;
        Process temp, type;
        String line, line1;

        try {

            for (i = 0; i < 29; i++)
            {
                temp = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone"+i+"/temp");
                temp.waitFor();
                BufferedReader reader = new BufferedReader(new InputStreamReader(temp.getInputStream()));
                line = reader.readLine();

                type = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone"+i+"/type");
                type.waitFor();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(type.getInputStream()));
                line1 = reader2.readLine();

                System.out.println(line1 + ": " + line  + "\n");
            }


            return "0";

            /*if (line != null) {

                return line;
            } else {
                return "vazio";
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public void Timer(){
        Timer timer = new Timer();
        Task task = new Task();

        timer.schedule(task, 1000, 5000);

    }

    class Task extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Refresh Activity
                    temp_cpu();
                    temp_battery();
                }
            });
        }
    }

    public void num_core(){
        float[] coreValues = new float[10];
        //get how many cores there are from function
        int numCores = getNumCores();
        for(byte i = 0; i < numCores; i++)
        {
            coreValues[i] = readCore(i);
            coreValues[i] = coreValues[i] * 100;
            textTempCPU.append("NÚCLEOS " + coreValues[i] + "\n");
        }
    }


    public void temp_battery(){
        temperatureSource = new TemperatureSource();
        float battery = temperatureSource.tempBattery();
        textTempBattery.setText(" Battery temperature: " + String.valueOf(battery) + " Cº");
    }



    public void temp_cpu() {
        temperatureSource = new TemperatureSource();
        float cpu = temperatureSource.tempCPU();
        textTempCPU.setText(" Cpu temperature: " + String.valueOf(cpu) + " Cº");
    }
}