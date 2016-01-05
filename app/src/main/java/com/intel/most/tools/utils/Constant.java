package com.intel.most.tools.utils;

public class Constant {
    public static final String FILE_SYSTEM = "EXT4";
    public static final String MOST_BIN = "mostbin.zip";
    public class CMD {
        public static final String CMD_MOST = "/data/most ";
        public static final String CMD_TRACE = "/data/blktrace ";
        public static final String CMD_PARSE = "/data/blkparse ";
        public static final String CMD_FILTER = "/data/filter.sh ";
    }

    public class PARTITION {
        public static final String FLASH=" /dev/block/mmcblk0";
        public static final String PART_DATA=" /dev/block/mmcblk0p11";
        public static final String PART_CACHE=" /dev/block/mmcblk0p6";
        public static final String PART_SYSTEM=" /dev/block/mmcblk0p5";
    }

    public class PATH {
        public static final String BLK_TRACE = " /sdcard/result";
        public static final String BLK_PARSE = " /sdcard/result.p";

        public static final String MOST_DATA = " /sdcard/data_result.txt ";
        public static final String MOST_CACHE = " /sdcard/cache_result.txt ";
        public static final String MOST_SYSTEM = " /sdcard/system_result.txt ";

        public static final String FILTER_DATA = "/data_filter_result.txt ";
        public static final String FILTER_CACHE = "/cache_filter_result.txt ";
        public static final String FILTER_SYSTEM = "/system_filter_result.txt ";
    }
}
