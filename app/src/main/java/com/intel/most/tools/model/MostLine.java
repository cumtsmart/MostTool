package com.intel.most.tools.model;

public class MostLine {
    public String eventDevice;
    public String cpuID;
    public String seqNumber;
    public String timeStamp;
    public String processID;
    public String action;
    public String ioType;
    public String tails;
    // split from tails
    public int blkIndex;
    public int blkSize;
    public String filePath;
    // calculate from ahead line
    public boolean isSequence;
}
