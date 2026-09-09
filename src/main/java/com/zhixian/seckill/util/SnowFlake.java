package com.zhixian.seckill.util;

/**
 * Description: Twitter's distributed auto-increment ID snowflake algorithm (Java version)
 **/
public class SnowFlake {

    /**
     * Start timestamp
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * Number of bits occupied by each part
     */
    private final static long SEQUENCE_BIT = 12; // Number of bits occupied by the sequence number
    private final static long MACHINE_BIT = 5;   // Number of bits occupied by the machine ID
    private final static long DATACENTER_BIT = 5;// Number of bits occupied by the data center ID

    /**
     * Maximum value of each part
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * Left shift for each part
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;  // Data center
    private long machineId;     // Machine ID
    private long sequence = 0L; // Sequence number
    private long lastStmp = -1L;// Last timestamp

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * Generate the next ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            // Within the same millisecond, increment the sequence number
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // The sequence number for the current millisecond has reached its maximum
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            // In a different millisecond, reset the sequence number to 0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT // Timestamp part
                | datacenterId << DATACENTER_LEFT       // Data center part
                | machineId << MACHINE_LEFT             // Machine ID part
                | sequence;                             // Sequence number part
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 1);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println(snowFlake.nextId());
        }

        System.out.println("Total time: " + (System.currentTimeMillis() - start));
    }
}