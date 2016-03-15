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

    public void control(int barHeights[], int blockHeights[]) {//sampleControlMechanism(barHeights,blockHeights);
//controlMechanismLoopedA(barHeights, blockHeights);
//controlMechanismLoopedB(barHeights, blockHeights);
//        controlMechanismOptimisedB(barHeights, blockHeights);
        controlMechanismC(barHeights, blockHeights);

    }

    public void controlMechanismC(int barHeights[], int blockHeights[]) {
        //todo -  once i know how to use for loops, get rid of some of these horrible while loops
        //todo- pathing properly. Add pathing support for blocks placed after initialisation.
        //todo- Path around bars. ensure that bars will not block the path of other blocks and take that into account when choosing optimal bar to place on.
        //todo- Add actual comments for newer pathing stuff.
        //todo- vary height of crane stem through estimated final column height.
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = blockHeights.length; //How many blocks need to be moved.
        int NumberOfThreeBlocks = 0; // Count the number of blocks that need to be put on bars
        byte ThreesLoop = 0;//todo-  change the number of referenced blocks in OptimisePathing to use Number of threes.
        while (ThreesLoop != blockHeights.length) {
            if (blockHeights[ThreesLoop] == 3) {
                NumberOfThreeBlocks++;
            }
            ThreesLoop++;
        }
        //setting stack height total for the end.
        int StackHeight = 0;
        int BlockRuns = blockHeights.length;
        int MaxBlockSize = 0;
        while (BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];
            if (MaxBlockSize < blockHeights[BlockRuns - 1]) {
                MaxBlockSize = blockHeights[BlockRuns - 1];
            }
            BlockRuns--;
        }
        int MaxHeight = 0;
        int BarRuns = 6;
        while (BarRuns != 0) {
            if (MaxHeight < barHeights[BarRuns - 1]) {
                MaxHeight = barHeights[BarRuns - 1];
            }
            BarRuns--;
        }
        int[] BarOptimised = null;
        if (NumberOfThreeBlocks >= 1) {
            BarOptimised = optimisePathing(barHeights, NumberOfThreeBlocks, MaxHeight);
        }
        int TopBlockNumber = blockHeights.length;
        if (MaxHeight + MaxBlockSize > StackHeight + 1) {
            height = moveVerticalTo(MaxHeight + MaxBlockSize, height);
        } else height = moveVerticalTo(StackHeight + 1, height);
        int BarOneHeight = 0; //Initialising outside of loop as we don't want these set to zero again.
        int BarTwoHeight = 0;
        int BarThreesPosition = 0;
        while (NumberBlocks != 0) { //Doing the actual movement
            int CurrentBlock = blockHeights[TopBlockNumber - 1];
            TopBlockNumber--;
            //Move to the stack
            width = moveHorizontalTo(10, width);
            //Drop to the top position of the stack
            drop = moveCraneToPosition(StackHeight, height, drop);
            r.pick();
            width = moveHorizontalTo(9, width);
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
            drop = moveCraneToPosition(MinMoveHeight + CurrentBlock /* To take bar height + crane height into account */, height, drop);
            width = moveHorizontalTo(MoveTo, width);
            drop = moveCraneToPosition(currentBarHeight + CurrentBlock, height, drop);
            r.drop();
            if (CurrentBlock == 3) {
                barHeights[BarOptimised[BarThreesPosition]] = barHeights[BarOptimised[BarThreesPosition]] + 3;
                BarThreesPosition++;
            } else if (CurrentBlock == 1) {
                BarOneHeight++;
            } else {
                BarTwoHeight = BarTwoHeight + 2;
            }
            NumberBlocks--;
            if (NumberBlocks > 0) {
                MinMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, BarOneHeight, BarTwoHeight);
                if (StackHeight + 1 > MinMoveHeight) {
                    drop = moveCraneToPosition(StackHeight, height, drop);
                } else drop = moveCraneToPosition(MinMoveHeight, height, drop); //replaces some other logic
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

    public int moveCraneToPosition(int MoveTo, int Height, int Drop) {
        MoveTo++;
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

    public int[] optimisePathing(int barHeights[], int NumberOfThrees, int MaxHeight) { //todo add max height pathing costs - add more efficient pathing of 3 high blocks - Blocks need to take into account the number of blocks that will be passed over them after they have been placed.
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
        while (BarRuns <= 5) {
            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
            BarRuns++;
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
        //convert the bar numbers array to coordinates
        int x = 0;
        while (x <= 5) {
//            BarNumbers[x] = BarNumbers[x]+3;
            System.out.print(BarNumbers[x] + ",");
            x++;

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

    public int checkMaxPathingHeightToBars(int barHeights[], int LeftBar, int BarOneHeight, int BarTwoHeight) {
        int MaximumHeight = 0;
        while (5 >= LeftBar - 3) {
            if (LeftBar >= 3) {
                if (barHeights[LeftBar - 3] >= MaximumHeight) {
                MaximumHeight = barHeights[LeftBar - 3];
                }
            } else if (LeftBar <= 2) {
                if (BarOneHeight >= MaximumHeight) {
                    MaximumHeight = BarOneHeight;
                }
                if (BarTwoHeight >= MaximumHeight) {
                    MaximumHeight = BarTwoHeight;
                }
            }
            LeftBar++;
        }


        return (MaximumHeight);
    }

}


