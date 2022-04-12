
public class CPU{

    boolean isSt; // Defining various control signals
    boolean isLd;
    boolean isBeq;
    boolean isBgt;
    boolean isImmediate;
    boolean isAdd;
    boolean isSub;
    boolean isLsl;
    boolean isAsr;
    boolean isOr;
    boolean isXor;
    boolean isAnd;
    boolean isSra;
    boolean isSll;
    boolean isJalr;
    boolean isJal;
    boolean isBne;
    boolean isBlt;
    boolean isBge;
    boolean isLui;
    boolean isOffset;
    boolean isCacheUsed;
    String pc, branchTarget, fetch_inst; // fetch_inst stores the current instruction, pc is the program counter
    int registerFile[]; // registerFile is defined to store values of the 32, 32-bit registers, r0 to r31
    String mem[];
    int accessTime;
    int timeTaken, branchPC;
    public Cache cache;

    CPU(int registerFile[], String mem[], int accessTime){
        this.registerFile = registerFile;
        this.mem = mem;
        this.accessTime = accessTime;
    }

    void initialise(){
        isSt = false;
        isOffset = false;
        isLd = false;
        isBeq = false;
        isBgt = false;
        isImmediate = false;
        isAdd = false;
        isSub = false;
        isLsl = false;
        isAsr = false;
        isOr = false;
        isAnd = false;
        isXor = false;
        isSra = false;
        isSll = false;
        isBlt = false;
        isJal = false;
        isJalr = false;
        isBge = false;
        isBne = false;
        isLui = false;
        branchTarget = null;
        branchPC = 0;
        timeTaken = 0;
    }

    public void updateControlSignal(String fh_inst){ // input is a binary instruction
        if(fh_inst.substring(25,30).equals("01100")){
            if(fh_inst.substring(17,20).equals("000")){
                if(fh_inst.substring(0,5).equals("00000")){
                    isAdd = true;
                }
                else{
                    isSub = true;
                }
            }
            else if(fh_inst.substring(17,20).equals("111")){
                isAnd = true;
            }
            else if(fh_inst.substring(17,20).equals("110")){
                isOr = true;
            }
            else if(fh_inst.substring(17,20).equals("100")){
                isXor = true;
            }
            else if(fh_inst.substring(17,20).equals("001")){
                isSll = true;
            }
            else if(fh_inst.substring(17,20).equals("101")){
                isSra = true;
            }
        }
        else if(fh_inst.substring(25,30).equals("00100")){ // addi inst
            isImmediate = true;
            isAdd = true;
        }
        else if(fh_inst.substring(25,30).equals("00000")){ // load inst
            isOffset = true;
            isLd = true;
            timeTaken+=(accessTime-1);
        }
        else if(fh_inst.substring(25,30).equals("01000")){ // store inst
            isOffset = true;
            isSt = true;
            timeTaken+=(accessTime-1);
        }
        else if(fh_inst.substring(25,30).equals("11001")){ // jalr inst
            isJalr = true;
            isOffset = true;
        }
        else if(fh_inst.substring(25,30).equals("11011")){ // jal inst
            isJal = true;
            isOffset = true;
        }
        else if(fh_inst.substring(25,30).equals("11000")){ // branch inst
            isOffset = true;
            if(fh_inst.substring(17,20).equals("000")){
                isBeq = true;
            }
            else if(fh_inst.substring(17,20).equals("001")){
                isBne = true;
            }
            else if(fh_inst.substring(17,20).equals("100")){
                isBlt = true;
            }
            else if(fh_inst.substring(17,20).equals("101")){
                isBge = true;
            }
        }
        else if(fh_inst.substring(25,30).equals("01101")){ // load upper immediate inst
            isLui = true;
            isImmediate = true;
        }
    }

    public void fetch(String pc){ // pc is a binary string
        // If cache is used, then instruction is read from cache, else instruction is read from main memory
        this.pc = pc;
        if(isCacheUsed){
            pc = "0".repeat(32-pc.length())+pc; // converting pc to a 32-bit binary string
            fetch_inst = cache.read(pc);
            updateControlSignal(fetch_inst);
            timeTaken += cache.updateTime();
        }
        else{
            timeTaken += accessTime;
            int adr = Integer.parseInt(pc,2);
            fetch_inst = mem[adr];
            updateControlSignal(fetch_inst);
        }
        decode(fetch_inst);
    }

