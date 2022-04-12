import java.io.*;
import java.util.*;
import java.lang.*;

public class Simulator {

    static String path_machine_code = "C:\\Users\\DELL\\IdeaProjects\\Project-1\\src\\MachineCode.txt";

    // register r1 stores the return address
    static String pc = null;
    static int totalTime = 0;
    static String[] mem;
    static Cache cache;
    static boolean isCacheUsed = false;
    static int[] registerFile = new int[32];

    public static void MemInitialise(int memCap) {
        mem = new String[memCap];
    }

    public static void MemInitialise() {
        MemInitialise(256);
    }

    public static void dumpPC() {
        System.out.println("Current address stored in PC-" + pc);
    }

    public static void dumpTiming(int timeTaken) {
        totalTime += timeTaken;
        System.out.println("Time taken by the current instruction (in cycles) = " + timeTaken);
    }

    public static void dumpRF(int registerFile[]) {
        System.out.println("Status of register file");
        for (int i = 0; i < 32; i++) {
            System.out.println("R" + i + ":" + registerFile[i]);
        }
    }

    public static void dumpMem() {
        System.out.println("Memory status-");
        for (int i = 0; i < mem.length; i++) {
            System.out.println("Memory location(" + i + "):" + mem[i]);
        }
    }

    public static void updatePC(String branchTarget, CPU s) {
        int pc_val = Integer.parseInt(pc, 2);
        if (s.branchPC != 0) {
            pc_val = pc_val + s.branchPC;
            pc = Integer.toBinaryString(pc_val);
            if (s.isJalr) {
                pc_val = s.branchPC;
                pc = branchTarget;
            }
        } else {
            pc_val = pc_val + 1;
            pc = Integer.toBinaryString(pc_val);
        }
    }

    public static void storeInstructions(String mem[]) throws IOException {
        // reading the machineCode and storing in the memory
        int id = 0;
        File assemblyFile = new File(path_machine_code);
        BufferedReader br = new BufferedReader(new FileReader(assemblyFile));
        String codeLine;
        while ((codeLine = br.readLine()) != null) {
            if(isCacheUsed) {
                String store = codeLine;
                String add = Integer.toBinaryString((id));
                add = "0".repeat(32 - add.length()) + add;
                if (store.length() < 32) {
                    int l = store.length();
                    for (int i = 0; i < 32 - l; i++) {
                        store = "0" + store;

                    }
                }
                cache.write(add, store);
            }
            mem[id] = codeLine;
            id++;
        }
    }

    public static void main(String[] args) throws IOException {
        Assembler assembler = new Assembler();
        Scanner sc = new Scanner(System.in);
        System.out.println("If you want to provide the size of Main Memory enter 1 else enter 2 (Default size = 256)");
        int ch = sc.nextInt();
        if (ch == 1) {
            System.out.println("Input the size of Main Memory (each location is 32 bit)-");
            int t = sc.nextInt();
            MemInitialise(t);
        } else {
            MemInitialise();
        }
        System.out.println("Enter the access time for Main Memory (in cycles where 1 unit access time = 1 cycle) for Memory Operations-");
        int accessTime = sc.nextInt();
        CPU s = new CPU(registerFile, mem, accessTime);
        System.out.println("If you want system with cache enter 1 else 2");
        ch = sc.nextInt();
        if (ch == 1) {
            System.out.println("Enter the choice for cache (1) For Direct Map (2) For Set Associative (3) For Fully Associative");
            ch = sc.nextInt();
            isCacheUsed = true;
            s.isCacheUsed = true;
            int replacePolicy = 2, writePolicy = 1, size = 8, blockSize = 4, hitTime = 2, missPenalty = 2;
            System.out.println("Enter the size of cache (number of cache lines)-");
            size = sc.nextInt();
            System.out.println("Enter the block size");
            blockSize = sc.nextInt();
            System.out.println("Enter hit time");
            hitTime = sc.nextInt();
            System.out.println("Enter miss penalty");
            missPenalty = sc.nextInt();
            System.out.println("Choose replacement policy  choice (1) LRU (2) FIFO (3) Random");
            replacePolicy = sc.nextInt();
            System.out.println("Choose write policy choice (1) Write Through (2) Write Back");
            writePolicy = sc.nextInt();
            if (ch == 2) {
                cache = new SetAssociative(size, blockSize, mem, writePolicy, replacePolicy);
            } else if (ch == 1) {
                cache = new DirectMapped(size, blockSize, mem, writePolicy, replacePolicy);
            } else {
                cache = new FullyAssociative(size, blockSize, mem, writePolicy, replacePolicy);
            }
            s.cache = cache;
            cache.hitTime = hitTime;
            cache.missPenalty = missPenalty;
            cache.accessTime = accessTime;
        }
        assembler.convertAssembly(); // Converting assembly code to binary (machine code)
        storeInstructions(mem); // Storing instructions in the memory
        pc = Integer.toBinaryString(0);
        int id = 0;
        if (isCacheUsed) cache.updateTime();
        while (mem[Integer.parseInt(pc, 2)] != null) {
            s.initialise();
            s.fetch(pc);
            dumpRF(registerFile);
            dumpPC();
            dumpTiming(s.timeTaken);
            updatePC(s.branchTarget, s);
            id++;
        }
        System.out.println("Total time taken (in cycles)-" + totalTime);
        dumpMem();
        dumpRF(registerFile);
        if (isCacheUsed) {
            System.out.println("Total misses in cache-" + cache.totalMiss);
            System.out.println("Cache miss rate-" + (cache.totalMiss * 1.0) / id);
            cache.print();
        }
    }
}

/* Assumptions- ra -> r31 (return address register)
-No concurrent data and instruction are present at a memory location
-First load the instructions in memory
-Each instruction will be executed after previous instruction completes its 5-stage pipeline execution
-Replacement policy choice (1) LRU (2) FIFO (3) Random
-Write policy choice (1) Write Through (2) Write Back
*/


