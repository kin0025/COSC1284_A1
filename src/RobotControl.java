/*
 * Copyright (c) 2016.
 * kin0025 aka Alexander Kinross-Smith
 * s3603437@student.rmit.edu.au
 * alex@akinrosssmith.id.au
 */
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
            } else if (MoveTo > 14) { //If move to is greater than 14, set it to 14
                MoveTo = 14;
            } else {
                MoveTo = 0; //If it isn't greater than 14, it must be a negative number. Set to 0
            }
        }
	return (Position);
    }
	public int moveHorizontalTo(int MoveTo, int Position) {
        while (Position != MoveTo) {
            if (MoveTo < 11 && MoveTo > 0) { //More input sanitizing

                if (Position < MoveTo) { //Extend to move to move to
                    r.extend();
                    Position++;
                }
                if (Position > MoveTo) {
                    r.contract();
                    Position--;
                }
            } else if (MoveTo > 10) { //if greater than 10, set to 10
                MoveTo = 10;
            } else {
                MoveTo = 1; //if less than 1 (the minimum horizontal width), set to 1
            }
        }
        return (Position); //Return our position so that it stays up to date
    }

    public int moveCraneTo(int MoveTo, int Position) { //Semi Obsolete- the move crane to position replaces this, but this still has uses
        while (Position != MoveTo) { //Why am I still writing comments for stuff that is the same as before?
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
        int Position = Height - Drop; //Slightly more complex than before. - We get the position of the crane end using the height of the tower and subtracting the drop
        while (Position != MoveTo) {
            Position = Height - Drop;
            if (MoveTo < 15 && MoveTo > -1) { //Is the function less than -technically couild have used >= or <=, but this works. Again making sure we don't move outside acceptable bounds.

                if (Position < MoveTo) {
                    r.raise();
                    Drop--; //Still change the drop value, not the position one, as it is updated every run, and we need to pass the drop value back at the end.
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

    public int[] optimisePathing(int barHeights[]) { //literally broken, need more debugging
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


} 


