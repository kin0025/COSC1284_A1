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

    //A level of 0 means no bar height optimisation is not run.
    //A level of 1 means imperfect, but nonetheless effective optimisation is run
    //A level of 2 means perfect optimisation runs. NOT IMPLEMENTED. INCREDIBLY COMPUTATIONALLY EXPENSIVE
    final int OPTIMISATION_LEVEL = 1;

    public void control(int barHeights[], int blockHeights[]) {
        r.speedUp(20);//For testing large numbers of iterations.
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
            switch (CurrentBlock) {
                case 1:
                MoveTo = 1;
                currentBarHeight = BarOneHeight;
                    break;
                case 2:
                MoveTo = 2;
                currentBarHeight = BarTwoHeight;
                    break;

                case 3:
                MoveTo = barOptimised[BarThreesPosition] + 3;
                currentBarHeight = barHeights[barOptimised[BarThreesPosition]];
                    break;
                default:
                    MoveTo = barOptimised[BarThreesPosition] + 3;
                    currentBarHeight = barHeights[barOptimised[BarThreesPosition]];
                    break;

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
        int firstTwo = 100;
        int firstOne = 100;
        int firstThree = 100;
        int lastThreeHeight = 0;
        int lastThree = 0;
        for (int barRuns = 0; barRuns <= 4; barRuns++) {//Set optimisation values for movements.
            optimisationBars[barRuns] = (7 - barHeights[barRuns]) + (6 - barRuns); //This should give {9,11,9,7,3,7}
        }
        if (lastThreeHeight < barHeights[5]) {
            optimisationBars[5] = 1; //Not a perfect solution. If our only 3 bar is high up, this is not the most efficient solution, however in many cases it is, as it is called last.check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
        } else
            optimisationBars[5] = (lastThreeHeight - barHeights[5]) + 1; //As stack is depleted by the time last 3 is reached, use a reduced move height for calculations
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
        boolean threesBefore = false;
        if (firstThree > firstOne || firstThree > firstTwo) {
            threesBefore = true;
        }

        //For run one should be {2,3,4,5} for run 7 should be {2,4}
//        We should end with optimisation values of {7,5,5,3,3,2} without the add bars
        //      Once add bars (Number of blocks below 3 * number of units above max height) we can expect (add 4 to bars that are 6 high, add 6 to bars that are 7 high)
        //     {11,11,9,9,7,7}
        //   This is not the solution we are looking for. We want something for 8th bar larger than 5th bar.
//So, what do we do?
        //We need a new algorithm.
        //If we create an array of the number of the optimisation value for all potential positions (maxing out at the number of threes) we can potentially get our result.
        //i.e for bar 3 we get {11,9}, bar 4 {11,8} bar 5 {9,7} bar 6 {9,6} bar 7 {7,5} bar 8 {7,4}. This is quite the conundrum.
        //I think that to accurately find the shortest path with the least moves, there is no shortcut. We need to generate optimisation values for every possible configuration (treating configurations as a single entity), and find the smallest one. This will be incredibly computationally expensive ( think ^3 +), so a toggle may need to be implemented.

//Run through the entire blocks array to get the positions of the three high bars.

//This is an imperfect solution, but is adequate.
        if (OPTIMISATION_LEVEL == 1) {
            int[] numbersPrelim;
            //Rerun the optimisation values on our bars again.
            for (int barRun = blockHeights.length; barRun >= 0; barRun--) {    //Iterate over this 3 times to get a rough approximation of bar ordering and any affect modified optimisation values from adding max height on will bring.
                numbersPrelim = optimisationOrder(optimisationBars, optimisation, barNumbers, numberOfThrees, true); //We get our old output - an ordered array of what bars to place on.
                //We need to take that list, re-run our optimisation bars algorithm to get the ordered result.
                for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for movements.
                    optimisationBars[barRuns] = (7 - barHeights[numbersPrelim[barRuns]]) + (6 - numbersPrelim[barRuns]);
                }
                for (int barRuns = 0; barRuns <= 5; barRuns++) { //Find the part of numbers final that equals 5, and rerun the equation from before.
                    if (numbersPrelim[barRuns] == 5) {
                        if (lastThreeHeight < barHeights[5]) { //As this bar won't have to go up and over something, and then back down, we are fine as long as the final bar that is three high is also lower than the bar. If it is higher, we need to take number of moves down into account, and it may be regarded as lower.
                            optimisationBars[barRuns] = 1; //Not a perfect solution. If our only 3 bar is high up, this is not the most efficient solution, however in many cases it is, as it is called last. Check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
                        } else
                            optimisationBars[barRuns] = (lastThreeHeight - numbersPrelim[barRuns]) + 1; //As stack is depleted by the time last 3 is reached, use a reduced move height for calculations
                    }
                }
                //Then multiply the amount that the stacked bar will protrude over the previous maximum height by the not threes counter.
                if (threesBefore) {//Only run this if there are one or two high blocks below a three high one
                    for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for height added above max.
                        int optimisationAdd;
                        optimisationAdd = (barHeights[numbersPrelim[barRuns]] + 3) - MaxHeight; //How many units will the block stick up over the maximum bar height?
                        if (optimisationAdd < 0) { //If it doesn't stick up, we don't want to call a negative number. Instead, set it to zero.
                            optimisationAdd = 0;
                        }
                        int barPotential;
                        if (barRuns >= numberOfThrees) { //We may have situations where there are more bars than there are blocks. We don't want to try to call too large an index, so set the value to maximum array amount.
                            barPotential = numberOfThrees - 1;//Subtract one to get a zero array index.
                        } else barPotential = barRuns;
                        int[] blockThreesIndex = new int[numberOfThrees];
                        int counter = 0;
                        //Set the block indexes of all 3 high blocks into an array- if we have 3 high blocks in positions 1,3,4 we will get an array like {0,2,3}
                        for (int blockRuns = 0; blockRuns != blockHeights.length; blockRuns++) {
                            if (blockHeights[blockRuns] == 3) {
                                blockThreesIndex[counter] = blockRuns;
                                counter++;
                            }
                        }
                        //Set the maximum height to the new maximum if it is greater than the previous - we don't need to count more height if something is already that high.
                        if (MaxHeight < barHeights[barRuns] + 3) {
                            MaxHeight = barHeights[barRuns] + 3;
                        }
                        //
                        int optimisationMultiply = notThreesStackCounter(blockHeights, blockThreesIndex[barPotential]); //On that result we need to take the array index of each of them, and use it as the height to pass to not threes stack counter.
                        optimisationAdd *= optimisationMultiply;
                        optimisationBars[barRuns] += optimisationAdd;
                    }

                }
                barNumbers = numbersPrelim;
            }
            //End it here
        }
        barNumbers = optimisationOrder(optimisationBars, optimisation, barNumbers, numberOfThrees, false);//Return the final ordered array.
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
        return (numbersFinal);//For run one should be {2,3,4,5} for run 7 should be {2,4}
    }

    public int[] optimisationOrder(int[] optimisationBars, int[] optimisation, int[] barNumbers, int numberOfThrees, boolean heightcalc) {
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
        if (heightcalc) { //If height calc is true, return 7 integers for the final array. Otherwise return an array as long as the number of three high blocks.
            int[] numbersPrelim = {10, 10, 10, 10, 10, 10, 10};
            for (int currentRun = 0; currentRun <= 5; currentRun++) {
                byte r = 5;
                if (barNumbers[currentRun] <= numbersPrelim[0]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersPrelim[r + 1] = numbersPrelim[r];
                    r--;
                }
                    numbersPrelim[0] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
                } else if (barNumbers[currentRun] <= numbersPrelim[1]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 1) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersPrelim[r + 1] = numbersPrelim[r];
                    r--;
                }
                    numbersPrelim[1] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
                } else if (barNumbers[currentRun] <= numbersPrelim[2]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 2) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersPrelim[r + 1] = numbersPrelim[r];
                    r--;
                }
                    numbersPrelim[2] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
                } else if (barNumbers[currentRun] <= numbersPrelim[3]) {// Is the number currently been examined less than the first position in the optimisation array?
                while (r >= 3) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                    numbersPrelim[r + 1] = numbersPrelim[r];
                    r--;
                }
                    numbersPrelim[3] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
                } else if (barNumbers[currentRun] <= numbersPrelim[4]) {// Is the number currently been examined less than the first position in the optimisation array?
                    while (r >= 4) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                        numbersPrelim[r + 1] = numbersPrelim[r];
                        r--;
                    }
                    numbersPrelim[4] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
                } else if (barNumbers[currentRun] <= numbersPrelim[5]) {// Is the number currently been examined less than the first position in the optimisation array?
                    while (r >= 5) { // If it is, move all numbers after it down an array index. Do the same to the barNumbers array
                        numbersPrelim[r + 1] = numbersPrelim[r];
                        r--;
                    }
                    numbersPrelim[5] = barNumbers[currentRun]; // Record the Bar associated with this optimisation value.
            }
        }
            return (numbersPrelim);
        } else {
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
    }

    public int checkMaxPathingHeightToBars(int barHeights[], int leftBar, int BarOneHeight, int BarTwoHeight) { //todo-  Add comments for this area.
        int maximumHeight = 0;
        while (5 >= leftBar - 3) { //Convert a coordinate system to a bar number system -  move from 1-8 to 0-5, then run through all bars
            if (leftBar >= 3) { //If our bar is
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
        for (int blockRuns = 0; blockRuns != (blockHeights.length - (CurrentBlock)); blockRuns++) { //Run this
            if (blockHeights[blockRuns] == 1 || blockHeights[blockRuns] == 2) {
                result++;
            }
        }
        return (result);
    }

}


