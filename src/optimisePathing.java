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
    public static void main(String[] args) { //literally broken, need more debugging
        int[] barHeights = {4,1,2,3,6,1};
        int BarRuns = 0; // We can assume that there are always 6 bars
        int[] BarNumbers = {0,0,0,0,0,0};
        while(BarRuns <= barHeights.length - 1) {
            int[] OptimisationBars = {0,0,0,0,0,0,0};
            OptimisationBars[BarRuns] = (7-barHeights[BarRuns])+(6-BarRuns);
            int[] Optimisation = {20,20,20,20,20,20,20};
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
        System.out.print(BarNumbers);
    }

}
