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

    /*
    *************LEVEL -1 ***************************
    * Assignment definition- does not bar optimisation, fills from left to right from position 3. Not complex. :(
    *
    *************LEVEL 0*****************************
    * Only basic position optimisation is performed. Works effectively and perfectly for part B, but does not work perfectly on Part C.
    *
    ************LEVEL 1******************************
    * Imperfect, but nonetheless effective optimisation is run. It will not pick up all potential optimisations, but ones with large numbers of moves to be saved should be found.
    * E.g 122347 1111111113 vs 122347 311111111
    * In situation 1, the 3 is the first block as all the one high bars pass over it, it should be places on the 4 high bar.
    * In the second situation, it is the last block. As none of the blocks will pass over it, it should be placed in the 7 high bar.
    */
    private final int OPTIMISATION_LEVEL = -1;
    private final int SUPER_SPEED = 20; //Set the speed up value - values larger the 20 may cause random errors.

    public void control(int barHeights[], int blockHeights[]) {
        r.speedUp(SUPER_SPEED);//For testing large numbers of iterations.
        if (OPTIMISATION_LEVEL > -1) {
            controlMechanismOptimisedC(barHeights, blockHeights); //Only run this if optimisation is enabled beyond an interpretation of spec.
        } else {
            controlMechanismC(barHeights, blockHeights);
        }
    }

    private void controlMechanismC(int barHeights[], int blockHeights[]) {
        //Setting cranes initial position as variables
        int[] position;
        position = new int[3];
        position[0] = 2;         // Initial height of arm 1
        position[1] = 1;         // Initial width of arm 2
        position[2] = 0;         // Initial drop of arm 3
        int numberOfBlocks = blockHeights.length; //How many blocks need to be moved.
        int maxHeight = 0; //Maximum height of bars or stack.
        int stackHeight = 0; //Height of stacked blocks.
        int maxBlockSize = 0; //Maximum size of blocks. Used to determine if code is necessary
        int firstOne = 100; //Position of first one high block
        int firstTwo = 100; //Position of first two high block
        int firstThree = 100; //Position of first three high block in stack
        int numberOfThreeBlocks = 0; //Number of blocks to be placed on bars
        int maxStackedHeight = 0; //Expected maximum height
        int topBlockNumber = blockHeights.length - 1; //Array index of the current top block
        int barOneHeight = 0; //Height of the first bar - where the one high blocks are placed
        int barTwoHeight = 0; //Height of the second bar - where the two high blocks are placed
        int barThreesPosition = 0; //This is used to keep track of what bar we are working on - refers to array index of barOptimised
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
            }//End if
        }//End for

        if (maxHeight < maxStackedHeight) { //Finding maximum expected height.
            maxHeight = maxStackedHeight;
        }//End if

        if (maxHeight >= stackHeight + 1) { //Move to the highest expected point.
            position[0] = moveVerticalTo(maxHeight + 1, position[0]);
        } else position[0] = moveVerticalTo(stackHeight + 1, position[0]);
        /* ****************MOVEMENT LOOP ******************* */
        while (numberOfBlocks != 0) {
            int currentBlock = blockHeights[topBlockNumber];
            topBlockNumber--;
            //Move to the stack
            position[1] = moveHorizontalTo(10, position[1]);
            //Drop to the top position of the stack
            position = moveCraneToPosition(stackHeight, position);
            r.pick();
            //We have removed a block, recalculate stack height
            stackHeight -= currentBlock;
            //Move to the stack for free vertical movement
            position[1] = moveHorizontalTo(9, position[1]);
            int MoveTo;
            int currentBarHeight;
            //Pick what to do based on the block been placed.
            switch (currentBlock) {
                case 1:
                    MoveTo = 1; //If the block is one high, place it on the first bar.
                    currentBarHeight = barOneHeight; //Set our vertical height to that of the first bar.
                    break;
                case 2:
                    MoveTo = 2; //If the block is two high, place it on the second bar.
                    currentBarHeight = barTwoHeight;//Set our vertical height to that of the second bar.
                    break;

                case 3:
                    MoveTo = barThreesPosition + 3; //If the block is three high, place it on the bar calculated by the optimisation algorithm for our current block.
                    currentBarHeight = barHeights[barThreesPosition]; //Set the height to that of the bar chosen
                    break;
                default:
                    MoveTo = barThreesPosition + 3; //Should never be called. Same as condition 3
                    currentBarHeight = barHeights[barThreesPosition];
                    break;

            }
            //Calculating the minimum height we can move to get to the chosen position
            int minMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, barOneHeight, barTwoHeight);
            //Move to our vertical position to prepare for horizontal movement.
            position = moveCraneToPosition(minMoveHeight + currentBlock /* To take bar height + crane height into account */, position);
            //Move horizontally to our target
            position[1] = moveHorizontalTo(MoveTo, position[1]);
            //Drop to the top of the chosen bar
            position = moveCraneToPosition(currentBarHeight + currentBlock, position);
            r.drop();
            //Set the bar position to move the stack to based on the current block
            switch (currentBlock) {
                case 3:
                    barHeights[barThreesPosition] += 3; //Ensure heights remain up to date for pathing.
                    barThreesPosition++;
                    break;
                case 2:
                    barTwoHeight += 2;
                    break;
                case 1:
                    barOneHeight++;//Set the stack height
                    break;
                default:
            }
            numberOfBlocks--;
            if (numberOfBlocks > 0) { //If we still have blocks to move move back to the stack.
                minMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, barOneHeight, barTwoHeight);
                if (stackHeight > minMoveHeight) { //If the stack height is larger than our calculated minimum, vertical move to stack height
                    position = moveCraneToPosition(stackHeight, position); //Move to the top of the stack
                } else //Otherwise, as minMoveHeight is larger, move to that vertical position.
                    position = moveCraneToPosition(minMoveHeight, position); //Move to the min move height.
            }//End if
        }//End while
    }

    private int checkMaxPathingHeightToBars(int barHeights[], int leftBar, int barOneHeight, int barTwoHeight) { //Find the maximum height between two points.
        int maximumHeight = 0;
        while (5 >= leftBar - 3) { //Convert a coordinate system to a bar number system -  move from 1-8 to 0-5, then run through all bars. leftBar has already been set, so use a while loop
            //If our bar is not one of the first two check the bar heights array. If the current bar is higher than previous maximum, set its height as current maximum.
            if (leftBar >= 3 && barHeights[leftBar - 3] >= maximumHeight) {
                maximumHeight = barHeights[leftBar - 3];
            }
            //if the current bar is the first position, check the maximum height against it and set it if greater.
            else if (leftBar == 1 && barOneHeight >= maximumHeight) {
                maximumHeight = barOneHeight;

                //Same as first bar, but for the second bar.
            } else if (leftBar == 2 && barTwoHeight >= maximumHeight) {

                maximumHeight = barTwoHeight;

            }

            leftBar++;
        }


        return (maximumHeight);
    }

    /* Movement methods. */

    private int moveVerticalTo(int moveTo, int position) { //We get the position to move to and the current position passed to us
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

    private int moveHorizontalTo(int moveTo, int position) {
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

    private int[] moveCraneToPosition(int moveTo, int[] position) {
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

    /*
    * Code Beyond this point is only for use of extra bar optimisation beyond the assignment specification
    */

    private void controlMechanismOptimisedC(int barHeights[], int blockHeights[]) {
        //Setting cranes initial position as variables
        int[] position;
        position = new int[3];
        position[0] = 2;         // Initial height of arm 1
        position[1] = 1;         // Initial width of arm 2
        position[2] = 0;         // Initial drop of arm 3
        int numberOfBlocks = blockHeights.length; //How many blocks need to be moved.
        int maxHeight = 0; //Maximum height of bars or stack.
        int stackHeight = 0; //Height of stacked blocks.
        int maxBlockSize = 0; //Maximum size of blocks. Used to determine if code is necessary
        int firstOne = 100; //Position of first one high block
        int firstTwo = 100; //Position of first two high block
        int firstThree = 100; //Position of first three high block in stack
        int numberOfThreeBlocks = 0; //Number of blocks to be placed on bars
        int maxStackedHeight = 0; //Expected maximum height
        int topBlockNumber = blockHeights.length - 1; //Array index of the current top block
        int barOneHeight = 0; //Height of the first bar - where the one high blocks are placed
        int barTwoHeight = 0; //Height of the second bar - where the two high blocks are placed
        int barThreesPosition = 0; //This is used to keep track of what bar we are working on - refers to array index of barOptimised
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
            }//End if
        }//End for

        //Initialise a variable
        int[] barOptimised;
        barOptimised = new int[numberOfThreeBlocks + 1];
        if (maxBlockSize == 3) {//We only need to optimise for Bars if there are blocks to be placed on them.
            barOptimised = optimisePathing(barHeights, numberOfThreeBlocks, maxHeight, blockHeights); //Find more efficient ways of placing blocks on bars, and return an array that contains them in ascending order (Generalisation for most efficient)
                /*
                Finding the maximum height that the blocks will reach when placed on bars
                Height of all one and two high blocks is not needed, as stackHeight will always be larger.
                */
            for (int x = numberOfThreeBlocks; x != 0; x--) { //Setting the maximum height blocks will take up.
                if (maxStackedHeight < barHeights[barOptimised[x - 1]] + 3) {
                    maxStackedHeight = barHeights[barOptimised[x - 1]] + 3;
                }//End if
            }//End for
        }//End if
        if (maxHeight < maxStackedHeight) { //Finding maximum expected height.
            maxHeight = maxStackedHeight;
        }//End if

        if (maxHeight >= stackHeight + 1) { //Move to the highest expected point.
            position[0] = moveVerticalTo(maxHeight + 1, position[0]);
        } else position[0] = moveVerticalTo(stackHeight + 1, position[0]);
        /* ****************MOVEMENT LOOP ******************* */
        while (numberOfBlocks != 0) {
            int currentBlock = blockHeights[topBlockNumber];
            topBlockNumber--;
            //Move to the stack
            position[1] = moveHorizontalTo(10, position[1]);
            //Drop to the top position of the stack
            position = moveCraneToPosition(stackHeight, position);
            r.pick();
            //We have removed a block, recalculate stack height
            stackHeight -= currentBlock;
            //Move to the stack for free vertical movement
            position[1] = moveHorizontalTo(9, position[1]);
            int MoveTo;
            int currentBarHeight;
            //Pick what to do based on the block been placed.
            switch (currentBlock) {
                case 1:
                    MoveTo = 1; //If the block is one high, place it on the first bar.
                    currentBarHeight = barOneHeight; //Set our vertical height to that of the first bar.
                    break;
                case 2:
                    MoveTo = 2; //If the block is two high, place it on the second bar.
                    currentBarHeight = barTwoHeight;//Set our vertical height to that of the second bar.
                    break;

                case 3:
                    MoveTo = barOptimised[barThreesPosition] + 3; //If the block is three high, place it on the bar calculated by the optimisation algorithm for our current block.
                    currentBarHeight = barHeights[barOptimised[barThreesPosition]]; //Set the height to that of the bar chosen
                    break;
                default:
                    MoveTo = barOptimised[barThreesPosition] + 3; //Should never be called. Same as condition 3
                    currentBarHeight = barHeights[barOptimised[barThreesPosition]];
                    break;

            }
            //Calculating the minimum height we can move to get to the chosen position
            int minMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, barOneHeight, barTwoHeight);
            //Move to our vertical position to prepare for horizontal movement.
            position = moveCraneToPosition(minMoveHeight + currentBlock /* To take bar height + crane height into account */, position);
            //Move horizontally to our target
            position[1] = moveHorizontalTo(MoveTo, position[1]);
            //Drop to the top of the chosen bar
            position = moveCraneToPosition(currentBarHeight + currentBlock, position);
            r.drop();
            //Set the bar position to move the stack to based on the current block
            switch (currentBlock) {
                case 3:
                    barHeights[barOptimised[barThreesPosition]] += 3; //Ensure heights remain up to date for pathing.
                    barThreesPosition++;
                    break;
                case 2:
                    barTwoHeight += 2;
                    break;
                case 1:
                    barOneHeight++;//Set the stack height
                    break;
                default:
            }
            numberOfBlocks--;
            if (numberOfBlocks > 0) { //If we still have blocks to move move back to the stack.
                minMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo, barOneHeight, barTwoHeight);
                if (stackHeight > minMoveHeight) { //If the stack height is larger than our calculated minimum, vertical move to stack height
                    position = moveCraneToPosition(stackHeight, position); //Move to the top of the stack
                } else //Otherwise, as minMoveHeight is larger, move to that vertical position.
                    position = moveCraneToPosition(minMoveHeight, position); //Move to the min move height.
            }//End if
        }//End while
    }

    private int[] optimisePathing(int barHeights[], int numberOfThrees, int MaxHeight, int blockHeights[]) {
        int[] barNumbers = {0, 1, 2, 3, 4, 5, 0};
        int[] optimisationBars;
        int firstTwo = 100;
        int firstOne = 100;
        int firstThree = 100;
        int lastThreeHeight = 0;
        int lastThree = 13;
        //Finding the first block on the stack of each type
        for (int blockRuns = blockHeights.length; blockRuns != 0; blockRuns--) {
            if (blockHeights[blockRuns - 1] == 2 && firstTwo == 100) { //If current block is equal to 2 and firstTwo has not been changed since declaration, the current block must be the first one that is two high.
                firstTwo = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 1 && firstOne == 100) {//Same as above, but for blocks one high.
                firstOne = blockRuns - 1;
            }
            if (blockHeights[blockRuns - 1] == 3 && firstThree == 100) {//Same as above, but for blocks one high.
                firstThree = blockRuns - 1;
            }
        }// End blocks decrement loop
        //If there are no blocks of a type, set them to 0 so they don't interfere with calculations
        if (firstOne == 100) {
            firstOne = 0;
        }
        if (firstTwo == 100) {
            firstTwo = 0;
        }
        if (firstThree == 100) {
            firstThree = 0;
        }
        //Find the position of the lowest three block on the stack.
        for (int blockRuns = 0; blockRuns != blockHeights.length; blockRuns++) {
            if (lastThree == 13) {
                lastThreeHeight += blockHeights[blockRuns];
            }
            if (blockHeights[blockRuns] == 3 && lastThree == 13) {
                lastThree = blockRuns;
            }
        }
        //Set some values for optimisation here based on the height and position of bars. This is used to "weigh" each bar's efficiency as a spot to place blocks.
        optimisationBars = generateOptimisationValues(barNumbers, barHeights, lastThreeHeight);

        //Are there any blocks that aren't three high below a three high block on the stack?
        boolean threesBefore = false;
        if (firstThree > firstOne || firstThree > firstTwo) {
            threesBefore = true;
        }
//Only run this if higher level optimisation is enabled.
        if (OPTIMISATION_LEVEL >= 1 && threesBefore) {
            //Rerun the optimisation values on our bars again.
            for (int barRun = blockHeights.length; barRun >= 0; barRun--) {    //Iterate over this 3 times to get a rough approximation of bar ordering and any affect modified optimisation values from adding max height on will bring.
                barNumbers = optimisationOrder(optimisationBars, barNumbers, numberOfThrees, true); //We get our old output - an ordered array of what bars to place on.
                //We need to take that list, re-run our optimisation bars algorithm to get the ordered result.
                optimisationBars = generateOptimisationValues(barNumbers, barHeights, lastThreeHeight);
                int[] blockThreesIndex = new int[numberOfThrees];
                int counter = 0;
                //Set the block indexes of all 3 high blocks into an array- if we have 3 high blocks in positions 1,3,4 we will get an array like {0,2,3}
                for (int blockRuns = 0; blockRuns != blockHeights.length; blockRuns++) {
                    if (blockHeights[blockRuns] == 3) {
                        blockThreesIndex[counter] = blockRuns;
                        counter++;
                    }
                }

                //Then multiply the amount that the stacked bar will protrude over the previous maximum height by the not threes counter.
                for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for height added above max.
                    int optimisationAdd;
                    optimisationAdd = (barHeights[barNumbers[barRuns]] + 3) - MaxHeight; //How many units will the block stick up over the maximum bar height?
                    if (optimisationAdd < 0) { //If it doesn't stick up, we don't want to call a negative number. Instead, set it to zero.
                        optimisationAdd = 0;
                    }
                    int barPotential;
                    if (barRuns >= numberOfThrees) { //We may have situations where there are more bars than there are blocks. We don't want to try to call too large an index, so set the value to maximum array amount.
                        barPotential = numberOfThrees - 1;//Subtract one to get an array index.
                    } else barPotential = barRuns;

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
        }

        barNumbers = optimisationOrder(optimisationBars, barNumbers, numberOfThrees, false);//Return the final ordered array.
  /*
   * Lets work through some examples (like part B)
   * For 776332 3333 - Our output would be {6,7,8,5} / {3,4,5,2}
   * For 676623 3333 - Our output would be {7,8,5,6} / {4,5,2,3}
   * For 137561 3333 - Our output would be {8,5,6,7} / {5,2,3,4}
   * For 734561 231231 - Our output would be {8,7} / {5,4}
   * Assign them with optimisation values again
   * If a bar + block is lower than maximum pathing height -> from stack to furthest block position
   * Order changing algorithm.
   * We should end with optimisation values of {7,5,5,3,3,2} without the add bars
   * Once add bars (Number of blocks below 3 * number of units above max height) we can expect (add 4 to bars that are 6 high, add 6 to bars that are 7 high)
   * {11,11,9,9,7,7}
   * This is not the solution we are looking for. We want something for 8th bar larger than 5th bar.
   * If we create an array of the number of the optimisation value for all potential positions (maxing out at the number of threes) we can potentially get our result.
   * i.e for bar 3 we get {11,9}, bar 4 {11,8} bar 5 {9,7} bar 6 {9,6} bar 7 {7,5} bar 8 {7,4}. This is quite the conundrum.
   * I think that to accurately find the shortest path with the least moves, there is no shortcut. We need to generate optimisation values for every possible configuration (treating configurations as a single entity), and find the smallest one. This will be incredibly computationally expensive ( think ^3 +), so a toggle may need to be implemented.
   * Run through the entire blocks array to get the positions of the three high bars.
   */
        return (barNumbers);
    }

    private int[] generateOptimisationValues(int[] barNumbers, int[] barHeights, int lastThreeHeight) {
        int[] optimisationBars = {0, 0, 0, 0, 0, 0, 0};
        for (int barRuns = 0; barRuns <= 5; barRuns++) {//Set optimisation values for movements.
            //Optimisation values are found through - maximum height of bars between the stack and this bar minus the height of this bar plus the number of bars away from the stack that it is.
            optimisationBars[barRuns] = (checkMaxPathingHeightToBars(barHeights, barNumbers[barRuns] + 3, 0, 0) - barHeights[barNumbers[barRuns]]) + (6 - barNumbers[barRuns]);
        }

        //If the last three on the stack is lower than the height of the final bar, it will always be most efficient to put a block on that bar.
        for (int barRuns = 0; barRuns <= 5; barRuns++) {
            //Check to find height of last 3 high block. If it is < or equal to height of bar, then height is not  a move factor consideration. Else, treat as normally would.
            if (lastThreeHeight < barHeights[5] && barNumbers[barRuns] == 5) {
                optimisationBars[barRuns] = 1;
            }
        }

        return (optimisationBars);

    }

    private int[] optimisationOrder(int[] optimisationBars, int[] barNumbers, int numberOfThrees, boolean heightCalc) { // TODO: 21/03/2016 Add comments and simplify this area. - can loops be used more effectively?
        int[] optimisation = {21, 22, 23, 24, 25, 26, 27};
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
        if (heightCalc) { //If height calc is true, return 7 integers for the final array. Otherwise return an array as long as the number of three high blocks.
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

    private int notThreesStackCounter(int blockHeights[], int currentBlock) {
        int result = 0;
        //Run this up our blocks array up to the current block.
        for (int blockRuns = 0; blockRuns != (blockHeights.length - (currentBlock)); blockRuns++) {
            //If the current block is a 1 or a 2, it has to path over the threes above it. Increment the number of blocks that aren't a three below the block been examined.
            if (blockHeights[blockRuns] == 1) {
                result++;
            } else if (blockHeights[blockRuns] == 2) {
                result += 2;
            }
        }
        return (result);
    }

/*Code Beyond this point is legacy. It references methods that have changed in implementation, and may not work.*/
//
//public void controlMechanismBasicA(int barHeights[], int blockHeights[]) {
//    int height = 2;         // Initial height of arm 1
//    int width = 1;         // Initial width of arm 2
//    int drop = 0;         // Initial depth of arm 3
//    int dropPos = height - drop;
//    int stackHeight = 12; //how do I add this shit up
//
//    height = moveVerticalTo(13, height);
//    width = moveHorizontalTo(10, width);
//    r.pick();
//    stackHeight = stackHeight - 3;
//    width = moveHorizontalTo(5, width);
//    drop = moveCraneTo(2, drop);
//    r.drop();
//    width = moveHorizontalTo(10, width);
//    drop = moveCraneTo(3, drop);
//    r.pick();
//    stackHeight = stackHeight - 3;
//    drop = moveCraneTo(2, drop);
//    width = moveHorizontalTo(6, width);
//    r.drop();
//    width = moveHorizontalTo(10, width);
//    drop = moveCraneTo(6, drop);
//    r.pick();
//    stackHeight = stackHeight - 3;
//    drop = moveCraneTo(2, drop);
//    width = moveHorizontalTo(7, width);
//    r.drop();
//    width = moveHorizontalTo(10, width);
//    drop = moveCraneTo(9, drop);
//    r.pick();
//    drop = moveCraneTo(2, drop);
//    width = moveHorizontalTo(8, width);
//    r.drop();
//
//}
//
//    public void controlMechanismLoopedA(int barHeights[], int blockHeights[]) {
//        int height = 2;         // Initial height of arm 1
//        int width = 1;         // Initial width of arm 2
//        int drop = 0;         // Initial depth of arm 3
//        int NumberBlocks = 4;
//        int NoThreeBlocks = 4;
//        int ThreesDropPoint = 9 - NoThreeBlocks;
//        int stackHeight = 12; //we can assume height of stack does not change for A
//
//        height = moveVerticalTo(13, height);
//
//        while (NumberBlocks != 0) {
//            width = moveHorizontalTo(10, width);
//            drop = moveCraneTo((height - 1) - stackHeight, drop);
//            r.pick();
//            width = moveHorizontalTo(9, width);
//            drop = moveCraneTo(2, drop);
//            stackHeight = stackHeight - 3;
//            width = moveHorizontalTo(ThreesDropPoint, width);
//            ThreesDropPoint++;
//            r.drop();
//            NumberBlocks--;
//        }
//    }
//
//    public void controlMechanismLoopedB(int barHeights[], int blockHeights[]) {
//        int height = 2;         // Initial height of arm 1
//        int width = 1;         // Initial width of arm 2
//        int drop = 0;         // Initial depth of arm 3
//        int NumberBlocks = 4;
//        int NoThreeBlocks = blockHeights.length; // Count the number of blocks- I need to count only 3s
//        int ThreesDropBarNumber = 9 - NoThreeBlocks;
//        //setting stack height total for the end.
//        int StackHeight = 0;
//        int BlockRuns = blockHeights.length;
//        //building in some basic collision detection
//        int[] BarOptimised = optimisePathing(barHeights);
//        while (BlockRuns != 0) {
//            StackHeight += blockHeights[BlockRuns - 1];
//
//            BlockRuns--;
//        }
//        int MaxHeight = 0;
//        int BarRuns = 6;
//        while (BarRuns != 0) {
//            if (MaxHeight < barHeights[BarRuns - 1]) {
//                MaxHeight = barHeights[BarRuns - 1];
//            }
//            BarRuns--;
//        }
//        height = moveVerticalTo(13, height);
//        while (NumberBlocks != 0) {
//            //Figure out the height of the current block in the array
//            int TopBlockNumber = blockHeights.length;
//            int CurrentBlock = blockHeights[TopBlockNumber - 1];
//            --TopBlockNumber;
//            //Move to to stack
//            width = moveHorizontalTo(10, width);
//            //Drop to the top position of the stack
//            drop = moveCraneToPosition(StackHeight + 1, height, drop);
//            r.pick();
//            //move to a spot where we can safely change our vertical height- legacy code from Part A and not necessary - a soltution that does not require the horizontal move could be devised, but in number of moves is equivalent, and this is cleaner to see, but it works
//            width = moveHorizontalTo(9, width);
//            drop = moveCraneTo(2, drop);
//            StackHeight = StackHeight - 3;
//            width = moveHorizontalTo(ThreesDropBarNumber, width);
//            int currentBarHeight = barHeights[ThreesDropBarNumber - 3];
//            drop = moveCraneToPosition(currentBarHeight + 1 + CurrentBlock, height, drop);
//            r.drop();
//            if (currentBarHeight + 3 > MaxHeight) {
//                MaxHeight = currentBarHeight + 3;
//            }
//            if (MaxHeight >= StackHeight + 1) {
//                drop = moveCraneToPosition(MaxHeight + 1, height, drop);
//            } else if (StackHeight + 1 > MaxHeight) {
//                drop = moveCraneToPosition(StackHeight + 1, height, drop);
//            }
//            ThreesDropBarNumber++;
//            NumberBlocks--;
//        }
//    }
//
//    public void controlMechanismOptimisedB(int barHeights[], int blockHeights[]) {
//        //todo -  once i know how to use for loops, get rid of some of these horrible while loops
//        int height = 2;         // Initial height of arm 1
//        int width = 1;         // Initial width of arm 2
//        int drop = 0;         // Initial depth of arm 3
//        int NumberBlocks = blockHeights.length; //How many blocks need to be moved.
//        int NoThreeBlocks = 0; // Count the number of blocks that need to be put on bars
//        byte ThreesLoop = 0;
//        while (ThreesLoop != blockHeights.length) {
//            if (blockHeights[ThreesLoop] == 3) {
//                NoThreeBlocks++;
//            }
//            ThreesLoop++;
//        }
//        int[] BarOptimised = optimisePathing(barHeights); //todo add usage of this functionality
//        //setting stack height total for the end.
//        int StackHeight = 0;
//        int BlockRuns = blockHeights.length;
//        while (BlockRuns != 0) {
//            StackHeight += blockHeights[BlockRuns - 1];
//            BlockRuns--;
//        }
//        int MaxHeight = 0;
//        int BarRuns = 6;
//        while (BarRuns != 0) {
//            if (MaxHeight < barHeights[BarRuns - 1]) {
//                MaxHeight = barHeights[BarRuns - 1];
//            }
//            BarRuns--;
//        }
//        int TopBlockNumber = blockHeights.length;
//        height = moveVerticalTo(13, height);
//        int BarOneHeight = 0; //Initialising outside of loop as we don't want these set to zero again.
//        int BarTwoHeight = 0;
//        int BarThreesPosition = 0;
//        while (NumberBlocks != 0) { //Doing the actual movement
//            int CurrentBlock = blockHeights[TopBlockNumber - 1];
//            TopBlockNumber--;
//            //Move to the stack
//            width = moveHorizontalTo(10, width);
//            //Drop to the top position of the stack
//            drop = moveCraneToPosition(StackHeight, height, drop);
//            r.pick();
//            width = moveHorizontalTo(9, width);
//            StackHeight = StackHeight - CurrentBlock;
//            int MoveTo;
//            int currentBarHeight;
//            if (CurrentBlock == 1) {
//                MoveTo = 1;
//                currentBarHeight = BarOneHeight;
//            } else if (CurrentBlock == 2) {
//                MoveTo = 2;
//                currentBarHeight = BarTwoHeight;
//            } else {
//                MoveTo = BarOptimised[BarThreesPosition] + 3;
//                currentBarHeight = barHeights[BarOptimised[BarThreesPosition]];
//            }
//            int MaxMoveHeight = checkMaxPathingHeightToBars(barHeights, MoveTo);
//            drop = moveCraneToPosition(MaxMoveHeight + CurrentBlock, height, drop);
//            width = moveHorizontalTo(MoveTo, width);
//            drop = moveCraneToPosition(currentBarHeight + CurrentBlock, height, drop);
//            r.drop();
//            if (StackHeight + 1 > MaxMoveHeight + 3) {
//                drop = moveCraneToPosition(StackHeight, height, drop);
//            } else drop = moveCraneToPosition(MaxMoveHeight + 3, height, drop); //replaces some other logic
//            if (CurrentBlock == 3) {
//                BarThreesPosition++;
//                barHeights[BarThreesPosition] = barHeights[BarThreesPosition] + CurrentBlock;
//            } else if (CurrentBlock == 1) {
//                BarOneHeight++;
//            } else {
//                BarTwoHeight = BarTwoHeight + 2;
//            }
//            NumberBlocks--;
//        }
//    }
//
//    //A listing of movement methods.
//    public int moveVerticalTo(int MoveTo, int Position) { //We get the position to move to and the current position passed to us
//        while (Position != MoveTo) { //when we have not moved to the correct position proceed
//            if (MoveTo < 15 && MoveTo > 0) { //Sanity checking to prevent moving to outside boundaries
//
//                if (Position < MoveTo) { //Move up, increment position
//                    r.up();
//                    Position++;
//                }
//                if (Position > MoveTo) { //move down, decrement position
//                    r.down();
//                    Position--;
//                }
//            } else if (MoveTo > 14) { //If move to is greater than 14, set it to 14
//                MoveTo = 14;
//            } else {
//                MoveTo = 0; //If it isn't greater than 14, it must be a negative number. Set to 0
//            }
//        }
//        return (Position);
//    }
//
//    public int moveHorizontalTo(int MoveTo, int Position) {
//        while (Position != MoveTo) {
//            if (MoveTo < 11 && MoveTo > 0) { //More input sanitizing
//
//                if (Position < MoveTo) { //Extend to move to move to
//                    r.extend();
//                    Position++;
//                }
//                if (Position > MoveTo) {
//                    r.contract();
//                    Position--;
//                }
//            } else if (MoveTo > 10) { //if greater than 10, set to 10
//                MoveTo = 10;
//            } else {
//                MoveTo = 1; //if less than 1 (the minimum horizontal width), set to 1
//            }
//        }
//        return (Position); //Return our position so that it stays up to date
//    }
//
//    public int moveCraneTo(int MoveTo, int Position) { //Semi Obsolete- the move crane to position replaces this, but this still has uses
//        while (Position != MoveTo) { //Why am I still writing comments for stuff that is the same as before?
//            if (MoveTo < 13 && MoveTo > -1) {
//
//                if (Position < MoveTo) {
//                    r.lower();
//                    Position++;
//                }
//                if (Position > MoveTo) {
//                    r.raise();
//                    Position--;
//                }
//
//            } else if (MoveTo > 12) {
//                MoveTo = 12;
//            } else {
//                MoveTo = 0;
//            }
//        }
//        return (Position);
//    }
//
//    public int moveCraneToPosition(int MoveTo, int Height, int Drop) {
//        MoveTo++;
//        int Position = Height - Drop; //Slightly more complex than before. - We get the position of the crane end using the height of the tower and subtracting the drop
//        while (Position != MoveTo) {
//            Position = Height - Drop;
//            if (MoveTo < 15 && MoveTo > -1) { //Is the function less than -technically couild have used >= or <=, but this works. Again making sure we don't move outside acceptable bounds.
//
//                if (Position < MoveTo) {
//                    r.raise();
//                    Drop--; //Still change the drop value, not the position one, as it is updated every run, and we need to pass the drop value back at the end.
//                }
//                if (Position > MoveTo) {
//                    r.lower();
//                    Drop++;
//                }
//
//            } else if (MoveTo > 14) {
//                MoveTo = 14;
//            } else {
//                MoveTo = 0;
//            }
//        }
//        return (Drop);
//    }
//
//    public int[] optimisePathing(int barHeights[], int NumberOfThrees) { //literally broken, need more debugging
//        int BarRuns = 0; // We can assume that there are always 6 bars
//        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
//        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
//        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
//        while (BarRuns <= 5) {
//            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
//            //End result should be {4,5,6,0/2,2/0,1}
//            BarRuns++;
//        }
//        BarRuns = 0;
//        while (BarRuns <= 5) {
//            byte r = 5;
//            // Check the current number against all positions in optimisation.
//            if (OptimisationBars[BarRuns] <= Optimisation[0]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[0] = OptimisationBars[BarRuns]; // Fill the spot left after moving all the numbers down
//                BarNumbers[0] = BarRuns; // Record the Bar associated with this optimisation value.
//            } else if (OptimisationBars[BarRuns] <= Optimisation[1]) {
//                while (r >= 1) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[1] = OptimisationBars[BarRuns];
//                BarNumbers[1] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[2]) {
//                while (r >= 2) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[2] = OptimisationBars[BarRuns];
//                BarNumbers[2] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[3]) {
//                while (r >= 3) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[3] = OptimisationBars[BarRuns];
//                BarNumbers[3] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[4]) {
//                while (r >= 4) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[4] = OptimisationBars[BarRuns];
//                BarNumbers[4] = BarNumbers[BarRuns];
//            } else if (OptimisationBars[BarRuns] <= Optimisation[5]) {
//                while (r >= 5) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[5] = OptimisationBars[BarRuns];
//                BarNumbers[5] = BarNumbers[BarRuns];
//            }
//            BarRuns++;
//        }
//        //convert the bar numbers array to coordinates
//        int x = 0;
//        while (x <= 5) {
////            BarNumbers[x] = BarNumbers[x]+3;
//            System.out.print(BarNumbers[x] + ",");
//            x++;
//
//        }
//        //For scenario 2 output should be 0,3,4,5,10
//        int[] NumbersFinal = {10, 10, 10, 10, 10};
//        int CurrentRun = NumberOfThrees - 1;
//        while (CurrentRun != -1) {
//            byte r = 3;
//            if (BarNumbers[CurrentRun] <= NumbersFinal[0]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[0] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[1]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 1) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[1] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[2]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 2) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[2] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[3]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 3) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[3] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            }
//            CurrentRun--;
//        }
//        return (NumbersFinal);
//
//    }
//
//    public int checkMaxPathingHeightToBars(int barHeights[], int LeftBar) {
//        int MaximumHeight = 0;//
//        if (LeftBar < 3) {
//            LeftBar = 3;
//        }
//        while (5 >= LeftBar - 3) {
//            if (barHeights[LeftBar - 3] >= MaximumHeight) {
//                MaximumHeight = barHeights[LeftBar - 3];
//            }
//            LeftBar++;
//        }
//
//
//        return (MaximumHeight);
//    }
//
//    public int[] optimisePathing(int barHeights[]) { //literally broken, need more debugging
//        int BarRuns = 0; // We can assume that there are always 6 bars
//        int[] BarNumbers = {0, 0, 0, 0, 0, 0, 0};
//        int[] OptimisationBars = {0, 0, 0, 0, 0, 0, 0};
//        int[] Optimisation = {21, 22, 23, 24, 25, 26, 27};
//        while (BarRuns <= 5) {
//            OptimisationBars[BarRuns] = (7 - barHeights[BarRuns]) + (6 - BarRuns); //This should give {9,11,9,7,3,7}
//            //End result should be {4,5,6,0/2,2/0,1}
//            BarRuns++;
//        }
//        BarRuns = 0;
//        while (BarRuns <= 5) {
//            byte r = 5;
//            // Check the current number against all positions in optimisation.
//            if (OptimisationBars[BarRuns] <= Optimisation[0]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[0] = OptimisationBars[BarRuns]; // Fill the spot left after moving all the numbers down
//                BarNumbers[0] = BarRuns; // Record the Bar associated with this optimisation value.
//            } else if (OptimisationBars[BarRuns] <= Optimisation[1]) {
//                while (r >= 1) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[1] = OptimisationBars[BarRuns];
//                BarNumbers[1] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[2]) {
//                while (r >= 2) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[2] = OptimisationBars[BarRuns];
//                BarNumbers[2] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[3]) {
//                while (r >= 3) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[3] = OptimisationBars[BarRuns];
//                BarNumbers[3] = BarRuns;
//            } else if (OptimisationBars[BarRuns] <= Optimisation[4]) {
//                while (r >= 4) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[4] = OptimisationBars[BarRuns];
//                BarNumbers[4] = BarNumbers[BarRuns];
//            } else if (OptimisationBars[BarRuns] <= Optimisation[5]) {
//                while (r >= 5) {
//                    Optimisation[r + 1] = Optimisation[r];
//                    BarNumbers[r + 1] = BarNumbers[r];
//                    r--;
//                }
//                Optimisation[5] = OptimisationBars[BarRuns];
//                BarNumbers[5] = BarNumbers[BarRuns];
//            }
//            BarRuns++;
//        }
//        //convert the bar numbers array to coordinates
//        int x = 0;
//        while (x <= 5) {
////            BarNumbers[x] = BarNumbers[x]+3;
//            System.out.print(BarNumbers[x] + ",");
//            x++;
//
//        }
//        //For scenario 2 output should be 0,3,4,5,10
//        int[] NumbersFinal = {10, 10, 10, 10, 10};
//        int CurrentRun = 3;
//        while (CurrentRun != -1) {
//            byte r = 3;
//            if (BarNumbers[CurrentRun] <= NumbersFinal[0]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 0) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[0] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[1]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 1) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[1] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[2]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 2) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[2] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            } else if (BarNumbers[CurrentRun] <= NumbersFinal[3]) {// Is the number currently been examined less than the first position in the optimisation array?
//                while (r >= 3) { // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
//                    NumbersFinal[r + 1] = NumbersFinal[r];
//                    r--;
//                }
//                NumbersFinal[3] = BarNumbers[CurrentRun]; // Record the Bar associated with this optimisation value.
//            }
//            CurrentRun--;
//        }
//        return (NumbersFinal);
//
//    } //Only here for the older Methods.
//
}


