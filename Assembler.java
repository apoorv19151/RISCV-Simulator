import java.io.BufferedReader;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.FileReader;

public class Assembler {

    String path_assembly_code = "C:\\Users\\DELL\\IdeaProjects\\Project-1\\src\\code1.txt";
    String path_machine_code = "C:\\Users\\DELL\\IdeaProjects\\Project-1\\src\\MachineCode.txt";

    String truncate(String str, int size){ // Helper function to truncate binary string to a certain size
        String temp = "";
        for(int i = str.length()-1 ; i>=0 ; i--){
            temp = str.charAt(i) + temp;
            if(temp.length()==size){
                break;
            }
        }
        return temp;
    }

    String getBinary(String Instruction) {  // Function for converting an instruction to a 32-bit binary

        String s1 = "MyLabel: add r1 r2 imm";
        if(Instruction.indexOf(":") != -1) { // Removing label from the instruction
            s1 = Instruction.substring(Instruction.indexOf(":")+2);
        }
        else {
            s1 = Instruction;
        }

        String[] arr = s1.split(" "); // Storing the instruction in an array after splitting

        String opcode = "";
        String imm = "";
        String offset = "";
        String rd = "";
        String rs1 = "";
        String rs2 = "";
        String func = "";
        String result = "";

        if(arr[0].equals("add")){ // Encoding for add instruction
            // format add rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "000";
            result = "0000000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("addi")) { // Encoding for addi instruction
            // format addi rd rs1 imm
            opcode = "0010011";
            imm = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(imm.length()<12) {
                imm = "0"+imm;
            }
            if(imm.length()>12){
                imm = truncate(imm, 12);
            }
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            func = "000";
            result = imm+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("sub")) { // Encoding for sub instruction
            // format sub rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "000";
            result = "0100000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("lw")) { // Encoding for lw instruction
            // format lw rd offset(rs1)
            opcode = "0000011";
            int n1 = arr[2].indexOf('(');
            int n2 = arr[2].indexOf(')');
            offset = Integer.toBinaryString(Integer.parseInt(arr[2].substring(0,n1)));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(n1+2,n2)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            func = "010";
            result = offset+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("sw")) { // Encoding for sw instruction
            // format sw rs2 offset(rs1)
            opcode = "0100011";
            int n1 = arr[2].indexOf('(');
            int n2 = arr[2].indexOf(')');
            offset = Integer.toBinaryString(Integer.parseInt(arr[2].substring(0,n1)));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(n1+2,n2)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rs2.length()<5) {
                rs2= "0"+rs2;
            }
            func = "010";
            result = offset.substring(0, 7)+rs2+rs1+func+offset.substring(7, 12)+opcode;
        }

        else if(arr[0].equals("jalr")) { // Encoding for jalr instruction
            // format jalr rd rs1 offset
            opcode = "1100111";
            offset = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            func = "000";
            result = offset+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("jal")) { // Encoding for jal instruction
            // format jal rd offset
            opcode = "1101111";
            offset = Integer.toBinaryString(Integer.parseInt(arr[2]));

            while(offset.length()<20) {
                offset = "0"+offset;
            }
            if(offset.length()>20){
                offset = truncate(offset, 20);
            }
            System.out.println("offset after truncation:"+offset);
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            result = offset.charAt(0)+offset.substring(10,20)+offset.charAt(9)+offset.substring(1,9)+rd+opcode;
        }

