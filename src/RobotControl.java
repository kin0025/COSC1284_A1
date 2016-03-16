import jdk.nashorn.internal.ir.Block;

import java.util.Stack;

/*
 * Copyright (c) 2016.
 * kin0025 aka Alexander Kinross-Smith
 * s3603437@student.rmit.edu.au
 * alex@akinrosssmith.id.au
 */
class RobotControl {
    private Robot r;

    public RobotControl(Robot r) {
        this.r = r;
    }

    public void control(int barHeights[], int blockHeights[]) {
        controlMechanismOptimisedC(barHeights, blockHeights);

    }

    public void controlMechanismOptimisedC(int barHeights[], int blockHeights[]) {
        //todo-  Once I know how to use for loops, get rid of some of these horrible while loops
        //Setting cranes initial position as variables
        int[] Position;
        Position = new int[3];
        Position[0] = 2;         // Initial height of arm 1
        Position[1] = 1;         // Initial width of arm 2
        Position[2] = 0;         // Initial drop of arm 3
        int NumberOfBlocks = blockHeights.length; //How many blocks need to be moved.

        //Initialising some variables that are used throughout.
        int BlockRuns = blockHeights.length; // Run the loop for as many times as there are blocks.
        //Initialising and declaring variables -  to be incremented in a loop
        int StackHeight = 0;
        int MaxBlockSize = 0;
        int FirstTwo = 100;
        int FirstOne = 100;
        int FirstThree = 100;
        int NumberOfThreeBlocks = 0;         // Count the number of blocks that need to be put on bars
        while (BlockRuns != 0) {//A loop that runs code for analysing the blocks
            StackHeight += blockHeights[BlockRuns - 1];// Add the current block onto the running total
            if (blockHeights[BlockRuns - 1] == 2 && FirstTwo == 100) { //If current block is equal to 2 and FirstTwo has not been changed since declaration, the current block must be the fist one that is two high.
                FirstTwo = BlockRuns - 1;
            }
            if (blockHeights[BlockRuns - 1] == 1 && FirstOne == 100) {//Same as above, but for blocks one high.
                FirstOne = BlockRuns - 1;
            }
            if (MaxBlockSize < blockHeights[BlockRuns - 1]) { //If current block is larger than all previous blocks, set the largest block to the current one.
                MaxBlockSize = blockHeights[BlockRuns - 1];
            }
            if (blockHeights[BlockRuns - 1] == 3 && FirstThree == 100) {//Same as above, but for blocks one high.
                FirstThree = BlockRuns - 1;
            }
            if (blockHeights[BlockRuns - 1] == 3) { //If the current block been examined is 3 high, increment the counter.
                NumberOfThreeBlocks++;
            }
            BlockRuns--;
        }// End blocks loop

        //Start Bars loop
        //Declare and initialise for the loop
        int MaxHeight = 0;
        int BarRuns = 6;
        while (BarRuns != 0) {
            if (MaxHeight < barHeights[BarRuns - 1]) { //Is the bar been examined larger than all bars previously?
                MaxHeight = barHeights[BarRuns - 1];// If it is, it is now that largest bar.
            }
            BarRuns--;
        }

        //Initialise a variable
        int[] BarOptimised;
        BarOptimised = new int[7];

        if (NumberOfThreeBlocks >= 1) { //We only need to optimise for Bars if there are blocks to be placed on them.
            BarOptimised = optimisePathing(barHeights, NumberOfThreeBlocks, MaxHeight, blockHeights); //Find more efficient ways of placing blocks on bars, and return an array that contains them in ascending order (Generalisation for most efficient)
        }

        //Finding the maximum height that the blocks will reach when placed on bars
        int MaxStackedHeight = 0;//Height of all one and two high blocks is not needed, as StackHeight will always be larger.
        if (MaxBlockSize == 3) {
            int x = NumberOfThreeBlocks;
            while (x != 0) { //Setting the maximum height blocks will take up.
                if (MaxStackedHeight < barHeights[BarOptimised[x - 1]] + 3) {
                    MaxStackedHeight = barHeights[BarOptimised[x - 1]] + 3;
                }
                x--;
            }//End while
        }

        if (MaxStackedHeight >= StackHeight + 1) { //Move to the highest expected point.
            Position[0] = moveVerticalTo(MaxStackedHeight + 1, Position[0]);
        } else Position[0] = moveVerticalTo(StackHeight + 1, Position[0]);

//      height = moveVerticalTo(13, height); //inefficient, but works for all solutions.

        //Initialise and declare for the movement loop
        int TopBlockNumber = blockHeights.length;
        int BarOneHeight = 0; //Initialising outside of loop as we don't want these set to zero again.
        int BarTwoHeight = 0;
        int BarThreesPosition = 0; //This is used to keep track of what bar we are working on - refers to array index of BarOptimised
        while (NumberOfBlocks != 0) { //Doing the actual movement
            int CurrentBlock = blockHeights[TopBlockNumber - 1];
            TopBlockNumber--;
            //Move to the stack
            Position[1] = moveHorizontalTo(10, Position[1]);
            //Drop to the top position of the stack
            Position = moveCraneToPosition(StackHeight, Position);
            r.pick();
            Position[1] = moveHorizontalTo(9, Position[1]);
            StackHeight = StackHeight - CurrentBlock;
            int MoveTo;
            int currentBarHeight;
            if (CurrentBlock == 1) {
                MoveTo = 1;
                currentBarHeight = BarOneHeight;
            } else if (CurrentBlock == 2) {
                MoveTo = 2;
                currentBarHeight = BarTwoHeight;
            } else {
                MoveTo = BarOptimised[BarThreesPosition] + 3;
                currentBarHeight = barHeights[BarOptimised[BarThreesPosition]];
            }
            int MinMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, BarOneHeight, BarTwoHeight);
            Position = moveCraneToPosition(MinMoveHeight + CurrentBlock /* To take bar height + crane height into account */, Position);
            Position[1] = moveHorizontalTo(MoveTo, Position[1]);
            Position = moveCraneToPosition(currentBarHeight + CurrentBlock, Position);
            r.drop();
            if (CurrentBlock == 3) {
                barHeights[BarOptimised[BarThreesPosition]] = barHeights[BarOptimised[BarThreesPosition]] + 3;
                BarThreesPosition++;
            } else if (CurrentBlock == 1) {
                BarOneHeight++;
            } else {
                BarTwoHeight = BarTwoHeight + 2;
            }
            NumberOfBlocks--;
            if (NumberOfBlocks > 0) {
                MinMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, BarOneHeight, BarTwoHeight);
                if (StackHeight + 1 > MinMoveHeight) {
                    Position = moveCraneToPosition(StackHeight, Position);
                } else
                    Position = moveCraneToPosition(MinMoveHeight, Position); //replaces some other logic
            }
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

