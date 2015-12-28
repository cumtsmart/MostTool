package com.intel.most.tools.model;

import java.util.ArrayList;

public class Partition {
    // IO read
    public ArrayList<MostLine> readLogs = new ArrayList<MostLine>();
    // IO write
    public ArrayList<MostLine> writeLogs = new ArrayList<MostLine>();
    // all
    public ArrayList<MostLine> allLogs = new ArrayList<MostLine>();
}
