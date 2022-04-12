import java.util.LinkedList;
import java.util.Queue;

public abstract class Cache { // Cache is word addressable so 1 block stores 1 word

    int size, missPenalty, hitTime, blockSize, accessTime, totalMiss=0, cyclesTaken=0;
    boolean isMiss = false;
    String tagArray[];
    String mem[];
    String dataArray[][];
    int writePolicy;
    int counterArray[];
    int replacePolicy;
    Queue<Integer>queue; // for FIFO replacement

    Cache(int size, int blockSize, String mem[], int writePolicy, int replacePolicy){
        this.size = size;
        this.replacePolicy = replacePolicy;
        this.writePolicy = writePolicy;
        this.mem = mem;
        this.blockSize = blockSize;
        tagArray = new String[size];
        counterArray = new int[size];
        dataArray = new String[size][blockSize];
        queue = new LinkedList<Integer>();
        for(int i=0 ; i<size ; i++){
            counterArray[i] = 1;
        }
    }

    public abstract int insert(String add);
    public abstract void write(String add, String data);
    public abstract void print();
    public abstract String read(String address);
    public abstract void evict(String address);

    public void updateCounterArray(int blockPos){
        for(int i=0 ; i<size ; i++){
            counterArray[i]+=1;
        }
        counterArray[blockPos] = 0;
    }

    public void updateFIFO(int blockPos){
        queue.add(blockPos);
    }

    public int updateTime(){
        if(isMiss){
            cyclesTaken = hitTime + missPenalty + accessTime;
        }
        else{
            cyclesTaken = hitTime;
        }
        isMiss = false;
        return cyclesTaken;
    }
}