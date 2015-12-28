package com.intel.most.tools.data;


import com.intel.most.tools.model.Partition;

import java.util.ArrayList;
import java.util.List;

public class GraphData {
    private List<Partition> partitions = new ArrayList<Partition>();

    public void addPartition(Partition partition) {
        partitions.add(partition);
    }

    public List<Partition> getPartitions() {
        return partitions;
    }
}
