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

    //Earlier ControlMechanism's may not work, as they reference methods shared with later methods that may have changed in implementation.
    public void controlMechanismBasicA(int barHeights[], int blockHeights[]) {
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int dropPos = height - drop;
        int stackHeight = 12; //how do I add this shit up

        height = moveVerticalTo(13, height);
        width = moveHorizontalTo(10, width);
        r.pick();
        stackHeight = stackHeight - 3;
        width = moveHorizontalTo(5, width);
        drop = moveCraneTo(2, drop);
        r.drop();
        width = moveHorizontalTo(10, width);
        drop = moveCraneTo(3, drop);
        r.pick();
        stackHeight = stackHeight - 3;
        drop = moveCraneTo(2, drop);
        width = moveHorizontalTo(6, width);
        r.drop();
        width = moveHorizontalTo(10, width);
        drop = moveCraneTo(6, drop);
        r.pick();
        stackHeight = stackHeight - 3;
        drop = moveCraneTo(2, drop);
        width = moveHorizontalTo(7, width);
        r.drop();
        width = moveHorizontalTo(10, width);
        drop = moveCraneTo(9, drop);
        r.pick();
        drop = moveCraneTo(2, drop);
        width = moveHorizontalTo(8, width);
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

        height = moveVerticalTo(13, height);

        while (NumberBlocks != 0) {
            width = moveHorizontalTo(10, width);
            drop = moveCraneTo((height - 1) - stackHeight, drop);
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
        while (BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];

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
        height = moveVerticalTo(13, height);
        while (NumberBlocks != 0) {
            //Figure out the height of the current block in the array
            int TopBlockNumber = blockHeights.length;
            int CurrentBlock = blockHeights[TopBlockNumber - 1];
            --TopBlockNumber;
            //Move to to stack
            width = moveHorizontalTo(10, width);
            //Drop to the top position of the stack
            drop = moveCraneToPosition(StackHeight + 1, height, drop);
            r.pick();
            //move to a spot where we can safely change our vertical height- legacy code from Part A and not necessary - a soltution that does not require the horizontal move could be devised, but in number of moves is equivalent, and this is cleaner to see, but it works
            width = moveHorizontalTo(9, width);
            drop = moveCraneTo(2, drop);
            StackHeight = StackHeight - 3;
            width = moveHorizontalTo(ThreesDropBarNumber, width);
            int currentBarHeight = barHeights[ThreesDropBarNumber - 3];
            drop = moveCraneToPosition(currentBarHeight + 1 + CurrentBlock, height, drop);
            r.drop();
            if (currentBarHeight + 3 > MaxHeight) {
                MaxHeight = currentBarHeight + 3;
            }
            if (MaxHeight >= StackHeight + 1) {
                drop = moveCraneToPosition(MaxHeight + 1, height, drop);
            } else if (StackHeight + 1 > MaxHeight) {
                drop = moveCraneToPosition(StackHeight + 1, height, drop);
            }
            ThreesDropBarNumber++;
            NumberBlocks--;
        }
    }

    public void controlMechanismOptimisedB(int barHeights[], int blockHeights[]) {
        //todo -  once i know how to use for loops, get rid of some of these horrible while loops
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = blockHeights.length; //How many blocks need to be moved.
        int NoThreeBlocks = 0; // Count the number of blocks that need to be put on bars
        byte ThreesLoop = 0;
        while (ThreesLoop != blockHeights.length) {
            if (blockHeights[ThreesLoop] == 3) {
                NoThreeBlocks++;
            }
            ThreesLoop++;
        }
        int[] BarOptimised = optimisePathing(barHeights); //todo add usage of this functionality
        //setting stack height total for the end.
        int StackHeight = 0;
        int BlockRuns = blockHeights.length;
        while (BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];
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
        int TopBlockNumber = blockHeights.length;
        height = moveVerticalTo(13, height);
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
            int MaxMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo);
            drop = moveCraneToPosition(MaxMoveHeight + CurrentBlock /* To take bar height + crane height into account */, height, drop);
            width = moveHorizontalTo(MoveTo, width);
            drop = moveCraneToPosition(currentBarHeight + CurrentBlock, height, drop);
            r.drop();
            if (StackHeight + 1 > MaxMoveHeight + 3) {
                drop = moveCraneToPosition(StackHeight, height, drop);
            } else drop = moveCraneToPosition(MaxMoveHeight + 3, height, drop); //replaces some other logic
            if (CurrentBlock == 3) {
                BarThreesPosition++;
                barHeights[BarThreesPosition] = barHeights[BarThreesPosition] + CurrentBlock;
            } else if (CurrentBlock == 1) {
                BarOneHeight++;
            } else {
                BarTwoHeight = BarTwoHeight + 2;
            }
            NumberBlocks--;
        }
    }

    public void controlMechanismC(int barHeights[], int blockHeights[]) {
        //todo -  once i know how to use for loops, get rid of some of these horrible while loops
        //todo- pathing properly. Don't assume all bars have a size 3 block on top.
        //todo- Path around bars. ensure that bars will not block the path of other blocks and take that into account when choosing optimal bar to place on.
        //todo- Add actual comments for newer pathing stuff.
        int height = 2;         // Initial height of arm 1
        int width = 1;         // Initial width of arm 2
        int drop = 0;         // Initial depth of arm 3
        int NumberBlocks = blockHeights.length; //How many blocks need to be moved.
        int NoThreeBlocks = 0; // Count the number of blocks that need to be put on bars
        byte ThreesLoop = 0;//todo-  change the number of referenced blocks in OptimisePathing to use Number of threes.
        while (ThreesLoop != blockHeights.length) {
            if (blockHeights[ThreesLoop] == 3) {
                NoThreeBlocks++;
            }
            ThreesLoop++;
        }
        int[] BarOptimised = null;
        if (NoThreeBlocks >= 1) {
            BarOptimised = optimisePathing(barHeights, NoThreeBlocks);
        }
        //setting stack height total for the end.
        int StackHeight = 0;
        int BlockRuns = blockHeights.length;
        while (BlockRuns != 0) {
            StackHeight += blockHeights[BlockRuns - 1];
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
        int TopBlockNumber = blockHeights.length;
        height = moveVerticalTo(13, height);
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
            int MaxMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo);
            drop = moveCraneToPosition(MaxMoveHeight + CurrentBlock /* To take bar height + crane height into account */, height, drop);
            width = moveHorizontalTo(MoveTo, width);
            drop = moveCraneToPosition(currentBarHeight + CurrentBlock, height, drop);
            r.drop();
            if (StackHeight + 1 > MaxMoveHeight + 3) {
                drop = moveCraneToPosition(StackHeight, height, drop);
            } else drop = moveCraneToPosition(MaxMoveHeight + 3, height, drop); //replaces some other logic
            if (CurrentBlock == 3) {
                barHeights[BarThreesPosition] = barHeights[BarThreesPosition] + CurrentBlock;
                BarThreesPosition++;
            } else if (CurrentBlock == 1) {
                BarOneHeight++;
            } else {
                BarTwoHeight = BarTwoHeight + 2;
            }
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

    public int[] optimisePathing(int barHeights[], int NumberOfThrees) { //literally broken, need more debugging
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
        while (BarRuns <= 5) {
            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
            //End result should be {4,5,6,0/2,2/0,1}
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

    public int checkMaxPathingHeightToBars(int barHeights[], int LeftBar) {
        int MaximumHeight = 0;//
        if (LeftBar < 3) {
            LeftBar = 3;
        }
        while (5 >= LeftBar - 3) {
            if (barHeights[LeftBar - 3] >= MaximumHeight) {
                MaximumHeight = barHeights[LeftBar - 3];
            }
            LeftBar++;
        }


        return (MaximumHeight);
    }

    public int[] optimisePathing(int barHeights[]) { //literally broken, need more debugging
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
        while (BarRuns <= 5) {
            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
            //End result should be {4,5,6,0/2,2/0,1}
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
        int CurrentRun = 3;
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

    } //Only here for the older Methods.

}


