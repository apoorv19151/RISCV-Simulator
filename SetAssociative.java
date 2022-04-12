import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class SetAssociative extends Cache {

    int offset, k, tagSize, set_index;
    int associativity;
    Queue<Integer>setQueue[];

    SetAssociative(int size, int blockSize, String mem[], int writePolicy, int replacePolicy) {
        super(size, blockSize, mem, writePolicy, replacePolicy);
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter k for k-way Associativity-");
        k = sc.nextInt();
        offset = (int)(Math.log(blockSize)/Math.log(2));
        set_index = (int)(Math.log(size/k)/Math.log(2));
        tagSize = 32-set_index-offset;
        tagArray = new String[size];
        setQueue = new Queue[size/k];
        for(int i=0 ; i<(size/k) ; i++){
            setQueue[i] = new LinkedList<>();
        }
    }

    @Override
    public int insert(String address) {
        String tag = address.substring(0, tagSize);
        String idxBin = address.substring(tagSize, tagSize + set_index);
        String dataLocBin = address.substring(tagSize + set_index, 32);
        int dataLoc = Integer.parseInt(dataLocBin, 2);
        int setNum;
        if(idxBin.equals("")){
            setNum= 0;
        }
        else{
            setNum = Integer.parseInt(idxBin,2);
        }
        associativity = k;
        String loc = tag + idxBin;
        String zeros = "0".repeat(offset);
        loc = loc + zeros;
        int startIdx = Integer.parseInt(loc, 2);  // for a block
        for (int i = 0; i < k; i++) { // empty block in a set
            if (tagArray[setNum * k + i] == null) {
                tagArray[setNum * k + i] = "10" + tag;
                for (int j = 0; j < blockSize; j++) {
                    dataArray[k * setNum + i][j] = mem[startIdx + j];
                }
                return setNum*k+i;
            }
        }
        return 0;
    }

    @Override
    public void write(String address, String data) {
        String tag = address.substring(0, tagSize);
        String idxBin = address.substring(tagSize, tagSize+set_index);
        String dataLocBin = address.substring(tagSize+set_index, 32);
        int dataLoc = Integer.parseInt(dataLocBin,2);
        int setNum;
        if(idxBin.equals("")){
            setNum= 0;
        }
        else{
            setNum = Integer.parseInt(idxBin,2);
        }
        int f=0;
        int blockPos = -1;
        for(int i=0 ; i<k ; i++){
            if(tagArray[setNum*k+i] != null && tagArray[setNum*k+i].substring(2).equals(tag)){
                dataArray[setNum*k + i][dataLoc] = data;
                blockPos = setNum*k + i;
                f=1;
            }
            if(f==1)break;
        }
        // cache miss
        if(f==0){
            // check if the set has empty slots
            int f2 = 0;
            for(int i=0 ; i<k ; i++){
                if(tagArray[setNum*k+i]==null){
                    f2=1;
                    blockPos = insert(address);
                    break;
                }
            }
            // if no empty slots evict and insert
            if(f2==0){
                evict(address);
                blockPos = insert(address);
            }
            isMiss = true;
            totalMiss++;
        }
        dataArray[blockPos][dataLoc] = data;
        // writePolicy = 1 (writeThrough) else writeBack
        if(writePolicy==1){
            int add = Integer.parseInt(address,2);
            mem[add] = data;
        }
        else{
            tagArray[blockPos] = "11"+tagArray[blockPos].substring(2);
        }
        // LRU
        if(replacePolicy==1){
            updateCounterArray(blockPos);
        }
        // FIFO update queue in case of cache miss only
        else if(replacePolicy==2 && f==0){
            setQueue[setNum].add(blockPos);
        }
    }

    @Override
    public String read(String address){
        String tag = address.substring(0, tagSize);
        String idxBin = address.substring(tagSize, tagSize+set_index);
        String dataLocBin = address.substring(tagSize+set_index,32);
        int dataLoc = Integer.parseInt(dataLocBin,2);
        int setNum;
        if(idxBin.equals("")){
            setNum= 0;
        }
        else{
            setNum = Integer.parseInt(idxBin,2);
        }int blockPos = -1;
        // Check cache Hit
        for(int i=0 ; i<k ; i++) {
            // if memory location present in cache (block)
            if (tagArray[setNum * k + i].substring(2).equals(tag)) {
                blockPos = setNum*k + i;
                if(replacePolicy==1){ // LRU
                    updateCounterArray(blockPos);
                }
                return dataArray[blockPos][dataLoc];
            }
        }
        // cache miss
        int f= 0;
        // checking if empty slots present
        for(int i=0 ; i<k ; i++){
            if(tagArray[setNum*k+i]==null){
                f=1;
                break;
            }
        }
        isMiss=true;
        totalMiss++;
        // if empty slots present
        if(f==1){
            blockPos = insert(address);
        }
        // no empty slots
        else{
            evict(address);
            blockPos = insert(address);
        }
        // LRU
        if(replacePolicy==1){
            updateCounterArray(blockPos);
        }
        // FIFO block added in FIFO only in case of cache miss
        else if(replacePolicy==2){
            setQueue[setNum].add(blockPos);
        }
        return dataArray[blockPos][dataLoc];
    }


    @Override
    public void evict(String address) {
        String idxBin = address.substring(tagSize, tagSize + set_index);
        int setNum;
        if(idxBin.equals("")){
            setNum= 0;
        }
        else{
            setNum = Integer.parseInt(idxBin,2);
        }
        int blockPos = -1;
        // LRU
        if(replacePolicy==1){
            int mx = -1;
            for (int i = 0; i < k; i++) {
                if (counterArray[setNum * k + i] > mx) {
                    mx = counterArray[setNum * k + i];
                    blockPos = setNum * k + i;
                }
            }
        }
        // FIFO
        else if(replacePolicy==2){
            blockPos = setQueue[setNum].peek();
            setQueue[setNum].peek();
            setQueue[setNum].poll();
        }
        // Random
        else {
            int random = (int)Math.round(Math.random()*(k));
            if(random==k){
                random = k-1;
            }
            blockPos = random+setNum*k;
        }

        String loc = tagArray[blockPos].substring(2) + idxBin;
        String zeros = "0".repeat(offset);
        loc = loc + zeros;
        int startIdx = Integer.parseInt(loc, 2);  // for a block

        for(int i=0 ; i<blockSize ; i++){
            // writeBack
            if(tagArray[blockPos].substring(1,2).equals("1") && writePolicy==2){
                mem[startIdx+i] = dataArray[blockPos][i];
            }
            if(dataArray[blockPos][i] != null){
                System.out.println(Integer.parseInt(dataArray[blockPos][i],2));
            }
            dataArray[blockPos][i] = null;
        }
        tagArray[blockPos] = null;
    }

    @Override
    public void print(){
        for(int i=0;i<size;i++){
            if(i%k==0) {
                System.out.println("Set Number-" + (i / k));
            }
            for(int j=0;j<blockSize;j++){
                System.out.print(dataArray[i][j]+" ");
            }
            System.out.println();
        }
    }
}