        else if(arr[0].equals("beq")) { // Encoding for beq instruction
            // format beq rs1 rs2 offset
            opcode = "1100011";
            offset = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            if(offset.length()>12){
                offset = truncate(offset, 12);
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "000";
            result = offset.charAt(0)+offset.substring(2,8)+rs2+rs1+func+offset.substring(8,12)+offset.charAt(1)+opcode;
        }

        else if(arr[0].equals("bne")) { // Encoding for bne instruction
            // format bne rs1 rs2 offset
            opcode = "1100011";
            offset = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            if(offset.length()>12){
                offset = truncate(offset, 12);
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "001";
            result = offset.charAt(0)+offset.substring(2,8)+rs2+rs1+func+offset.substring(8,12)+offset.charAt(1)+opcode;
        }

        else if(arr[0].equals("blt")) { // Encoding for blt instruction
            // format blt rs1 rs2 offset
            opcode = "1100011";
            offset = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            if(offset.length()>12){
                offset = truncate(offset, 12);
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "100";
            result = offset.charAt(0)+offset.substring(2,8)+rs2+rs1+func+offset.substring(8,12)+offset.charAt(1)+opcode;
        }

        else if(arr[0].equals("bge")) { // Encoding for bge instruction
            // format bge rs1 rs2 offset
            opcode = "1100011";
            offset = Integer.toBinaryString(Integer.parseInt(arr[3]));
            while(offset.length()<12) {
                offset = "0"+offset;
            }
            if(offset.length()>12){
                offset = truncate(offset, 12);
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "101";
            result = offset.charAt(0)+offset.substring(2,8)+rs2+rs1+func+offset.substring(8,12)+offset.charAt(1)+opcode;
        }

        else if(arr[0].equals("lui")) { // Encoding for lui instruction
            // format lui rd imm
            opcode = "0110111";
            imm = Integer.toBinaryString(Integer.parseInt(arr[2]));
            while(imm.length()<32) {
                imm = "0"+imm;
            }
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            result = imm.substring(0,20)+rd+opcode;
        }

        else if(arr[0].equals("and")) { // Encoding for AND instruction
            // format and rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "111";
            result = "0000000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("or")) { // Encoding for OR instruction
            // format or rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "110";
            result = "0000000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("xor")) { // Encoding for XOR instruction
            // format xor rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "100";
            result = "0000000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("sll")) { // Encoding for sll instruction
            // format sll rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "001";
            result = "0000000"+rs2+rs1+func+rd+opcode;
        }

        else if(arr[0].equals("sra")) { // Encoding for sra instruction
            // format sra rd rs1 rs2
            opcode = "0110011";
            rd = Integer.toBinaryString(Integer.parseInt(arr[1].substring(1)));
            while(rd.length()<5) {
                rd = "0"+rd;
            }
            rs1 = Integer.toBinaryString(Integer.parseInt(arr[2].substring(1)));
            while(rs1.length()<5) {
                rs1 = "0"+rs1;
            }
            rs2 = Integer.toBinaryString(Integer.parseInt(arr[3].substring(1)));
            while(rs2.length()<5) {
                rs2 = "0"+rs2;
            }
            func = "101";
            result = "0100000"+rs2+rs1+func+rd+opcode;
        }
        return(result);
    }

    void treatLabel(HashMap<String, Integer> mp, ArrayList<String> labelArr) throws IOException {
        // This function reads all the lines in the assembly code, if any code line contains a label, it will be stored.
        File assemblyFile = new File(path_assembly_code);
        BufferedReader br = new BufferedReader(new FileReader(assemblyFile));
        String codeLine;

        int id=0;
        // extracting the labels and storing them in the array labelArr and hashmap mp
        while((codeLine=br.readLine()) != null){
            String arr[] = codeLine.split(" ");
            if(arr[0].indexOf(":") != -1){
                int len = arr[0].length();
                labelArr.add(arr[0].substring(0, len-1));
                mp.put(arr[0].substring(0, len-1), id);
            }
            id++;
        }
    }

    public String check(String line, ArrayList<String> labelArr){
        // function for checking whether a particular code line contains a label present in labelArr
        for(String i : labelArr){
            if(line.indexOf(i) != -1){ // If the code line contains a label, then that label is returned.
                return i;
            }
        }
        return null;
    }

    public  void createMachineCodeFile(){
        // function for creating a file that will have instructions in binary
        try {
            File myObj = new File(path_machine_code);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void convertAssembly() throws IOException {
        ArrayList<String> labelArr = new ArrayList<>();
        HashMap<String, Integer> mp = new HashMap<>();
        treatLabel(mp, labelArr);
        createMachineCodeFile();
        File assemblyFile = new File(path_assembly_code);
        BufferedReader br = new BufferedReader(new FileReader(assemblyFile));

        String codeLine;
        int id = 0;
        FileWriter myWriter = new FileWriter(path_machine_code);

        while((codeLine=br.readLine()) != null){
            String arr[] = codeLine.split(" ");
            String labelPresent = check(codeLine, labelArr);
            // if instruction contains label but not jumps etc. then simply remove label name from instruction
            if(labelPresent != null && codeLine.indexOf(":") != -1){
                int idx = codeLine.indexOf(arr[1]);
                codeLine = codeLine.substring(idx);
            }
            arr = codeLine.split(" "); // checking again for jump, branch instructions
            labelPresent = check(codeLine, labelArr);
            // if the instruction is branch then simply replace the label with offset
            if(labelPresent != null && !arr[0].equals("jal")){
                int offset = mp.get(labelPresent)-id;
                codeLine = arr[0]+" "+arr[1]+" "+arr[2]+" "+Integer.toString(offset);
            }
            // instruction is jal (jump and link) remove label name and include return address register(r31) and offset in codeLine
            else if(labelPresent != null){
                int offset = mp.get(labelPresent)-id;
                codeLine = arr[0] + " r31 " + Integer.toString(offset);
            }
            try {
                String binary_str = getBinary(codeLine);
                System.out.println(binary_str);
                myWriter.write(binary_str+"\n");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            id++;
        }
        myWriter.close();
    }
}