    public int[] moveCraneToPosition(int MoveTo, int[] Position) {
        MoveTo++;
        int VerticalPosition = Position[0] - Position[2]; //Slightly more complex than before. - We get the position of the crane end using the height of the tower and subtracting the drop
        while (VerticalPosition != MoveTo) {
            VerticalPosition = Position[0] - Position[2];
            if (MoveTo > Position[0]) {//If we are trying to move too high, increment the height. This is dangerous, as we don't pass the height back. We need to check for a negative drop at the other end.
                r.up();
                Position[0]++;
            }
            if (MoveTo < 15 && MoveTo > -1) { //Is the function less than -technically couild have used >= or <=, but this works. Again making sure we don't move outside acceptable bounds.

                if (VerticalPosition < MoveTo) {
                    r.raise();
                    Position[2]--; //Still change the drop value, not the position one, as it is updated every run, and we need to pass the drop value back at the end.
                }
                if (VerticalPosition > MoveTo) {
                    r.lower();
                    Position[2]++;
                }

            } else if (MoveTo > 14) { //If trying to move too high set to acceptable height
                MoveTo = 14;
            } else {
                MoveTo = 0;
            }
        }
        return (Position);//Return drop. height may change whilst function runs. - See checks during movement loop.
    }

    public int[] optimisePathing(int barHeights[], int NumberOfThrees, int MaxHeight, int blockHeights[]) {
        //todo add max height pathing costs - add more efficient pathing of 3 high blocks - Blocks need to take into account the number of blocks that will be passed over them after they have been placed.
        //todo-  Optimisation is needed for positioning of blocks that are three high. Proposed solution is to calculate the number of blocks after each block that is picked that are less than 3. Use this as a multiplier for the max blocks value.
        //todo-  Take bar height into account when adding pathing height stuff.
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
        while (BarRuns <= 4) {//Set Optimisation values for movements.
            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
            BarRuns++;
        }
        int BlockRuns = blockHeights.length;
        int FirstTwo = 100;
        int FirstOne = 100;
        int FirstThree = 100;
        int LastThreeHeight = 0;
        int LastThree = 0;
        while (BlockRuns != 0) {//A loop that runs code for analysing the blocks
            if (blockHeights[BlockRuns - 1] == 2 && FirstTwo == 100) { //If current block is equal to 2 and FirstTwo has not been changed since declaration, the current block must be the fist one that is two high.
                FirstTwo = BlockRuns - 1;
            }
            if (blockHeights[BlockRuns - 1] == 1 && FirstOne == 100) {//Same as above, but for blocks one high.
                FirstOne = BlockRuns - 1;
            }
            if (blockHeights[BlockRuns - 1] == 3 && FirstThree == 100) {//Same as above, but for blocks one high.
                FirstThree = BlockRuns - 1;
            }
            BlockRuns--;
        }// End blocks decrement loop
        BlockRuns = 0;
        while (BlockRuns != blockHeights.length) {
            if (LastThree == 0) {
                LastThreeHeight += blockHeights[BlockRuns];
            }
            if (blockHeights[BlockRuns] == 3 && LastThree == 0) {
                LastThree = BlockRuns;

            }
            BlockRuns++;
        }
        if (LastThreeHeight < barHeights[5]) {
            OptimisationBars[5] = 1; //Not a perfect solution. If our only 3 bar is high up, this is not the most efficient solution, however in many cases it is, as it is called last.check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
        } else
            OptimisationBars[5] = (LastThreeHeight - barHeights[5]) + 1; //As stack is depleted by the time last 3 is reached, use a reduced move height for calculations


        boolean ThreesBefore = false;
        int FirstNonThree = 0;
        if (FirstThree > FirstOne || FirstThree > FirstTwo) {
            ThreesBefore = true;
            if (FirstOne >= FirstTwo) {
                FirstNonThree = FirstOne;
            } else FirstNonThree = FirstTwo;
        }

//todo- multiply the Added value by the number of blocks that will pass over it
        if (ThreesBefore) {
            BarRuns = 0;
            while (BarRuns <= 5) {//Set Optimisation values for height added above max. //todo only run this if there are blocks less than 3 high below blocks that are 3 high.
                int OptimisationAdd;
                OptimisationAdd = (barHeights[BarRuns] + 3) - MaxHeight;
                if (OptimisationAdd < 0) {
                    OptimisationAdd = 0;
                }
                OptimisationBars[BarRuns] = OptimisationBars[BarRuns] + OptimisationAdd; //This should give {9,11,9,7,3,7}
                BarRuns++;
            }
        }
        BarRuns = 0;
        while (BarRuns <= 5) {
            byte r = 5;
            // Check the current number against all positions in optimisation.
            if (OptimisationBars[BarRuns] <= Optimisation[0]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[0] = OptimisationBars[BarRuns]; // Fill the spot left after moving all the numbers down
                BarNumbers[0] = BarRuns; // Record the Bar associated with this optimisation value.
            } else if (OptimisationBars[BarRuns] <= Optimisation[1]) {
                while (r >= 1) {
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[1] = OptimisationBars[BarRuns];
                BarNumbers[1] = BarRuns;
            } else if (OptimisationBars[BarRuns] <= Optimisation[2]) {
                while (r >= 2) {
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[2] = OptimisationBars[BarRuns];
                BarNumbers[2] = BarRuns;
            } else if (OptimisationBars[BarRuns] <= Optimisation[3]) {
                while (r >= 3) {
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[3] = OptimisationBars[BarRuns];
                BarNumbers[3] = BarRuns;
            } else if (OptimisationBars[BarRuns] <= Optimisation[4]) {
                while (r >= 4) {
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[4] = OptimisationBars[BarRuns];
                BarNumbers[4] = BarNumbers[BarRuns];
            } else if (OptimisationBars[BarRuns] <= Optimisation[5]) {
                while (r >= 5) {
                    Optimisation[r + 1] = Optimisation[r];
                    BarNumbers[r + 1] = BarNumbers[r];
                    r--;
                }
                Optimisation[5] = OptimisationBars[BarRuns];
                BarNumbers[5] = BarNumbers[BarRuns];
            }
            BarRuns++;
        }

        //For scenario 2 output should be 0,3,4,5,10
        int[] NumbersFinal = {10, 10, 10, 10, 10};
        int CurrentRun = NumberOfThrees - 1;
        while (CurrentRun != -1) {
            byte r = 3;
            if (BarNumbers[CurrentRun] <= NumbersFinal[0]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                    NumbersFinal[r + 1] = NumbersFinal[r];
                    r--;
                }
                NumbersFinal[0] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
            } else if (BarNumbers[CurrentRun] <= NumbersFinal[1]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 1) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                    NumbersFinal[r + 1] = NumbersFinal[r];
                    r--;
                }
                NumbersFinal[1] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
            } else if (BarNumbers[CurrentRun] <= NumbersFinal[2]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 2) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                    NumbersFinal[r + 1] = NumbersFinal[r];
                    r--;
                }
                NumbersFinal[2] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
            } else if (BarNumbers[CurrentRun] <= NumbersFinal[3]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 3) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                    NumbersFinal[r + 1] = NumbersFinal[r];
                    r--;
                }
                NumbersFinal[3] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
            }
            CurrentRun--;
        }
        return (NumbersFinal);

    }

    public int checkMaxPathingHeightToBars(int barHeights[], int LeftBar, int BarOneHeight, int BarTwoHeight) { //todo-  Add comments for this area.
        int MaximumHeight = 0;
        while (5 >= LeftBar - 3) {
            if (LeftBar >= 3) {
                if (barHeights[LeftBar - 3] >= MaximumHeight) {
                    MaximumHeight = barHeights[LeftBar - 3];
                }
            } else if (LeftBar == 1) {
                if (BarOneHeight >= MaximumHeight) {
                    MaximumHeight = BarOneHeight;
                }
            } else if (LeftBar == 2) {
                if (BarTwoHeight >= MaximumHeight) {
                    MaximumHeight = BarTwoHeight;
                }
            }

            LeftBar++;
        }


        return (MaximumHeight);
    }

    public int notThreesStackCounter(int barHeights[], int CurrentBlock) {
        int BarRuns = barHeights.length - (CurrentBlock + 1);
        int Result = 0;
        while (BarRuns > 0) {
            if (barHeights[BarRuns] == 1 || barHeights[BarRuns] == 2) {
                Result++;
            }

        }
        return (Result);
    }

}


