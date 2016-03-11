import jdk.nashorn.internal.runtime.OptimisticReturnFilters;

class RobotControl
{
   private Robot r;
   public RobotControl(Robot r)
   {
       this.r = r;
   }

   public void control(int barHeights[], int blockHeights[])
   {//sampleControlMechanism(barHeights,blockHeights);
//controlMechanismLoopedA(barHeights, blockHeights);
//controlMechanismLoopedB(barHeights, blockHeights);
controlMechanismOptimisedB(barHeights, blockHeights);
     

   }
   public void controlMechanismBasicA(int barHeights[], int blockHeights[]) {
	   int height = 2;         // Initial height of arm 1
	   int width = 1;         // Initial width of arm 2
	   int drop = 0;         // Initial depth of arm 3
       int dropPos = height - drop;
	   int stackHeight = 12; //how do I add this shit up

	   height = moveVerticalTo(13,height);
	   width = moveHorizontalTo(10,width);
       r.pick();
       stackHeight = stackHeight - 3;
       width = moveHorizontalTo(5,width);
       drop = moveCraneTo(2,drop);
       r.drop();
       width = moveHorizontalTo(10,width);
       drop = moveCraneTo(3,drop);
       r.pick();
       stackHeight = stackHeight - 3;
       drop = moveCraneTo(2,drop);
       width = moveHorizontalTo(6,width);
       r.drop();
       width = moveHorizontalTo(10,width);
       drop = moveCraneTo(6,drop);
       r.pick();
       stackHeight = stackHeight - 3;
       drop = moveCraneTo(2,drop);
       width = moveHorizontalTo(7,width);
       r.drop();
       width = moveHorizontalTo(10,width);
       drop = moveCraneTo(9,drop);
       r.pick();
       drop = moveCraneTo(2,drop);
       width = moveHorizontalTo(8,width);
       r.drop();

}
    public void controlMechanismLoopedA(int barHeights[], int blockHeights[]) {
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = 4;
        int NoThreeBlocks = 4;
        int ThreesDropPoint = 9 - NoThreeBlocks;
        int stackHeight = 12; //we can assume height of stack does not change for A

        height = moveVerticalTo(13,height);

        while(NumberBlocks !=0) {
            width = moveHorizontalTo(10, width);
            drop = moveCraneTo((height-1) - stackHeight ,drop);
            r.pick();
            width = moveHorizontalTo(9, width);
            drop = moveCraneTo(2, drop);
            stackHeight = stackHeight - 3;
            width = moveHorizontalTo(ThreesDropPoint, width);
            ThreesDropPoint++;
            r.drop();
            NumberBlocks--;
        }
    }
    public void controlMechanismLoopedB(int barHeights[], int blockHeights[]) {
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = 4;
        int NoThreeBlocks = blockHeights.length; // Count the number of blocks- I need to count only 3s
        int ThreesDropBarNumber = 9 - NoThreeBlocks;
        //setting stack height total for the end.
        int StackHeight = 0;
        int BlockRuns = blockHeights.length;
        //building in some basic collision detection
        int[] BarOptimised = optimisePathing(barHeights);
        while(BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];

            BlockRuns--;
        }
    int MaxHeight = 0;
    int BarRuns = 6;
    while(BarRuns != 0) {
            if(MaxHeight < barHeights[BarRuns-1]){
                MaxHeight = barHeights[BarRuns-1];
            }
            BarRuns--;
        }
        height = moveVerticalTo(13,height);
        while(NumberBlocks !=0) {
            //Figure out the height of the current block in the array
            int TopBlockNumber = blockHeights.length;
            int CurrentBlock = blockHeights[TopBlockNumber - 1];
            --TopBlockNumber;
            //Move to to stack
            width = moveHorizontalTo(10, width);
            //Drop to the top position of the stack
            drop = moveCraneToPosition(StackHeight + 1,height,drop);
            r.pick();
            //move to a spot where we can safely change our vertical height- legacy code from Part A and not necessary - a soltution that does not require the horizontal move could be devised, but in number of moves is equivalent, and this is cleaner to see, but it works
            width = moveHorizontalTo(9, width);
            drop = moveCraneTo(2, drop);
            StackHeight = StackHeight - 3;
            width = moveHorizontalTo(ThreesDropBarNumber, width);
            int currentBarHeight = barHeights[ThreesDropBarNumber - 3];
            drop = moveCraneToPosition(currentBarHeight + 1 + CurrentBlock ,height,drop);
            r.drop();
            if(currentBarHeight + 3 > MaxHeight){
                MaxHeight = currentBarHeight + 3;
            }
            if(MaxHeight >= StackHeight+1){
                drop = moveCraneToPosition(MaxHeight+1,height,drop);
            }
            else if(StackHeight+1 > MaxHeight){
                drop = moveCraneToPosition(StackHeight + 1,height,drop);
            }
            ThreesDropBarNumber++;
            NumberBlocks--;
        }
    }
    public void controlMechanismOptimisedB(int barHeights[], int blockHeights[]) {
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = 4;
        int NoThreeBlocks = blockHeights.length; // Count the number of blocks- I need to count only 3s
        int ThreesDropBarNumber = 9 - NoThreeBlocks;
        //setting stack height total for the end.
        int StackHeight = 0;
        int BlockRuns = blockHeights.length;
        //Actually useable Optimised stuff
        int[] BarOptimised = optimisePathing(barHeights);
        while(BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];

            BlockRuns--;
        }
        int MaxHeight = 0;
        int BarRuns = 6;
        while(BarRuns != 0) {
            if(MaxHeight < barHeights[BarRuns-1]){
                MaxHeight = barHeights[BarRuns-1];
            }
            BarRuns--;
        }
        height = moveVerticalTo(13,height);
        while(NumberBlocks !=0) {
            //Figure out the height of the current block in the array
            int TopBlockNumber = blockHeights.length;
            int CurrentBlock = blockHeights[TopBlockNumber - 1];
            --TopBlockNumber;
            //Move to to stack
            width = moveHorizontalTo(10, width);
            //Drop to the top position of the stack
            drop = moveCraneToPosition(StackHeight + 1,height,drop);
            r.pick();
            //move to a spot where we can safely change our vertical height- legacy code from Part A and not necessary - a soltution that does not require the horizontal move could be devised, but in number of moves is equivalent, and this is cleaner to see, but it works
            width = moveHorizontalTo(9, width);
            drop = moveCraneTo(2, drop);
            StackHeight = StackHeight - 3;
            width = moveHorizontalTo(ThreesDropBarNumber, width);
            int currentBarHeight = barHeights[ThreesDropBarNumber - 3];
            drop = moveCraneToPosition(currentBarHeight + 1 + CurrentBlock ,height,drop);
            r.drop();
            if(currentBarHeight + 3 > MaxHeight){
                MaxHeight = currentBarHeight + 3;
            }
            if(MaxHeight >= StackHeight+1){
                drop = moveCraneToPosition(MaxHeight+1,height,drop);
            }
            else if(StackHeight+1 > MaxHeight){
                drop = moveCraneToPosition(StackHeight + 1,height,drop);
            }
            ThreesDropBarNumber++;
            NumberBlocks--;
        }
    }
	//A listing of movement methods.
    public int moveVerticalTo(int MoveTo, int Position) { //We get the position to move to and the current position passed to us
        while (Position != MoveTo) { //when we have not moved to the correct position proceed
            if (MoveTo < 15 && MoveTo > 0) { //Sanity checking to prevent moving to outside boundaries

                if (Position < MoveTo) { //Move up, increment position
                    r.up();
                    Position++;
                }
                if (Position > MoveTo) { //move down, decrement position
                    r.down();
                    Position--;
                }
            } else if (MoveTo > 14) { //
                MoveTo = 14;
            } else {
                MoveTo = 0;
            }
        }
	return (Position);
    }
	public int moveHorizontalTo(int MoveTo, int Position) {
        while (Position != MoveTo) {
            if (MoveTo < 11 && MoveTo > 0) {

                if (Position < MoveTo) {
                    r.extend();
                    Position++;
                }
                if (Position > MoveTo) {
                    r.contract();
                    Position--;
                }
            } else if (MoveTo > 10) {
                MoveTo = 10;
            } else {
                MoveTo = 1;
            }
        }
            return (Position);
    }
	public int moveCraneTo(int MoveTo, int Position) {
        while (Position != MoveTo) {
            if (MoveTo < 13 && MoveTo > -1) {

                if (Position < MoveTo) {
                    r.lower();
                    Position++;
                }
                if (Position > MoveTo) {
                    r.raise();
                    Position--;
                }

            } else if (MoveTo > 12) {
                MoveTo = 12;
            } else {
                MoveTo = 0;
            }
        }
		return (Position);
	}
    public int moveCraneToPosition(int MoveTo, int Height, int Drop) {
        int Position = Height - Drop;
        while (Position != MoveTo) {
            Position = Height - Drop;
            if (MoveTo < 15 && MoveTo > -1) {

                if (Position < MoveTo) {
                    r.raise();
                    Drop--;
                }
                if (Position > MoveTo) {
                    r.lower();
                    Drop++;
                }

            } else if (MoveTo > 14) {
                MoveTo = 14;
            } else {
                MoveTo = 0;
            }
        }
        return (Drop);
    }
    public int[] optimisePathing(int barHeights[]){
        //Finding optimal bar - magic happens here
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0,0,0,0,0,0};
        while(BarRuns <= barHeights.length - 1) {
            int[] OptimisationBars = {0,0,0,0,0,0,0};
            OptimisationBars[BarRuns] = (7-barHeights[BarRuns])+(6-BarRuns);
            int[] Optimisation = {0,0,0,0,0,0,0};
            byte r = 0;
            if(OptimisationBars[BarRuns] <= Optimisation[0]){
                while(r <= 6){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[0]=OptimisationBars[BarRuns];
                BarNumbers[0]=BarNumbers[BarRuns];
            }
            else if(OptimisationBars[BarRuns] <= Optimisation[1]){
                r = 1;
                while(r <= 5){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[1]=OptimisationBars[BarRuns];
                BarNumbers[1]=BarNumbers[BarRuns];
            }
            else if(OptimisationBars[BarRuns] <= Optimisation[2]){
                r = 2;
                while(r <= 4){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[2]=OptimisationBars[BarRuns];
                BarNumbers[2]=BarNumbers[BarRuns];
            }
            else if(OptimisationBars[BarRuns] <= Optimisation[3]){
                r = 3;
                while(r <= 3){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[3]=OptimisationBars[BarRuns];
                BarNumbers[3]=BarNumbers[BarRuns];
            }
            else if(OptimisationBars[BarRuns] <= Optimisation[4]){
                r = 4;
                while(r <= 2){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[4]=OptimisationBars[BarRuns];
                BarNumbers[4]=BarNumbers[BarRuns];
            }
            else if(OptimisationBars[BarRuns] <= Optimisation[5]){
                r = 5;
                while(r <= 1){
                    Optimisation[r+1]=Optimisation[r];
                    BarNumbers[r+1]=BarNumbers[r];
                    r++;
                }
                Optimisation[5]=OptimisationBars[BarRuns];
                BarNumbers[5]=BarNumbers[BarRuns];
            }
            BarRuns++;
        }
        //convert the bar numbers array to coordinates
        int x = 0;
while(x <= 5){
    BarNumbers[x] = BarNumbers [x]+3;
    x++;
}
        return(BarNumbers);
    }
   public void sampleControlMechanism(int barHeights[], int blockHeights[])
   {
	// Internally the Robot object maintains the value for Robot height(h), 
	     // arm-width (w) and picker-depth (d).

	     // These values are displayed for your convenience
	     // These values are initialised as h=2 w=1 and d=0

	     // When you call the methods up() or down() h will be changed   
	     // When you call the methods extend() or contract() w will be changed   
	     // When you call the methods lower() or raise() d will be changed   


	     // sample code to get you started
	     // Try running this program with obstacle 555555 and blocks of height 2222 (default)
	     // It will work for first block only
	     // You are free to introduce any other variables

	   

	     int h = 2;         // Initial height of arm 1
	     int w = 1;         // Initial width of arm 2  
	     int d = 0;         // Initial depth of arm 3

int sourceHt = 12;      

	     // For Parts (a) and (b) assume all four blocks are of the same height
	     // For Part (c) you need to compute this from the values stored in the 
	     // array blockHeights
	     // i.e.  sourceHt = blockHeights[0] + blockHeights[1] + ...  use a loop!
	 
	     int targetCol1Ht = 0;    // Applicable only for part (c) - Initially empty
	     int targetCol2Ht = 0;    // Applicable only for part (c) - Initially empty

	     // height of block just picked will be 3 for parts A and B
	     // For part (c) this value must be extracing the topmost unused value 
	     // from the array blockHeights

	     int blockHt = 3;      


	     // clearance should be based on the bars, the blocks placed on them, 
	     // the height of source blocks and the height of current block

	     // Initially clearance will be determined by the blocks at source (3+3+3+3=12)
	     // as they are higher than any bar and block-height combined 

	     int clearence = 12;  

	     // Raise it high enough - assumed max obstacle = 4 < sourceHt 
	     while ( h < clearence + 1 ) 
	     {
	         // Raising 1
	         r.up();     

	         // Current height of arm1 being incremented by 1
	         h++;
	     }

	     System.out.println("Debug 1: height(arm1)= "+ h + " width (arm2) = "+
	                        w + " depth (arm3) =" + d); 

	     // this will need to be updated each time a block is dropped off
	     int extendAmt = 10;

	     // Bring arm 2 to column 10
	     while ( w < extendAmt )
	     {
	        // moving 1 step horizontally
	        r.extend();

	        // Current width of arm2 being incremented by 1
	        w++;
	     }

	     System.out.println("Debug 2: height(arm1)= " + h + " width (arm2) = "+
	                        w + " depth (arm3) =" + d); 

	     // lowering third arm - the amount to lower is based on current height
	     //  and the top of source blocks

	     // the position of the picker (bottom of third arm) is determined by h and d
	     while ( h - d > sourceHt + 1)   
	     {
	        // lowering third arm
	        r.lower();

	        // current depth of arm 3 being incremented
	        d++;
	     }


	     // picking the topmost block 
	     r.pick();

	     // topmost block is assumed to be 3 for parts (a) and (b)
	     blockHt = 3;

	     // When you pick the top block height of source decreases   
	     sourceHt -= blockHt;

	     // raising third arm all the way until d becomes 0
	     while ( d > 0)
	     {
	         r.raise();
	         d--;
	     } 

	     System.out.println("Debug 3: height(arm1)= " + h + " width (arm2) = "+
	                        w + " depth (arm3) =" + d); 

	     // why not see the effect of changing contractAmt to 6 ? 
	     int contractAmt = 7;

	     // Must be a variable. Initially contract by 3 units to get to column 3
	     // where the first bar is placed (from column 10)

	     while ( contractAmt > 0 )
	     {
	         r.contract();
	         contractAmt--;
	     }

	     System.out.println("Debug 4: height(arm1)= " + h + " width (arm2) = "+
	                        w + " depth (arm3) =" + d); 


	     // You need to lower the third arm so that the block sits just above the bar
	     // For part (a) all bars are initially set to 7
	     // For Parts (b) and (c) you must extract this value from the array barHeights

	     int currentBar  = 0;             

	     // lowering third arm
	     while ( (h - 1) - d - blockHt > barHeights[currentBar] )   
	     {
	         r.lower();
	         d++;
	     }

	     System.out.println("Debug 5: height(arm1)= " + h + " width (arm2) = "+
	                        w + " depth (arm3) =" + d); 
	     
	     // dropping the block      
	     r.drop();

	     // The height of currentBar increases by block just placed    
	     barHeights[currentBar] += blockHt;

	     // raising the third arm all the way
	     while ( d > 0 )
	     {
	         r.raise();
	         d--;
	     }
	     System.out.println("Debug 6: height(arm1)= " + h + " width (arm2) = " +
	                        w + " depth (arm3) =" + d); 

	     // This just shows the message at the end of the sample robot run -
	     // you don't need to duplicate (or even use) this code in your program.

	     /*JOptionPane.showMessageDialog(null,
	                                   "You have moved one block from source " +
	                                   "to the first bar position.\n" + 
	                                   "Now you may modify this code or " +
	                                   "redesign the program and come up with " +
	                                   "your own method of controlling the robot.", 
	                                   "Helper Code Execution", 
	                                   JOptionPane.INFORMATION_MESSAGE);
	     // You have moved one block from source to the first bar position. 
	     // You should be able to get started now.
	     */
   }
   
   public void controlMechanismForScenarioA(int barHeights[], int blockHeights[])
   {

   }
   
   public void controlMechanismForScenarioB(int barHeights[], int blockHeights[])
   {
	   
   }
   
   public void controlMechanismForScenarioC(int barHeights[], int blockHeights[])
   {
	   
   }

} 


