package com.intel.most.tools.utils;

import android.content.Context;
import android.util.Log;

import com.intel.most.tools.model.CachePartition;
import com.intel.most.tools.model.DataPartition;
import com.intel.most.tools.model.MostLine;
import com.intel.most.tools.model.Partition;
import com.intel.most.tools.model.SystemPartition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ParseLogTools {
    private static final String LINE_REGEX = "\\s+";

    public static CachePartition parseCache(Context context) {
        String cacheResult = context.getExternalFilesDir(null) + Constant.PATH.FILTER_CACHE;
        CachePartition cachePartition = new CachePartition();
        parse(cacheResult.trim(), cachePartition);
        return cachePartition;
    }

    public static DataPartition parseData(Context context) {
        String dataResult = context.getExternalFilesDir(null) + Constant.PATH.FILTER_DATA;
        DataPartition dataPartition = new DataPartition();
        parse(dataResult.trim(), dataPartition);
        return dataPartition;
    }

    public static SystemPartition parseSystem(Context context) {
        String systemResult = context.getExternalFilesDir(null) + Constant.PATH.FILTER_SYSTEM;
        SystemPartition systemPartition = new SystemPartition();
        parse(systemResult.trim(), systemPartition);
        return systemPartition;
    }

    private static void parse(String path, Partition partition) {
        Log.e("yangjun", "------parse:" + path);
        FileReader cacheReader = null;
        BufferedReader bfReader= null;
        try {
            File file = new File(path);
            if (file.exists()) {
                Log.e("yangjun", path + " file exist");
            } else {
                Log.e("yangjun", path + " file not exist");
                return;
            }
            cacheReader = new FileReader(file);
            bfReader = new BufferedReader(cacheReader);

            String result = null;
            while ((result = bfReader.readLine()) != null) {
                Log.e("yangjun", result);
                String[] arr = result.split(LINE_REGEX, 8);
                if (arr.length == 8) {
                    MostLine mostLine = new MostLine();
                    mostLine.eventDevice = arr[0];
                    mostLine.cpuID = arr[1];
                    mostLine.seqNumber = arr[2];
                    mostLine.timeStamp = arr[3];
                    mostLine.processID = arr[4];
                    mostLine.action = arr[5];
                    mostLine.ioType = arr[6];
                    mostLine.tails = arr[7];

                    // 3722172 + 8 [mmcqd/0]	D	D	/vendor/lib/egl/libGLES_mali.so	Random
                    String[] arr2 = arr[7].split(LINE_REGEX);
                    for (String line :arr2) {
                        if (line.startsWith("/")) {
                            mostLine.filePath = line;
                        }
                    }

                    mostLine.blkIndex = Integer.valueOf(arr2[0]);
                    mostLine.blkSize = Integer.valueOf(arr2[2]);
                    Log.e("yangjun", arr2[0] + ":" + arr2[2]);
                    // get the ahead most line
                    int length = partition.allLogs.size();
                    if (length > 0) {
                        MostLine aheadLine = partition.allLogs.get(length - 1);

                        if ((aheadLine.blkIndex + aheadLine.blkSize) == mostLine.blkIndex) {
                            mostLine.isSequence = true;
                        } else {
                            mostLine.isSequence = false;
                        }
                    } else {
                        mostLine.isSequence = false;
                    }

                    if (mostLine.action.equals("D")) {
                        if (mostLine.ioType.startsWith("R")) {
                            partition.readLogs.add(mostLine);
                            if (mostLine.isSequence) {
                                partition.readSequence.add(mostLine);
                            } else {
                                partition.readRandom.add(mostLine);
                            }
                        } else if (mostLine.ioType.startsWith("W")) {
                            partition.writeLogs.add(mostLine);
                            if (mostLine.isSequence) {
                                partition.writeSequence.add(mostLine);
                            } else {
                                partition.writeRandom.add(mostLine);
                            }
                        }
                    }
                    //
                    partition.allLogs.add(mostLine);
                }
            }
            Log.e("yangjun", "   size:" + partition.allLogs.size());
            Log.e("yangjun", "RR size:" + partition.readRandom.size());
            Log.e("yangjun", "RS size:" + partition.readSequence.size());
            Log.e("yangjun", "WR size:" + partition.writeRandom.size());
            Log.e("yangjun", "WS size:" + partition.writeSequence.size());

        } catch (FileNotFoundException e) {
            Log.e("yangjun", "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("yangjun", "IOException");
            e.printStackTrace();
        } finally {
            if (bfReader != null) {
                try {
                    bfReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
