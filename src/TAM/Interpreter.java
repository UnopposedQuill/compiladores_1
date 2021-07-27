/*
 * @(#)Interpreter.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */
package TAM;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Interpreter {

    static String objectName;
    static int currentChar;
    
    // DATA STORE
    static int[] data = new int[1024];

    // DATA STORE REGISTERS AND OTHER REGISTERS
    final static int CB = 0,
            SB = 0,
            HB = 1024;  // = upper bound of data array + 1

    static int CT, CP, ST, HT, LB, status;

    // status values
    final static int RUNNING = 0, HALTED = 1, FAILEDDATASTOREFULL = 2, FAILEDINVALIDCODEADDRESS = 3,
            FAILEDINVALIDINSTRUCTION = 4, FAILEDOVERFLOW = 5, FAILEDZERODIVIDE = 6,
            FAILEDIOERROR = 7, FAILEDARRAYINDEXOUTOFBOUNDS = 8;

    static long accumulator;

    //Returns the current content of register r,
    //even if r is onhe of the pseudo-registers L1..L6
    static int content(int r) {
        return switch (r) {
            case Machine.CBr -> CB;
            case Machine.CTr -> CT;
            case Machine.PBr -> Machine.PB;
            case Machine.PTr -> Machine.PT;
            case Machine.SBr -> SB;
            case Machine.STr -> ST;
            case Machine.HBr -> HB;
            case Machine.HTr -> HT;
            case Machine.LBr -> LB;
            case Machine.L1r -> data[LB];
            case Machine.L2r -> data[data[LB]];
            case Machine.L3r -> data[data[data[LB]]];
            case Machine.L4r -> data[data[data[data[LB]]]];
            case Machine.L5r -> data[data[data[data[data[LB]]]]];
            case Machine.L6r -> data[data[data[data[data[data[LB]]]]]];
            case Machine.CPr -> CP;
            default -> 0;
        };
    }

    // PROGRAM STATUS
    // Writes a summary of the machine state.
    static void dump() {
        int addr, staticLink, dynamicLink, localRegNum;

        System.out.println("");
        System.out.println("State of data store and registers:");
        System.out.println("");
        if (HT == HB) {
            System.out.println("            |--------|          (heap is empty)");
        } else {
            System.out.println("       HB-->");
            System.out.println("            |--------|");
            for (addr = HB - 1; addr >= HT; addr--) {
                System.out.print(addr + ":");
                if (addr == HT) {
                    System.out.print(" HT-->");
                } else {
                    System.out.print("      ");
                }
                System.out.println("|" + data[addr] + "|");
            }
            System.out.println("            |--------|");
        }
        System.out.println("            |////////|");
        System.out.println("            |////////|");
        if (ST == SB) {
            System.out.println("            |--------|          (stack is empty)");
        } else {
            dynamicLink = LB;
            staticLink = LB;
            localRegNum = Machine.LBr;
            System.out.println("      ST--> |////////|");
            System.out.println("            |--------|");
            for (addr = ST - 1; addr >= SB; addr--) {
                System.out.print(addr + ":");
                if (addr == SB) {
                    System.out.print(" SB-->");
                } else if (addr == staticLink) {
                    switch (localRegNum) {
                        case Machine.LBr:
                            System.out.print(" LB-->");
                            break;
                        case Machine.L1r:
                            System.out.print(" L1-->");
                            break;
                        case Machine.L2r:
                            System.out.print(" L2-->");
                            break;
                        case Machine.L3r:
                            System.out.print(" L3-->");
                            break;
                        case Machine.L4r:
                            System.out.print(" L4-->");
                            break;
                        case Machine.L5r:
                            System.out.print(" L5-->");
                            break;
                        case Machine.L6r:
                            System.out.print(" L6-->");
                            break;
                    }
                    staticLink = data[addr];
                    localRegNum++;
                } else {
                    System.out.print("      ");
                }
                if ((addr == dynamicLink) && (dynamicLink != SB)) {
                    System.out.print("|SL=" + data[addr] + "|");
                } else if ((addr == dynamicLink + 1) && (dynamicLink != SB)) {
                    System.out.print("|DL=" + data[addr] + "|");
                } else if ((addr == dynamicLink + 2) && (dynamicLink != SB)) {
                    System.out.print("|RA=" + data[addr] + "|");
                } else {
                    System.out.print("|" + data[addr] + "|");
                }
                System.out.println("");
                if (addr == dynamicLink) {
                    System.out.println("            |--------|");
                    dynamicLink = data[addr + 1];
                }
            }
        }
        System.out.println("");
    }

    static void showStatus() {
        // Writes an indication of whether and why the program has terminated.
        System.out.println("");
        switch (status) {
            case RUNNING -> System.out.println("Program is running.");
            case HALTED -> System.out.println("Program has halted normally.");
            case FAILEDDATASTOREFULL -> System.out.println("Program has failed due to exhaustion of Data Store.");
            case FAILEDINVALIDCODEADDRESS -> System.out.println("Program has failed due to an invalid code address.");
            case FAILEDINVALIDINSTRUCTION -> System.out.println("Program has failed due to an invalid instruction.");
            case FAILEDOVERFLOW -> System.out.println("Program has failed due to overflow.");
            case FAILEDZERODIVIDE -> System.out.println("Program has failed due to division by zero.");
            case FAILEDIOERROR -> System.out.println("Program has failed due to an IO error.");
            case FAILEDARRAYINDEXOUTOFBOUNDS -> System.out.println("Program has failed due to an Index Out of Bounds");
        }
        if (status != HALTED) {
            dump();
        }
    }

    // INTERPRETATION
    static void checkSpace(int spaceNeeded) {
        // Signals failure if there is not enough space to expand the stack or
        // heap by spaceNeeded.

        if (HT - ST < spaceNeeded) {
            status = FAILEDDATASTOREFULL;
        }
    }

    static boolean isTrue(int datum) {
        // Tests whether the given datum represents true.
        return (datum == Machine.trueRep);
    }

    static boolean equal(int size, int addr1, int addr2) {
        // Tests whether two multi-word objects are equal, given their common
        // size and their base addresses.

        boolean eq;
        int index;

        eq = true;
        index = 0;
        while (eq && (index < size)) {
            if (data[addr1 + index] == data[addr2 + index]) {
                index++;
            } else {
                eq = false;
            }
        }
        return eq;
    }

    static int overflowChecked(long datum) {
        // Signals failure if the datum is too large to fit into a single word,
        // otherwise returns the datum as a single word.

        if ((-Machine.maxintRep <= datum) && (datum <= Machine.maxintRep)) {
            return (int) datum;
        } else {
            status = FAILEDOVERFLOW;
            return 0;
        }
    }

    static int toInt(boolean b) {
        return b ? Machine.trueRep : Machine.falseRep;
    }

    

    static int readInt() throws java.io.IOException {
        int temp = 0;
        int sign = 1;

        do {
            currentChar = System.in.read();
        } while (Character.isWhitespace((char) currentChar));

        if ((currentChar == '-') || (currentChar == '+')) {
            do {
                sign = (currentChar == '-') ? -1 : 1;
                currentChar = System.in.read();
            } while ((currentChar == '-') || currentChar == '+');
        }

        if (Character.isDigit((char) currentChar)) {
            do {
                temp = temp * 10 + (currentChar - '0');
                currentChar = System.in.read();
            } while (Character.isDigit((char) currentChar));
        }

        return sign * temp;
    }

    static void callPrimitive(int primitiveDisplacement) {
        // Invokes the given primitive routine.

        int addr, size;
        char ch;

        switch (primitiveDisplacement) {
            case Machine.idDisplacement:
                break; // nothing to be done
            case Machine.notDisplacement:
                data[ST - 1] = toInt(!isTrue(data[ST - 1]));
                break;
            case Machine.andDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(isTrue(data[ST - 1]) & isTrue(data[ST]));
                break;
            case Machine.orDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(isTrue(data[ST - 1]) | isTrue(data[ST]));
                break;
            case Machine.succDisplacement:
                data[ST - 1] = overflowChecked(data[ST - 1] + 1);
                break;
            case Machine.predDisplacement:
                data[ST - 1] = overflowChecked(data[ST - 1] - 1);
                break;
            case Machine.negDisplacement:
                data[ST - 1] = -data[ST - 1];
                break;
            case Machine.addDisplacement:
                ST = ST - 1;
                accumulator = data[ST - 1];
                data[ST - 1] = overflowChecked(accumulator + data[ST]);
                break;
            case Machine.subDisplacement:
                ST = ST - 1;
                accumulator = data[ST - 1];
                data[ST - 1] = overflowChecked(accumulator - data[ST]);
                break;
            case Machine.multDisplacement:
                ST = ST - 1;
                accumulator = data[ST - 1];
                data[ST - 1] = overflowChecked(accumulator * data[ST]);
                break;
            case Machine.divDisplacement:
                ST = ST - 1;
                accumulator = data[ST - 1];
                if (data[ST] != 0) {
                    data[ST - 1] = (int) (accumulator / data[ST]);
                } else {
                    status = FAILEDZERODIVIDE;
                }
                break;
            case Machine.modDisplacement:
                ST = ST - 1;
                accumulator = data[ST - 1];
                if (data[ST] != 0) {
                    data[ST - 1] = (int) (accumulator % data[ST]);
                } else {
                    status = FAILEDZERODIVIDE;
                }
                break;
            case Machine.ltDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(data[ST - 1] < data[ST]);
                break;
            case Machine.leDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(data[ST - 1] <= data[ST]);
                break;
            case Machine.geDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(data[ST - 1] >= data[ST]);
                break;
            case Machine.gtDisplacement:
                ST = ST - 1;
                data[ST - 1] = toInt(data[ST - 1] > data[ST]);
                break;
            case Machine.eqDisplacement:
                size = data[ST - 1]; // size of each comparand
                ST = ST - 2 * size;
                data[ST - 1] = toInt(equal(size, ST - 1, ST - 1 + size));
                break;
            case Machine.neDisplacement:
                size = data[ST - 1]; // size of each comparand
                ST = ST - 2 * size;
                data[ST - 1] = toInt(!equal(size, ST - 1, ST - 1 + size));
                break;
            case Machine.eolDisplacement:
                data[ST] = toInt(currentChar == '\n');
                ST = ST + 1;
                break;
            case Machine.eofDisplacement:
                data[ST] = toInt(currentChar == -1);
                ST = ST + 1;
                break;
            case Machine.getDisplacement:
                ST = ST - 1;
                addr = data[ST];
                try {
                    currentChar = System.in.read();
                } catch (java.io.IOException s) {
                    status = FAILEDIOERROR;
                }
                data[addr] = (int) currentChar;
                break;
            case Machine.putDisplacement:
                ST = ST - 1;
                ch = (char) data[ST];
                System.out.print(ch);
                break;
            case Machine.geteolDisplacement:
        try {
                while ((currentChar = System.in.read()) != '\n');
            } catch (java.io.IOException s) {
                status = FAILEDIOERROR;
            }
            break;
            case Machine.puteolDisplacement:
                System.out.println("");
                break;
            case Machine.getintDisplacement:
                ST = ST - 1;
                addr = data[ST];
                try {
                    accumulator = readInt();
                } catch (java.io.IOException s) {
                    status = FAILEDIOERROR;
                }
                data[addr] = (int) accumulator;
                break;
            case Machine.putintDisplacement:
                ST = ST - 1;
                accumulator = data[ST];
                System.out.print(accumulator);
                break;
            case Machine.newDisplacement:
                size = data[ST - 1];
                checkSpace(size);
                HT = HT - size;
                data[ST - 1] = HT;
                break;
            case Machine.disposeDisplacement:
                ST = ST - 1; // no action taken at present
                break;
            case Machine.indexCheckDisplacement:
                // Upper bound, then lower bound, then index to check
                if (data[ST - 1] <= data[ST - 3] || data[ST - 2] > data[ST - 3]) {
                    status = FAILEDARRAYINDEXOUTOFBOUNDS;// It's out of bounds
                } else {
                    ST = ST - 3;
                }
                break;
        }
    }

    static void interpretProgram() {
        // Runs the program in code store.

        Instruction currentInstr;
        int op, r, n, d, addr, index;

        // Initialize registers ...
        ST = SB;
        HT = HB;
        LB = SB;
        CP = CB;
        status = RUNNING;
        do {
            // Fetch instruction ...
            currentInstr = Machine.code[CP];
            // Decode instruction ...
            op = currentInstr.op;
            r = currentInstr.r;
            n = currentInstr.n;
            d = currentInstr.d;
            // Execute instruction ...
            //dump();//Debugging
            switch (op) {
                case Machine.LOADop:
                    addr = d + content(r);
                    checkSpace(n);
                    for (index = 0; index < n; index++) {
                        data[ST + index] = data[addr + index];
                    }
                    ST = ST + n;
                    CP = CP + 1;
                    break;
                case Machine.LOADAop:
                    addr = d + content(r);
                    checkSpace(1);
                    data[ST] = addr;
                    ST = ST + 1;
                    CP = CP + 1;
                    break;
                case Machine.LOADIop:
                    ST = ST - 1;
                    addr = data[ST];
                    checkSpace(n);
                    for (index = 0; index < n; index++) {
                        data[ST + index] = data[addr + index];
                    }
                    ST = ST + n;
                    CP = CP + 1;
                    break;
                case Machine.LOADLop:
                    checkSpace(1);
                    data[ST] = d;
                    ST = ST + 1;
                    CP = CP + 1;
                    break;
                case Machine.STOREop:
                    addr = d + content(r);
                    ST = ST - n;
                    for (index = 0; index < n; index++) {
                        data[addr + index] = data[ST + index];
                    }
                    CP = CP + 1;
                    break;
                case Machine.STOREIop:
                    ST = ST - 1;
                    addr = data[ST];
                    ST = ST - n;
                    for (index = 0; index < n; index++) {
                        data[addr + index] = data[ST + index];
                    }
                    CP = CP + 1;
                    break;
                case Machine.CALLop:
                    addr = d + content(r);
                    if (addr >= Machine.PB) {
                        callPrimitive(addr - Machine.PB);
                        CP = CP + 1;
                    } else {
                        checkSpace(3);
                        if ((0 <= n) && (n <= 15)) {
                            data[ST] = content(n); // static link
                        } else {
                            status = FAILEDINVALIDINSTRUCTION;
                        }
                        data[ST + 1] = LB; // dynamic link
                        data[ST + 2] = CP + 1; // return address
                        LB = ST;
                        ST = ST + 3;
                        CP = addr;
                    }
                    break;
                case Machine.CALLIop:
                    ST = ST - 2;
                    addr = data[ST + 1];
                    if (addr >= Machine.PB) {
                        callPrimitive(addr - Machine.PB);
                        CP = CP + 1;
                    } else {
                        // data[ST] = static link already
                        data[ST + 1] = LB; // dynamic link
                        data[ST + 2] = CP + 1; // return address
                        LB = ST;
                        ST = ST + 3;
                        CP = addr;
                    }
                    break;
                case Machine.RETURNop:
                    addr = LB - d;
                    CP = data[LB + 2];
                    LB = data[LB + 1];
                    ST = ST - n;
                    for (index = 0; index < n; index++) {
                        data[addr + index] = data[ST + index];
                    }
                    ST = addr + n;
                    break;
                case Machine.PUSHop:
                    checkSpace(d);
                    ST = ST + d;
                    CP = CP + 1;
                    break;
                case Machine.POPop:
                    addr = ST - n - d;
                    ST = ST - n;
                    for (index = 0; index < n; index++) {
                        data[addr + index] = data[ST + index];
                    }
                    ST = addr + n;
                    CP = CP + 1;
                    break;
                case Machine.JUMPop:
                    CP = d + content(r);
                    break;
                case Machine.JUMPIop:
                    ST = ST - 1;
                    CP = data[ST];
                    break;
                case Machine.JUMPIFop:
                    ST = ST - 1;
                    if (data[ST] == n) {
                        CP = d + content(r);
                    } else {
                        CP = CP + 1;
                    }
                    break;
                case Machine.HALTop:
                    status = HALTED;
                    break;
            }
            if ((CP < CB) || (CP >= CT)) {
                status = FAILEDINVALIDCODEADDRESS;
            }
        } while (status == RUNNING);
    }

// LOADING
    static void loadObjectProgram(String objectName) {
        // Loads the TAM object program into code store from the named file.

        FileInputStream objectFile = null;
        DataInputStream objectStream = null;

        int addr;
        boolean finished = false;

        try {
            objectFile = new FileInputStream(objectName);
            objectStream = new DataInputStream(objectFile);

            addr = Machine.CB;
            while (!finished) {
                Machine.code[addr] = Instruction.read(objectStream);
                if (Machine.code[addr] == null) {
                    finished = true;
                } else {
                    addr = addr + 1;
                }
            }
            CT = addr;
            objectFile.close();
        } catch (FileNotFoundException s) {
            CT = CB;
            System.err.println("Error opening object file: " + s);
        } catch (IOException s) {
            CT = CB;
            System.err.println("Error reading object file: " + s);
        }
    }

// RUNNING
    public static void main(String[] args) {
        System.out.println("********** TAM Interpreter (Java Version 2.1) **********");

        if (args.length == 1) {
            objectName = args[0];
        } else {
            objectName = "obj.tam";
        }

        loadObjectProgram(objectName);
        if (CT != CB) {
            interpretProgram();
            showStatus();
        }
    }
}
