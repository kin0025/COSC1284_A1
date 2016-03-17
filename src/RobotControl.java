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
        r.speedUp(10);//For testing large numbers of iterations.
        controlMechanismOptimisedC(barHeights, blockHeights);

    }

    public void controlMechanismOptimisedC(int barHeights[], int blockHeights[]) {
        //Setting cranes initial position as variables
        int[] position;
        position = new int[3];
        position[0] = 2;         // Initial height of arm 1
        position[1] = 1;         // Initial width of arm 2
        position[2] = 0;         // Initial drop of arm 3
        int numberOfBlocks = blockHeights.length; //How many blocks need to be moved.
        int maxHeight = 0;
        int stackHeight = 0;
        int maxBlockSize = 0;
        int firstTwo = 100;
        int firstOne = 100;
        int firstThree = 100;
        int numberOfThreeBlocks = 0;
        int maxStackedHeight = 0;
        int topBlockNumber = blockHeights.length;
        int BarOneHeight = 0; //Initialising outside of loop as we don't want these set to zero again.
        int BarTwoHeight = 0;
        int BarThreesPosition = 0; //This is used to keep track of what bar we are working on - refers to array index of barOptimised
        // Count the number of blocks that need to be put on bars
        for (int blockRuns = blockHeights.length; blockRuns > 0; blockRuns--) {//A loop that runs code for analysing the blocks. Run the loop for as many times as there are blocks.
            stackHeight += blockHeights[blockRuns - 1];// Add the current block onto the running total
            if (blockHeights[blockRuns - 1] == 2 && firstTwo == 100) { //If current block is equal to 2 and firstTwo has not been changed since declaration, the current block must be the fist one that is two high.
                firstTwo = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 1 && firstOne == 100) {//Same as above, but for blocks one high.
                firstOne = blockRuns - 1;
            }
            if (maxBlockSize < blockHeights[blockRuns - 1]) { //If current block is larger than all previous blocks, set the largest block to the current one.
                maxBlockSize = blockHeights[blockRuns - 1];
            }
            if (blockHeights[blockRuns - 1] == 3 && firstThree == 100) {//Same as above, but for blocks one high.
                firstThree = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 3) { //If the current block been examined is 3 high, increment the counter.
                numberOfThreeBlocks++;
            }
        }// End blocks loop

        //Start Bars loop
        for (int barRuns = 6; barRuns != 0; barRuns--) {
            if (maxHeight < barHeights[barRuns - 1]) { //Is the bar been examined larger than all bars previously?
                maxHeight = barHeights[barRuns - 1];// If it is, it is now that largest bar.
            }
        }

        //Initialise a variable
        int[] barOptimised;
        barOptimised = new int[7];

        if (numberOfThreeBlocks >= 1) { //We only need to optimise for Bars if there are blocks to be placed on them.
            barOptimised = optimisePathing(barHeights, numberOfThreeBlocks, maxHeight, blockHeights); //Find more efficient ways of placing blocks on bars, and return an array that contains them in ascending order (Generalisation for most efficient)
        }
        /*barOptimised[0]= 2;
        barOptimised[1]= 4;*/
        //Finding the maximum height that the blocks will reach when placed on bars
        //Height of all one and two high blocks is not needed, as stackHeight will always be larger.
        if (maxBlockSize == 3) {
            int x = numberOfThreeBlocks;
            while (x != 0) { //Setting the maximum height blocks will take up.
                if (maxStackedHeight < barHeights[barOptimised[x - 1]] + 3) {
                    maxStackedHeight = barHeights[barOptimised[x - 1]] + 3;
                }
                x--;
            }//End while
        }//End if
        if (maxHeight < maxStackedHeight) { //Finding maximum expected height.
            maxHeight = maxStackedHeight;
        }//End if
        if (maxHeight >= stackHeight + 1) { //Move to the highest expected point.
            position[0] = moveVerticalTo(maxHeight + 1, position[0]);
        } else position[0] = moveVerticalTo(stackHeight + 1, position[0]);

        //Initialise and declare for the movement loop
        while (numberOfBlocks != 0) { //Doing the actual movement
            int CurrentBlock = blockHeights[topBlockNumber - 1];
            topBlockNumber--;
            //Move to the stack
            position[1] = moveHorizontalTo(10, position[1]);
            //Drop to the top position of the stack
            position = moveCraneToPosition(stackHeight, position);
            r.pick();
            position[1] = moveHorizontalTo(9, position[1]);
            stackHeight = stackHeight - CurrentBlock;
            int MoveTo;
            int currentBarHeight;
            if (CurrentBlock == 1) {
                MoveTo = 1;
                currentBarHeight = BarOneHeight;
            } else if (CurrentBlock == 2) {
                MoveTo = 2;
                currentBarHeight = BarTwoHeight;
            } else {
                MoveTo = barOptimised[BarThreesPosition] + 3;
                currentBarHeight = barHeights[barOptimised[BarThreesPosition]];
            }
            int MinMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, BarOneHeight, BarTwoHeight);
            position = moveCraneToPosition(MinMoveHeight + CurrentBlock /* To take bar height + crane height into account */, position);
            position[1] = moveHorizontalTo(MoveTo, position[1]);
            position = moveCraneToPosition(currentBarHeight + CurrentBlock, position);
            r.drop();
            if (CurrentBlock == 3) {
                barHeights[barOptimised[BarThreesPosition]] = barHeights[barOptimised[BarThreesPosition]] + 3;
                BarThreesPosition++;
            } else if (CurrentBlock == 1) {
                BarOneHeight++;
            } else {
                BarTwoHeight = BarTwoHeight + 2;
            }
            numberOfBlocks--;
            if (numberOfBlocks > 0) {
                MinMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, BarOneHeight, BarTwoHeight);
                if (stackHeight + 1 > MinMoveHeight) {
                    position = moveCraneToPosition(stackHeight, position);
                } else
                    position = moveCraneToPosition(MinMoveHeight, position); //replaces some other logic
            }
        }
    }

    //A listing of movement methods.

    public int moveVerticalTo(int moveTo, int position) { //We get the position to move to and the current position passed to us
        while (position != moveTo) { //when we have not moved to the correct position proceed
            if (moveTo < 15 && moveTo > 0) { //Sanity checking to prevent moving to outside boundaries

                if (position < moveTo) { //Move up, increment position
                    r.up();
                    position++;
                }
                if (position > moveTo) { //move down, decrement position
                    r.down();
                    position--;
                }
            } else if (moveTo > 14) { //If move to is greater than 14, set it to 14
                moveTo = 14;
            } else {
                moveTo = 0; //If it isn't greater than 14, it must be a negative number. Set to 0
            }
        }
        return (position);
    }

    public int moveHorizontalTo(int moveTo, int position) {
        while (position != moveTo) {
            if (moveTo < 11 && moveTo > 0) { //More input sanitizing

                if (position < moveTo) { //Extend to move to move to
                    r.extend();
                    position++;
                }
                if (position > moveTo) {
                    r.contract();
                    position--;
                }
            } else if (moveTo > 10) { //if greater than 10, set to 10
                moveTo = 10;
            } else {
                moveTo = 1; //if less than 1 (the minimum horizontal width), set to 1
            }
        }
        return (position); //Return our position so that it stays up to date
    }

    public int[] moveCraneToPosition(int moveTo, int[] position) {
        moveTo++;
        int verticalPosition = position[0] - position[2]; //Slightly more complex than before. - We get the position of the crane end using the height of the tower and subtracting the drop
        while (verticalPosition != moveTo) {
            verticalPosition = position[0] - position[2];
            if (moveTo > position[0]) {//If we are trying to move too high, increment the height.
                r.up();
                position[0]++;
            } else if (moveTo < 15 && moveTo > -1) { //Is the function less than -technically couild have used >= or <=, but this works. Again making sure we don't move outside acceptable bounds.

                if (verticalPosition < moveTo) {
                    r.raise();
                    position[2]--; //Still change the drop value, not the position one, as it is updated every run, and we need to pass the drop value back at the end.
                }
                if (verticalPosition > moveTo) {
                    r.lower();
                    position[2]++;
                }

            } else if (moveTo > 14) { //If trying to move too high set to acceptable height
                moveTo = 14;
            } else {
                moveTo = 0;
            }
        }
        return (position);//Return drop. height may change whilst function runs. - See checks during movement loop.
    }

    public int[] optimisePathing(int barHeights[], int numberOfThrees, int MaxHeight, int blockHeights[]) {
        int[] barNumbers = {0, 0, 0, 0, 0, 0, 0};
        int[] optimisationBars = {0, 0, 0, 0, 0, 0, 0};
        int[] optimisation = {21, 22, 23, 24, 25, 26, 27};
        for (int barRuns = 0; barRuns <= 4; barRuns++) {//Set optimisation values for movements.
            optimisationBars[barRuns] = (7 - barHeights[barRuns]) + (6 - barRuns); //This should give {9,11,9,7,3,7}
        }
        int firstTwo = 100;
        int firstOne = 100;
        int firstThree = 100;
        int lastThreeHeight = 0;
        int lastThree = 0;
        for (int blockRuns = blockHeights.length; blockRuns != 0; blockRuns--) {
            if (blockHeights[blockRuns - 1] == 2 && firstTwo == 100) { //If current block is equal to 2 and firstTwo has not been changed since declaration, the current block must be the fist one that is two high.
                firstTwo = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 1 && firstOne == 100) {//Same as above, but for blocks one high.
                firstOne = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 3 && firstThree == 100) {//Same as above, but for blocks one high.
                firstThree = blockRuns - 1;
            }
        }// End blocks decrement loop
        if (firstOne == 100) {
            firstOne = 0;
        }
        if (firstTwo == 100) {
            firstTwo = 0;
        }
        if (firstThree == 100) {
            firstThree = 0;
        }
        for (int blockRuns = 0; blockRuns != blockHeights.length; blockRuns++) {
            if (lastThree == 0) {
                lastThreeHeight += blockHeights[blockRuns];
            }
            if (blockHeights[blockRuns] == 3 && lastThree == 0) {
                lastThree = blockRuns;
            }
        }
        if (lastThreeHeight < barHeights[5]) {
            optimisationBars[5] = 1; //Not a perfect solution. If our only 3 bar is high up, this is not the most efficient solution, however in many cases it is, as it is called last.check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
        } else
            optimisationBars[5] = (lastThreeHeight - barHeights[5]) + 1; //As stack is depleted by the time last 3 is reached, use a reduced move height for calculations

        boolean threesBefore = false;
        if (firstThree > firstOne || firstThree > firstTwo) {
            threesBefore = true;
        }

        int[] numbersFinal = optimisationOrder(optimisationBars, optimisation, barNumbers, numberOfThrees); //We get our old output - an ordered array of what bars to place on.
        //We need to take that list, re-run our optimisation bars algorithm to get the ordered result.\
        for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for movements.
            optimisationBars[barRuns] = (7 - barHeights[numbersFinal[barRuns]]) + (6 - numbersFinal[barRuns]); //This should give {9,11,9,7,3,7}
        }
        for (int barRuns = 0; barRuns <= 5; barRuns++) { //Find the part of numbers final that equals 5, and rerun the equation from before.
            if (numbersFinal[barRuns] == 5) {
                if (lastThreeHeight < barHeights[5]) {
                    optimisationBars[barRuns] = 1; //Not a perfect solution. If our only 3 bar is high up, this is not the most efficient solution, however in many cases it is, as it is called last.check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
                } else
                    optimisationBars[barRuns] = (lastThreeHeight - numbersFinal[barRuns]) + 1; //As stack is depleted by the time last 3 is reached, use a reduced move height for calculations
            }
        }
        //On that result we need to take the array index of each of them, and use it as the height to pass to not threes stack counter.

        //Then multiply the amount that the stacked bar will protrude over the previous maximum height by the not threes counter.

        //Iterate over this 3 times to get a rough approximation of bar ordering and any affect modified optimisation values from adding max height on will bring.

        //Return the final ordered array.

        // TODO: 17/03/2016 multiply the added value by the number of blocks that will pass over it.
        //todo add max height pathing costs - add more efficient pathing of 3 high blocks - Blocks need to take into account the number of blocks that will be passed over them after they have been placed.
        //todo-  optimisation is needed for positioning of blocks that are three high. Proposed solution is to calculate the number of blocks after each block that is picked that are less than 3. Use this as a multiplier for the max blocks value.
        //todo-  Take bar height into account when adding pathing height stuff.
        if (threesBefore) {//This is an imperfect solution.- We really need to examine each bar in an ordered fashion to find the number of blocks, reorder and re-examine. However this would be complex and computationally expensive, when this close approximation will suffice.
            for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for height added above max.
                int optimisationAdd;
                optimisationAdd = (barHeights[barRuns] + 3) - MaxHeight;
                if (optimisationAdd < 0) {
                    optimisationAdd = 0;
                }
                optimisationAdd = optimisationAdd * notThreesStackCounter(blockHeights, numbersFinal[barRuns]);
                optimisationBars[barRuns] = optimisationBars[barRuns] + optimisationAdd; //This should give {9,11,9,7,3,7}
            }

        }

        numbersFinal = optimisationOrder(optimisationBars, optimisation, barNumbers, numberOfThrees);
        return (numbersFinal);
    }

    public int[] optimisationOrder(int[] optimisationBars, int[] optimisation, int[] barNumbers, int numberOfThrees) {
        for (int barRuns = 0; barRuns <= 5; barRuns++) {
            byte r = 5;
            // Check the current number against all positions in optimisation.
            if (optimisationBars[barRuns] <= optimisation[0]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[0] = optimisationBars[barRuns]; // Fill the spot left after moving all the numbers down
                barNumbers[0] = barRuns; // Record the Bar associated with this optimisation value.
            } else if (optimisationBars[barRuns] <= optimisation[1]) {
                while (r >= 1) {
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[1] = optimisationBars[barRuns];
                barNumbers[1] = barRuns;
            } else if (optimisationBars[barRuns] <= optimisation[2]) {
                while (r >= 2) {
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[2] = optimisationBars[barRuns];
                barNumbers[2] = barRuns;
            } else if (optimisationBars[barRuns] <= optimisation[3]) {
                while (r >= 3) {
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[3] = optimisationBars[barRuns];
                barNumbers[3] = barRuns;
            } else if (optimisationBars[barRuns] <= optimisation[4]) {
                while (r >= 4) {
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[4] = optimisationBars[barRuns];
                barNumbers[4] = barNumbers[barRuns];
            } else if (optimisationBars[barRuns] <= optimisation[5]) {
                while (r >= 5) {
                    optimisation[r + 1] = optimisation[r];
                    barNumbers[r + 1] = barNumbers[r];
                    r--;
                }
                optimisation[5] = optimisationBars[barRuns];
                barNumbers[5] = barNumbers[barRuns];
            }
        }

        //For scenario 2 output should be 0,3,4,5,10
        int[] numbersFinal = {10, 10, 10, 10, 10};
        for (int currentRun = numberOfThrees - 1; currentRun != -1; currentRun--) {
            byte r = 3;
            if (barNumbers[currentRun] <= numbersFinal[0]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersFinal[r + 1] = numbersFinal[r];
                    r--;
                }
                numbersFinal[0] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
            } else if (barNumbers[currentRun] <= numbersFinal[1]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 1) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersFinal[r + 1] = numbersFinal[r];
                    r--;
                }
                numbersFinal[1] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
            } else if (barNumbers[currentRun] <= numbersFinal[2]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 2) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersFinal[r + 1] = numbersFinal[r];
                    r--;
                }
                numbersFinal[2] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
            } else if (barNumbers[currentRun] <= numbersFinal[3]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 3) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersFinal[r + 1] = numbersFinal[r];
                    r--;
                }
                numbersFinal[3] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
            }
        }
        return (numbersFinal);

    }

    public int checkMaxPathingHeightToBars(int barHeights[], int leftBar, int BarOneHeight, int BarTwoHeight) { //todo-  Add comments for this area.
        int maximumHeight = 0;
        while (5 >= leftBar - 3) {
            if (leftBar >= 3) {
                if (barHeights[leftBar - 3] >= maximumHeight) {
                    maximumHeight = barHeights[leftBar - 3];
                }
            } else if (leftBar == 1) {
                if (BarOneHeight >= maximumHeight) {
                    maximumHeight = BarOneHeight;
                }
            } else if (leftBar == 2) {
                if (BarTwoHeight >= maximumHeight) {
                    maximumHeight = BarTwoHeight;
                }
            }

            leftBar++;
        }


        return (maximumHeight);
    }

    public int notThreesStackCounter(int blockHeights[], int CurrentBlock) {
        int result = 0;
        for (int blockRuns = 0; blockRuns != (blockHeights.length - (CurrentBlock)); blockRuns++) {
            if (blockHeights[blockRuns] == 1 || blockHeights[blockRuns] == 2) {
                result++;
            }
        }
        return (result);
    }

}


