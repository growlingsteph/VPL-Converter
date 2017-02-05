import java.io.*;
import java.util.*;

public class VPL{
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

    public static void main(String[] args) throws Exception{
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

        System.out.println("ip: " + ip + "; bp: " + bp + "; sp: " + sp + "; gp: " + gp + "; hp: "+ hp);


        while( mem[ ip ] != 26 ){ //nextInt

            op = mem[ ip ];

            if( op == noopCode ){          		// Instruction #0: Do Nothing
                ip = ip +1;                     // move ip to next instruction
            }
            else if( op == labelCode ) {    	// Instruction #1: All occurrences of L are replaced by the actual index
                                    			// in mem array where the opcode 1 would have been stored.
                                    			// Do nothing
            }
            else if( op == callCode) {     		// Instruction #2: Set up for execution of the
                                            	// subprograms that begins at label L.
                System.out.println("executing instr. #2 \nold ip: " + ip + "; old bp: " + bp + "; old sp: " + sp + "; old gp: " + gp);
                address = mem[ ip+1 ];			// store address
                System.out.println("address: " + address);
                mem[ sp + numPassed ] = bp;     // store previous bp
                bp = sp + numPassed;            // move bp to current sp + numPassed
                mem[ bp+1 ] = ip + 2;           // store ip of previous next instruction
                ip = address;			   		// move ip to new address
                sp = bp + 2;                     // move sp to the end of the current stack, passed the numbers passed to stack.
                numPassed = 0;                  // reset numPassed variable
                System.out.println("new ip: " + ip + "; new bp: " + bp + "; new sp: " + sp + "; new gp: " + gp);
            }
            else if( op == passCode ) {     	// Instruction #3: Push the contents of cell a on the stack.
                a = mem[ ip+1 ];                // store a value
                System.out.println("Instruction #3: a = " + a);
                mem[ sp + numPassed ] = mem[ bp+2+a ];      // store the contents of a in the first empty cell on the stack
                System.out.println("contents of cell " + a + ": " + mem[ bp+2+a ] + "; contents pushed to cell sp + numpassed = " + mem[ sp + numPassed ]);
                numPassed++;                    // increment numPassed
                ip = ip + (numArgs(op) + 1);      // move instruction pointer to next instruction
                System.out.println("after instr. #3: \nvalue in cell " + a + " pushed to stack in cell " + (sp+numPassed) + "; numPassed = " + numPassed);
            }
            else if( op == allocCode ) {    	// Instruction #4: Increase sp by n to make space for local variables.
                                    			// in the current stack frame.
                n = mem[ ip+1 ];    			// store the argument n
                System.out.println("executing instr. #4: \nold sp " + sp + "; n: " + n);
                sp = sp + n;           		// move the stack point n spaces from OG sp
	            numPassed = 0;                  // start counting the numbers passed to the stack
                ip = ip + (numArgs(op) + 1);      // move instruction pointer to next instruction
                System.out.println("new sp " + sp );
            }
            else if( op == returnCode ) {    	// Instruction #5: Return from the current subprogram,
                                    			// including putting the value stored in call a in rv.
                System.out.println("executing instr. #5 \nold ip: " + ip + "; old bp: " + bp + "; old sp: " + sp + "; old gp: " + gp);
                a = mem[ ip+1 ];    			// store a value
                rv = mem[ bp + 2 + a];  		// Store contents of cell a in rv
                ip = mem[ bp+1 ];               // restore previous ip address
                sp = bp-1;                        // restore previous sp address
                bp = mem[ bp+0 ];               // restore previous bp address
                System.out.println("new ip: " + ip + "; new bp: " + bp + "; new sp: " + sp + "; new gp: " + gp);
            }
            else if( op == getRetvalCode) {     // Instruction #6: Copy the value store in rv into cell a
                a = mem[ ip+1 ];    			// store a value
                mem[ bp + 2 + a] = rv;      	// store contents of cell rv into cell a
                ip = ip + (numArgs(op) + 1);      // move instruction pointer to next instruction
            }
            else if( op == jumpCode ){     		// Instruction #7: Change instruction point to L.
                address = mem[ ip+1 ];        	// store address
                ip = address;
                System.out.println("end of 7 ip: " + ip);
            }
            else if( op == condJumpCode ){     	// Instruction #8: If the value stored in cell a is non-zero, change
                                    			// instruction pointer to L (otherwise, move ip to the next instruction).
                System.out.println("start of 8 ip: " + ip);
                System.out.println("start of 8 op: " + op);
                a = mem[ ip+2 ];    			// store a value
                int temp = mem[bp + 2 + a]; 	// store contents of cell a as temp

                if( temp != 0 ){            	// if contents of cell a "temp" is non-zero, execute--
                    address = mem[ ip+1 ];    	// store address
                    ip = address;         		// change instruction pointer to new address
                } else { ip = ip + 3; } // Otherwise, move instruction pointer next instruction
                System.out.println("end of 8 ip: " + ip);
            }
            else if( op == addCode ){     		// Instruction #9: Add the values in cell b and cell c
                                    			// and store the result in cell a.

                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] + mem[ bp+2+c ];    // add contents of cells b,c then store in cell a
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the sum is: " + mem[ bp+2+a ]);
                // ^^^ a = b+c
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == subCode ){    		// Instruction #10: Subtract the values in cell b and cell c
                                    			// and store the result in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] - mem[ bp+2+c ];    // subtract contents of cells b,c then store in cell a

                // ^^^ a = b-c
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the subtract result is: " + mem[ bp+2+a ]);
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == multCode ){    		// Instruction #11: Multiply the values in cell b and cell c
                                    			// and store the result in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] * mem[ bp+2+c ];    // multiply contents of cells b,c then store in cell a
                // ^^^ a = b*c
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the multiply result is: " + mem[ bp+2+a ]);
            }
            else if( op == divCode ){     		// Instruction #12: Divide the values in cell b and cell c
                                    			// and store the result in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] / mem[ bp+2+c ];    // divide contents of cells b,c then store in cell a
                // ^^^ a = b/c
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the divide result is: " + mem[ bp+2+a ]);
            }
            else if( op == remCode ){    		// Instruction #13: The remainder of cells b is stored in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                mem[ bp+2+a ] = mem[ bp+2+b ] % mem[ bp+2+c ];    // remainder of cells b/c is stored in cell a
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the remainder result is: " + mem[ bp+2+a ]);
            }
            else if( op == equalCode ){    		// Instruction #14: If the values in cell b and cell c are equal,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] == mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;      	// if equal store 1 in cell 1
                } else { mem[ bp+2+a ] = 0;}    // otherwise, store 0
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the is-equal result is: " + mem[ bp+2+a ]);
            }
            else if( op == notEqualCode ){    	// Instruction #15: If the values in cell b and cell c are not equal,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] != mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the is-not equal result is: " + mem[ bp+2+a ]);
            }
            else if( op == lessCode ){    		// Instruction #16: If the value in cell b is less than cell c,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] < mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the less-than result is: " + mem[ bp+2+a ]);
            }
            else if( op == lessEqualCode ){  	// Instruction #17: If the value in cell b is less than or equal to cell c,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] <= mem[ bp+2+c]){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the less-than or equal-to result is: " + mem[ bp+2+a ]);
            }
            else if( op == andCode ){    		// Instruction #18: If the value in cell b is 1 AND the value in cell c is 1,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] == 1 && mem[ bp+2+c] == 1){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the 1-and-1 result is: " + mem[ bp+2+a ]);
            }
            else if( op == orCode ){    		// Instruction #19: If the value in cell b is 1 OR the value in cell c is 1,
                                    			// store value 1 in cell a, otherwise store 0 in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value

                if(mem[ bp+2+b] == 1 || mem[ bp+2+c] == 1){
                    mem[ bp+2+a ] = 1;
                } else { mem[ bp+2+a ] = 0;}
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; contents of cell " + c + ": " + mem[ bp+2+c ] + "; the 1-or-1 result is: " + mem[ bp+2+a ]);
            }
            else if( op == notCode ){    		// Instruction #20: If cell b holds zero, put 1 in cell a, otherwise put 0.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value

                if(mem[ bp+2+b ] == 0){     	// if the contents in cell b is 0,
                    mem[ bp+2+a ] = 1;      	// store 1 in cell a
                } else { mem[ bp+2+a ] = 0; }   // otherwise store 0 in cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": "  + mem[ bp+2+b ] + "; the has-zero result is: " + mem[ bp+2+a ]);
            }
            else if( op == oppCode ){    		// Instruction #21: Put the opposite of the contents of cell b in cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                mem[ bp+2+a ] = mem[ bp+2+b ] * -1;		// put the opposite of the contents of cell b into cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("contents of cell " + b + ": " + mem[ bp+2+b] + "; the opposite result is: " + mem[ bp+2+a ]);
            }
            else if( op == litCode ){    		// Instruction #22: Put n in cell a.
                a = mem[ ip+1 ];    			// store n value
                n = mem[ ip+2 ];    			// store a value
                mem[ bp+2+a ] = n;  			// storing n in cell a
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("executing instr. #22: \nvalue: " + n + " stored in cell " + a);
            }
            else if( op == copyCode ){    		// Instruction #23: Copy the value in cell b into cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                mem[ bp+2+a ] = mem[ bp+2+b ];  // store the contents of cell b into cell a
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("end of 23 ip: " + ip);
            }
            else if( op == getCode ){    		// Instruction #24: Get the value stored in the heap at the index
                                        		// obtained by adding the value of cell b and the value of cell c
                                                // and copy it into cell a.
                a = mem[ ip+1 ];    			// store a value
                b = mem[ ip+2 ];    			// store b value
                c = mem[ ip+3 ];    			// store c value
                int location = mem[ bp+2+b ] + mem[ bp+2+c ];      // store the sum of the contents in cells b and c
                mem[ bp+2+a] = mem[ location ];     // use the sum "temp" as the index in the heap then copy to local cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("executing instr. #24: \n location= " + location + "; contents in cell " + a + ": " + mem[ bp+2+a ]);
            }
            else if( op == putCode ) {    		// Instruction #25: Take the value from cell c
                								// and store it in the heap at the location with index
                								// computed as the value in cell a plus the value in cell b
                a = mem[ip + 1];    			// store a value
                b = mem[ip + 2];    			// store b value
                c = mem[ip + 3];    			// store c value
                int location = mem[bp + 2 + a] + mem[bp + 2 + b];  	// cell location computed by adding contents of cell a & b
                mem[location] = mem[bp + 2 + c];       // store the contents of cell c into new location
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("Instr. #25: \nlocation= " + location + "; contents in cell location: " + mem[location]);
            }
            else if( op == haltCode ){    		// Instruction #26: Halt Execution
                break;
            }
            else if( op == inputCode ){    		// Instruction #27: Print a question mark and a space ?_ in the console
                                            	// and wait for an integer value typed by the user,
                                            	// and then store it in cell a.
                System.out.println("start of 27 ip: " + ip);
                a = mem[ ip+1 ];                // store value a
                Scanner keyboard = new Scanner(System.in);
                System.out.println("? ");       // print ?_
                int myInt = keyboard.nextInt();// user input is stored into cell temp location
                //BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
                //int myInt = Integer.parseInt(sysIn.readLine());
                mem[ bp+2+a ] = myInt;           // store input in cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("end of 27 ip: " + ip);
            }
            else if( op == outputCode ){    	// Instruction #28: Display the value store in a cell a in the console.
                a = mem[ ip+1 ];                // store value a
                System.out.println( "Contents of cell " + a + " :" + mem[ bp+2+a ] );        // print contents of cell a
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == newlineCode ){    	// Instruction #29: Move the console cursor to
                                            	// the beginning of the next line.
                System.out.println("\n");
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == symbolCode ){    	// Instruction #30: If the value stored in cell a is between 32 and 126,
                                            	// display the corresponding symbol at the console cursor,
                                            	// otherwise, do nothing
                a = mem[ip + 1];    			// store a value
                int temp = mem[ bp+2+a ]; 		// store contents of cell a in temp
                if( a > 31 && a < 127 ){ 		// if temp is between 32 and 126 then....
                    System.out.println((char)temp);		// print symbol
                }
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == newCode ){    		// Instruction #31: Let the value stored in cell b be denoted by m.
                                        		// Decrease hp by m and put the new value of hp in cell a.
                a = mem[ip + 1];    			// store a value
                b = mem[ip + 2];    			// store b value
                int m = mem[ bp+2+b ]; 			// contents of cell b denoted by m
                hp = hp - m;                    // hp is decreased by m
                mem[ bp+2+a ] = hp;             // new value of hp in cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
                System.out.println("instr. #31 executed; contents of cell " + b + "; m = " + m + "; hp = " + hp + "; contents of cell " + a + ": " + mem[ bp+2+a ]);
            }
            else if( op == allocGlobalCode ){   // Instruction #32: This instruction must occur first in any program
                                                // that uses it. It simply sets the initial value of
                                                // sp to n cells beyond the end of stored program
                                                // memory, and sets gp to the end of stored program memory.
                n = mem[ ip+1 ];    			// store n value
                bp = gp+n;						// moves bp n space after gp
                sp = bp+2;						// moves sp 2 spaces after bp
	            ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            }
            else if( op == toGlobalCode ){    	// Instruction #33: Copy the contents of cell a to the
                                            // global memory area at index gp+n.
                n = mem[ip + 1];                // store n value
                a = mem[ip + 2];                // store a value
                mem[gp + n] = mem[bp + 2 + a];    // stores contents of local cell a into global cell n
                ip += numArgs(op) + 1;            // move instruction pointer to next instruction

            }
            else if( op == fromGlobalCode ){    // Instruction #34:
                n = mem[ip + 1];                // store n value
                a = mem[ip + 2];                // store a value
                mem[bp + 2 + a] = mem[gp + n];    // stores contents of glocal cell n into local cell a
                ip += numArgs(op)+1;    		// move instruction pointer to next instruction
            } // end last else-if statement
            
        } // end while loop

        System.out.println("Code is " );
        showMem( 0, sp+10 );

    } // end main

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

}// VPL