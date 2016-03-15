/*
 * Copyright (c) 2016.
 * kin0025 aka Alexander Kinross-Smith
 * s3603437@student.rmit.edu.au
 * alex@akinrosssmith.id.au
 */

/*
*@author - Alex Kinross-Smith
*/

/**
 * Created by Alex on 12/03/2016.
 */
public class optimisePathing {
    public static void main(String[] args) {
        int[] barHeights = {4,1,2,3,6,1};
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0,0,0,0,0,0,0};
        int[] OptimisationBars = {0,0,0,0,0,0,0};
        int[] Optimisation = {21,22,23,24,25,26,27};
        while(BarRuns <= 5){
            OptimisationBars[BarRuns] = (7-barHeights[BarRuns])+(6-BarRuns); //This should give {9,11,9,7,3,7}
            //End result should be {4,5,6,0/2,2/0,1}
            BarRuns++;
        }
        BarRuns = 0;
        while(BarRuns <= 5) {
            byte r = 5;
            // Check the current number against all positions in optimisation.
            if(OptimisationBars[BarRuns] <= Optimisation[0]){// Is the number currently been examined less than the first position in the optimisation array?
                    while(r >= 0){ // If it is, move all numbers after it down an array index. Do the same to the BarNumbers array
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[0]=OptimisationBars[BarRuns]; // Fill the spot left after moving all the numbers down
                    BarNumbers[0]=BarRuns; // Record the Bar associated with this optimisation value.
                }
                else if(OptimisationBars[BarRuns] <= Optimisation[1]){
                    while(r >= 1){
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[1]=OptimisationBars[BarRuns];
                    BarNumbers[1]=BarRuns;
                }
                else if(OptimisationBars[BarRuns] <= Optimisation[2]){
                    while(r >= 2){
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[2]=OptimisationBars[BarRuns];
                    BarNumbers[2]=BarRuns;
                }
                else if(OptimisationBars[BarRuns] <= Optimisation[3]){
                    while(r >= 3){
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[3]=OptimisationBars[BarRuns];
                    BarNumbers[3]=BarRuns;
                }
                else if(OptimisationBars[BarRuns] <= Optimisation[4]){
                    while(r >= 4){
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[4]=OptimisationBars[BarRuns];
                    BarNumbers[4]=BarNumbers[BarRuns];
                }
                else if(OptimisationBars[BarRuns] <= Optimisation[5]){
                    while(r >= 5){
                        Optimisation[r+1]=Optimisation[r];
                        BarNumbers[r+1]=BarNumbers[r];
                        r--;
                    }
                    Optimisation[5]=OptimisationBars[BarRuns];
                    BarNumbers[5]=BarNumbers[BarRuns];
                }
            BarRuns++;
        }
        //convert the bar numbers array to coordinates
        int x = 0;
        while(x <= 5){
//            BarNumbers[x] = BarNumbers[x]+3;
            System.out.print(BarNumbers[x] + ",");
            x++;
        }
    }

}