    public void decode(String inst){
        timeTaken += 1;

        // Tasks- Calculating the value of immediate and reading the source registers
        // Decoding the immediate operands
        // Object array contains op1, op2, immediate, offset

        Object[] arr = new Object[4];
        int op1, op2;
        op1 = Integer.parseInt(inst.substring(12,17),2); // For register type operations (add, sub, and, or, xor, sll, sra)
        op1 = registerFile[op1];
        op2 = Integer.parseInt((inst.substring(7,12)),2);
        op2 = registerFile[op2];
        arr[0] = op1;
        arr[1] = op2;

        if(isBlt||isBeq||isBne||isBge){ // Branch Instructions
            String offset = inst.substring(0,1)+inst.substring(24,25)+inst.substring(1,7)+inst.substring(20,24);
            if(offset.substring(0,1).equals("0")){
                arr[3] = Integer.parseInt(offset,2);
            }
            else{
                arr[3] = twoComplement(offset);
            }
        }

        if(isImmediate){
            int immediate;
            String imm;
            if(isAdd){
                imm = inst.substring(0,12);
            }else {
                imm = inst.substring(0,20);
            }
            if(imm.substring(0,1).equals("1")){
                immediate = twoComplement(imm);
            }
            else{
                immediate = Integer.parseInt(imm,2);
            }
            arr[2] = immediate;
        }

        if(isLd){ // Load instruction
            String offset = inst.substring(0,12);
            if(offset.substring(0,1).equals("0")){
                arr[3] = Integer.parseInt(offset,2);
            }
            else{
                arr[3] = twoComplement(offset);
            }
        }

        if(isSt){ // Store instruction
            String offset = inst.substring(0,7)+inst.substring(20,25);
            if(offset.substring(0,1).equals("0")){
                arr[3] = Integer.parseInt(offset,2);
            }
            else{
                arr[3] = twoComplement(offset);
            }
        }

        if(isJalr){ // Jump instruction
            String offset = inst.substring(0,12);
            if(offset.substring(0,1).equals("0")){
                arr[3] = Integer.parseInt(offset,2);
            }
            else{
                arr[3] = twoComplement(offset);
            }
        }

        if(isJal){ // Jump instruction
            String offset = inst.substring(0,1)+inst.substring(12,20)+inst.substring(11,12)+inst.substring(1,11);
            if(offset.substring(0,1).equals("0")){
                arr[3] = Integer.parseInt(offset,2);
            }
            else{
                arr[3] = twoComplement(offset);
            }
        }
        execute(arr);
    }

    // Object array contains [op1, op2, immediate, offset]

    public void execute(Object[] val){
        timeTaken += 1;
        Object aluResult = null;

        if(isAdd){
            isAdd = false;
            if(isImmediate){
                aluResult = (int)val[0]+(int)val[2];
            }
            else{
                aluResult = (int)val[0]+(int)val[1];
            }
        }

        if(isSub){
            isSub = false;
            aluResult = (int)val[0]-(int)val[1];
        }

        if(isAnd){
            isAnd = false;
            aluResult = (int)val[0]&(int)val[1];
        }

        if(isOr){
            isOr = false;
            aluResult = (int)val[0]|(int)val[1];
        }

        if(isXor){
            isXor = false;
            aluResult = (int)val[0]^(int)val[1];
        }

        if(isSll){ // logical shift left
            isSll = false;
            aluResult = (int)val[0]<<(int)val[1];
        }

        if(isSra){ // arithmetic shift right
            isSra = false;
            aluResult = (int)val[0]>>(int)val[1];
        }

        if(isBeq){
            if((int)val[0]==(int)val[1]){
                branchPC = (int)val[3];
                branchTarget = Integer.toBinaryString((int)val[3]);
            }
        }

        if(isBne) {
            if ((int) val[0] != (int) val[1]) {
                branchPC = (int)val[3];
                branchTarget = Integer.toBinaryString((int)val[3]);
            }
        }

        if(isBlt) {
            if ((int) val[0] < (int) val[1]) {
                branchPC = (int)val[3];
                branchTarget = Integer.toBinaryString((int)val[3]);
            }
        }

        if(isBge) {
            if ((int)val[0] >= (int)val[1]) {
                branchPC = (int)val[3];
                branchTarget = Integer.toBinaryString((int)val[3]);
            }
        }

        if(isJal||isJalr){
            int pc_val = Integer.parseInt(pc,2);
            branchPC = (int)val[3];
            if(isJal){
                branchTarget = Integer.toBinaryString(branchPC);
            }
            if(isJalr){
                branchPC = (int)val[0]+branchPC;
                branchTarget = Integer.toBinaryString(branchPC);
            }
            aluResult = pc_val+1;
        }

        if(isLd||isSt){
            aluResult = (int)val[3]+(int)val[0];
        }

        if(isLui){
            aluResult = val[2];
            aluResult = (int)aluResult<<12;
        }
        memory(val, aluResult);
    }

    public void memory(Object val[], Object res){
        if(isLd||isLui){
            if(isLd) {
                String address = Integer.toBinaryString((int)res);
                address = "0".repeat(32-address.length())+address;
                String ldResult;
                if(isCacheUsed){
                    ldResult = cache.read(address);
                    timeTaken += cache.updateTime();
                }
                else{
                    ldResult = mem[Integer.parseInt(address,2)];
                    timeTaken += accessTime;
                }
                res = Integer.parseInt(ldResult,2);
            }
            else{
                timeTaken += 1;
            }
            writeBack(res);
        }
        else if(isSt){
            String data = Integer.toBinaryString((int)val[1]);
            String address = Integer.toBinaryString((int)res);
            address = "0".repeat(32-address.length())+address;
            if(data.length()<32){
                int l = data.length();
                for(int i=0 ; i<32-l ; i++){
                    data = "0"+data;
                }
            }
            if(isCacheUsed){
                cache.write(address,data);
                timeTaken += cache.updateTime();
            }
            else{
                mem[(int)res] = data;
                timeTaken += accessTime;
            }
            writeBack(null);
        }
        else{
            timeTaken += 1;
            writeBack(res);
        }
    }

    public void writeBack(Object ans){
        timeTaken += 1;
        if(ans != null) {
            String adr = fetch_inst.substring(20, 25);
            int idx = Integer.parseInt(adr, 2);
            registerFile[idx] = (int)ans;
            return;
        }
    }

    public int twoComplement(String num){
        String temp = "";
        for(int i=0;i<num.length();i++){
            if(num.charAt(i)=='0'){
                temp += '1';
            }
            else{
                temp += '0';
            }
        }
        return -1*(Integer.parseInt(temp,2)+1);
    }
}