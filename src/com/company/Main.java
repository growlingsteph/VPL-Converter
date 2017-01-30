package com.company;


import java.io.*;
import java.util.*;

public class VPLstart
{
    static final int max = 1000; //size of array
    static int[] mem = new int[max]; //array called mem with length of max
    static int ip, bp, sp, rv, hp, numPassed, gp;
  /* ip = instruction pointer keeps track of where the current instruction is located
   * bp = base pointer keeps track of the beginning of the current stack
   * sp = stack point keeps track of the current stack starting that start at 0
   * rv = return value
   * numPassed =
   * gp =
   */

    static String fileName;

    public static void main(String[] args) throws Exception
    {
        BufferedReader keys = new BufferedReader(
                new InputStreamReader( System.in));
        System.out.print("enter name of file containing VPLstart program: ");
        fileName = keys.readLine();

        // load the program into the front part of
        // memory
        BufferedReader input = new BufferedReader( new FileReader( fileName ));
        String line;
        StringTokenizer st;
        int opcode;

        ArrayList<IntPair> labels, holes;
        labels = new ArrayList<IntPair>();
        holes = new ArrayList<IntPair>();
        int label;

        int k=0;
        do {
            line = input.readLine();
            System.out.println("parsing line [" + line + "]");
            if( line != null )
            {// extract any tokens
                st = new StringTokenizer( line );
                if( st.countTokens() > 0 )
                {// have a token, so must be an instruction (as opposed to empty line)

                    opcode = Integer.parseInt(st.nextToken());

                    // load the instruction into memory:

                    if( opcode == labelCode )
                    {// note index that comes where label would go
                        label = Integer.parseInt(st.nextToken());
                        labels.add( new IntPair( label, k ) );
                    }
                    else
                    {// opcode actually gets stored
                        mem[k] = opcode;  ++k;

                        if( opcode == callCode || opcode == jumpCode ||
                                opcode == condJumpCode )
                        {// note the hole immediately after the opcode to be filled in later
                            label = Integer.parseInt( st.nextToken() );
                            mem[k] = label;  holes.add( new IntPair( k, label ) );
                            ++k;
                        }

                        // load correct number of arguments (following label, if any):
                        for( int j=0; j<numArgs(opcode); ++j )
                        {
                            mem[k] = Integer.parseInt(st.nextToken());
                            ++k;
                        }

                    }// not a label

                }// have a token, so must be an instruction
            }// have a line
        }while( line != null );

        //System.out.println("after first scan:");
        //showMem( 0, k-1 );

        // fill in all the holes:
        int index;
        for( int m=0; m<holes.size(); ++m )
        {
            label = holes.get(m).second;
            index = -1;
            for( int n=0; n<labels.size(); ++n )
                if( labels.get(n).first == label )
                    index = labels.get(n).second;
            mem[ holes.get(m).first ] = index;
        }

        System.out.println("after replacing labels:");
        showMem( 0, k-1 );

        // initialize registers:
        bp = k;  sp = k+2;  ip = 0;  rv = -1;  hp = max;
        numPassed = 0;

        int codeEnd = bp-1;

        System.out.println("Code is " );
        showMem( 0, codeEnd );

        gp = codeEnd + 1;

        // interpret the VPL code:

        int op, a, b, c, n, address;

        while(mem[ip+1] != 0){ //nextInt

            op = mem[ ip ];

            if( op == 0 ){          // Instruction #0: Do Nothing
            }
            else if( op == 1 ) {    // Instruction #1: All occurrences of L are replaced by the actual index
                                    // in mem array where the opcode 1 would have been stored.

            }
            else if( op == 2) {     // Instruction #2: Set up for execution of the subprograms that begins at label L.

            }
            else if( op == 3) {     // Instruction #3: Push the contents of cell a on the stack.
                a = mem[ ip+1 ];
                mem[ sp ] = a;      // store a where the sp is currently pointing
                ip += 2;            // move instruction pointer next instruction
            }
            else if( op == 4 ) {    // Instruction #4: Increase sp by n to make space for local variables.
                                    // in the current stack frame.
                n = mem[ ip+1 ];    // store the argument n
                sp += n;            // move the stack point n spaces
                ip += 2;            // move instruction pointer next instruction
            }
            else if( op == 5 ) {    // Instruction #5: Return from the current subprogram,
                                    // including putting the value stored in call a in rv.
                a = mem[ ip+1 ];    // store a
                rv = mem[ bp + 2 + a];  // Store contents of cell a in rv
                bp = mem[ bp ];     // base pointer is
                sp = mem[ bp + 1 ]; // stack pointer
                ip += 2;            // move instruction pointer next instruction
            }

        // use mem[ bp + 2 + a ] for cell a
            else if( op == 6) {     // Instruction #6: Copy the value store in rv into cell a
                a = mem[ ip+1 ];    // store a
                mem[ bp + 2 + a] = rv;      // store contents of cell rv into cell a
                ip += 2;            // move instruction pointer next instruction
            }
            else if( op == 7 ){     // Instruction #7: Change instruction point to L.

            }
            else if( op == 8 ){     // Instruction #8: If the value stored in cell a is non-zero, change
                                    // instruction pointer to L (otherwise, move ip to the next instruction).
                a = mem[ ip+2 ];    // store a
                int temp = mem[bp + 2 + a]; // store contents of cell a as temp

                if( temp != 0 ){            // if contents of cell a "temp" is non-zero, execute--
                    int L = mem[ ip+1 ];    // store L
                    ip = L;         // change instruction point to L
                } else { ip += 3; } // Otherwise, move instruction pointer next instruction
            }
            else if( op == 9 ){     // Instruction #9: Add the values in cell b and cell c
                                    // and store the result in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] + mem[ bp+2+c ];    // add contents of cells b,c then store in cell a
                // ^^^ a = b+c

                ip += 4;            // move instruction pointer next instruction
            }
            else if( op == 10 ){    // Instruction #10: Subtract the values in cell b and cell c
                                    // and store the result in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] - mem[ bp+2+c ];    // subtract contents of cells b,c then store in cell a
                // ^^^ a = b-c

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 11 ){    // Instruction #11: Multiply the values in cell b and cell c
                                    // and store the result in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] * mem[ bp+2+c ];    // multiply contents of cells b,c then store in cell a
                // ^^^ a = b*c

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 12 ){     // Instruction #12: Divide the values in cell b and cell c
                                    // and store the result in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] / mem[ bp+2+c ];    // divide contents of cells b,c then store in cell a
                // ^^^ a = b/c

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 13 ){    // Instruction #13: The remainder of cells b is stored in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] % mem[ bp+2+c ];    // remainder of cells b/c is stored in cell a

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 14 ){    // Instruction #14: If the values in cell b and cell c are equal,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] == mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;      // if equal store 1 in cell 1
                } else { mem[ bp+2+a ] = 0;}    // otherwise, store 0

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 15 ){    // Instruction #15: If the values in cell b and cell c are not equal,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] != mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 15 ){    // Instruction #15: If the values in cell b and cell c are not equal,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] != mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 16 ){    // Instruction #16: If the value in cell b is less than cell c,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] < mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 17 ){    // Instruction #17: If the value in cell b is less than or great than cell c,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] <= mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 18 ){    // Instruction #18: If the value in cell b is 1 AND the value in cell c is 1,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] == 1 && mem[ bp+2+c] == 1){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op == 19 ){    // Instruction #19: If the value in cell b is 1 OR the value in cell c is 1,
                                    // store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    // store a value
                b = mem[ ip+2 ];    // store b value
                c = mem[ ip+3 ];    // store c value

                if(mem[ bp+2+b] == 1 || mem[ bp+2+c] == 1){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}

                ip += 4;            // move instruction pointer next instruction

            }
            else if( op== 20 ){     // Instruction #20: If cell b holds zero, put 1 in cell a, other put 0.

                a = mem[ ip+1];     // store a value
                b = mem[ ip+2];     // store b value

                if(mem[ bp+2+b ] == 0){     // if the contents in cell b is 0,
                    mem[ bp+2+a ] = 1;      // store 1 in cell a
                } else { mem[ bp+2+a ] = 0; }       // otherwise store 0 in cell a

            }
            else if( op== 21 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }
            else if( op== 20 ){     // Instruction #

            }


        } // end while loop

    }// main

    // use symbolic names for all opcodes:

    // op to produce comment on a line by itself
    private static final int noopCode = 0;

    // ops involved with registers
    private static final int labelCode = 1;
    private static final int callCode = 2;
    private static final int passCode = 3;
    private static final int allocCode = 4;
    private static final int returnCode = 5;    // return a means "return and put
                                                // copy of value stored in cell a in register rv
    private static final int getRetvalCode = 6; //op a means "copy rv into cell a"
    private static final int jumpCode = 7;
    private static final int condJumpCode = 8;

    // arithmetic ops
    private static final int addCode = 9;
    private static final int subCode = 10;
    private static final int multCode = 11;
    private static final int divCode = 12;
    private static final int remCode = 13;
    private static final int equalCode = 14;
    private static final int notEqualCode = 15;
    private static final int lessCode = 16;
    private static final int lessEqualCode = 17;
    private static final int andCode = 18;
    private static final int orCode = 19;
    private static final int notCode = 20;
    private static final int oppCode = 21;

    // ops involving transfer of data
    private static final int litCode = 22;  // litCode a b means "cell a gets b"
    private static final int copyCode = 23; // copy a b means "cell a gets cell b"
    private static final int getCode = 24;  // op a b means "cell a gets
                                            // contents of cell whose
                                            // index is stored in b"
    private static final int putCode = 25;  // op a b means "put contents
                                            // of cell b in cell whose offset is stored in cell a"

    // system-level ops:
    private static final int haltCode = 26;
    private static final int inputCode = 27;
    private static final int outputCode = 28;
    private static final int newlineCode = 29;
    private static final int symbolCode = 30;
    private static final int newCode = 31;

    // global variable ops:
    private static final int allocGlobalCode = 32;
    private static final int toGlobalCode = 33;
    private static final int fromGlobalCode = 34;

    // debug ops:
    private static final int debugCode = 35;

    // return the number of arguments after the opcode,
    // except ops that have a label return number of arguments
    // after the label, which always comes immediately after
    // the opcode
    private static int numArgs( int opcode )
    {
        // highlight specially behaving operations
        if( opcode == labelCode ) return 1;  // not used
        else if( opcode == jumpCode ) return 0;  // jump label
        else if( opcode == condJumpCode ) return 1;  // condJump label expr
        else if( opcode == callCode ) return 0;  // call label

            // for all other ops, lump by count:

        else if( opcode==noopCode ||
                 opcode==haltCode ||
                 opcode==newlineCode ||
                 opcode==debugCode
                )
            return 0;  // op

        else if( opcode==passCode ||
                 opcode==allocCode ||
                 opcode==returnCode ||
                 opcode==getRetvalCode ||
                 opcode==inputCode ||
                 opcode==outputCode ||
                 opcode==symbolCode ||
                 opcode==allocGlobalCode
                )
            return 1;  // op arg1

        else if( opcode==notCode ||
                 opcode==oppCode ||
                 opcode==litCode ||
                 opcode==copyCode ||
                 opcode==newCode ||
                 opcode==toGlobalCode ||
                 opcode==fromGlobalCode

                )
            return 2;  // op arg1 arg2

        else if( opcode==addCode ||
                 opcode==subCode ||
                 opcode==multCode ||
                 opcode==divCode ||
                 opcode==remCode ||
                 opcode==equalCode ||
                 opcode==notEqualCode ||
                 opcode==lessCode ||
                 opcode==lessEqualCode ||
                 opcode==andCode ||
                 opcode==orCode ||
                 opcode==getCode ||
                 opcode==putCode
                )
            return 3;

        else
        {
            System.out.println("Fatal error: unknown opcode [" + opcode + "]" );
            System.exit(1);
            return -1;
        }

    }// numArgs

    private static void showMem( int a, int b )
    {
        for( int k=a; k<=b; ++k )
        {
            System.out.println( k + ": " + mem[k] );
        }
    }// showMem

}// VPLstart
