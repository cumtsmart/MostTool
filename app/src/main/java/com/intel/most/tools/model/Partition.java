package com.intel.most.tools.model;

import java.util.ArrayList;
import java.util.HashSet;

public class Partition {
    // IO read
    public ArrayList<MostLine> readLogs = new ArrayList<MostLine>();
    // IO write
    public ArrayList<MostLine> writeLogs = new ArrayList<MostLine>();
    // Sequence R/W
    public ArrayList<MostLine> writeSequence = new ArrayList<MostLine>();
    public ArrayList<MostLine> readSequence = new ArrayList<MostLine>();
    // Random   R/W
    public ArrayList<MostLine> writeRandom = new ArrayList<MostLine>();
    public ArrayList<MostLine> readRandom = new ArrayList<MostLine>();
    // all visit files
    public HashSet<String> visitFiles = new HashSet<String>();
    // all
    public ArrayList<MostLine> allLogs = new ArrayList<MostLine>();
}
