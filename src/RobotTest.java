import java.util.Scanner;

public class RobotTest {
    final static String[] STAGE_A_CONFIG = {"777777", "3333"};
    final static String[] STAGE_B_CONFIG_1_CONFIG = {"734561"};
    final static String[] STAGE_B_CONFIG_2_CONFIG = {"137561"};
    final static String[] STAGE_C_TEST_1_CONFIG = {"734561", "231231"};
    final static String[] STAGE_C_TEST_2_CONFIG = {"222222", "2111"};
    final static String[] STAGE_C_TEST_3_CONFIG = {"444444", "1222"};
    final static String[] STAGE_C_TEST_4_CONFIG = {"676767", "1233"};
    final static String[] STAGE_C_TEST_5_CONFIG = {"676767", "1332"};
    final static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        int response;

        do {
            printMenu();

            System.out.print("Enter selection: ");
            while (!sc.hasNextInt()) {

                printMenu();
                sc.next(); // this is important!
            }
            response = sc.nextInt();

            System.out.println();

            switch (Integer.valueOf(response)) {
                case 0:
                    System.exit(0);
                case 1:
                    runStageATest();
                    break;

                case 2:
                    runStageBTest1();
                    break;

                case 3:
                    runStageBTest2();
                    break;

                case 4:
                    runStageCTest1();
                    break;

                case 5:
                    runStageCTest2();
                    break;

                case 6:
                    runStageCTest3();
                    break;

                case 7:
                    runStageCTest4();
                    break;

                case 8:
                    runStageCTest5();
                    break;

                case 9:
                    runCustomTest();
                    break;

                case 10:
                    runAllTests();
                    break;

                case 11:
                    runRandomTest();
                    break;

                default:
                    System.out.println("Error - invalid selection!");
            }
            System.out.println();

        }
        while (response != 0);
    }


    public static void runStageATest() {
        System.out.println(
                "Running Stage A Test - bars = 777777, blocks = 3333 (default bar / block config)");
        System.out.println("Minimum move count: 94)");
        Robot.main(STAGE_A_CONFIG);
    }


    public static void runStageBTest1() {
        System.out.println(
                "Running Stage B Test - bars = 734561, blocks = 3333 (default block config)");
        System.out.println("Minimum move count: 90)");
        Robot.main(STAGE_B_CONFIG_1_CONFIG);
    }


    public static void runStageBTest2() {
        System.out.println(
                "Running Stage B Test - bars = 137561, blocks = 3333 (default block config)");
        System.out.println("Minimum move count: 106)");
        Robot.main(STAGE_B_CONFIG_2_CONFIG);
    }

    public static void runStageCTest1() {
        System.out.println(
                "Running Stage C Test 1 - bars = 734561, blocks = 231231");
        System.out.println("Minimum move count: 224)");
        Robot.main(STAGE_C_TEST_1_CONFIG);
    }

    public static void runStageCTest2() {
        System.out.println(
                "Running Stage C Test 2 - bars = 222222, blocks = 2111");
        System.out.println("Minimum move count: 98)");
        Robot.main(STAGE_C_TEST_2_CONFIG);
    }

    public static void runStageCTest3() {
        System.out.println(
                "Running Stage C Test 3 - bars = 444444, blocks = 1222");
        System.out.println("Minimum move count: 112)");
        Robot.main(STAGE_C_TEST_3_CONFIG);
    }

    public static void runStageCTest4() {
        System.out.println(
                "Running Stage C Test 4 - bars = 676767, blocks = 1233");
        System.out.println("Minimum move count: 148)");
        Robot.main(STAGE_C_TEST_4_CONFIG);
    }

    public static void runStageCTest5() {
        System.out.println(
                "Running Stage C Test 5 - bars = 734561, blocks = 1332");
        System.out.println("Minimum move count: 132)");
        Robot.main(STAGE_C_TEST_5_CONFIG);
    }

    public static void runCustomTest() {
        System.out.print(
                "Running custom test - please enter bar and block heights below: ");
        Scanner scCustom = new Scanner(System.in);
        String configInput = scCustom.nextLine();
        System.out.println();

        String[] configArgs = configInput.split(" ");
        System.out.println("Running robot with config bars = "
                + (configArgs[0].length() == 0 ? "{7,7,7,7,7,7} (default)"
                : configArgs[0])
                + ", blocks: " + (configArgs.length == 1 ? "{3,3,3,3} (default)"
                : configArgs[1]));
        System.out.println();

        Robot.main(configArgs);
    }

    public static void runRandomTest() {
/* Alex Kinross-Smith 2016
   s3603437@student.rmit.edu.au
   alex@akinrosssmith.id.au
*/
        Scanner RunsInput = new Scanner(System.in);
        System.out.println("Enter the number of runs you want this to do");
        int runs = RunsInput.nextInt();
        int runsMax = runs;
        while (1 <= runs) {
            //generate random number between 1 & 7
            String BarA = Integer.toString(1 + (int) (Math.random() * 7));
            String BarB = Integer.toString(1 + (int) (Math.random() * 7));
            String BarC = Integer.toString(1 + (int) (Math.random() * 7));
            String BarD = Integer.toString(1 + (int) (Math.random() * 7));
            String BarE = Integer.toString(1 + (int) (Math.random() * 7));
            String BarF = Integer.toString(1 + (int) (Math.random() * 7));
            //Generate our 6 digit bar config
            String Bars = BarA + BarB + BarC + BarD + BarE + BarF;
            //Run the block config method until Blocks returns something that isn't nope
            String Blockcreate = "nope";
            while (Blockcreate.equals("nope")) {
                Blockcreate = Blocks();
            }
            //Print the final result
            System.out.print("Running random test number: " + (runsMax - runs) + " and config:");
            System.out.println(Bars + " " + Blockcreate);
            String[] FinalOutput = {Bars, Blockcreate};
            Robot.main(FinalOutput);

            runs--;
        }
    }

    //Blocks method
    public static String Blocks() {
        int runs = 0;
        int[] Block;
        Block = new int[12];
        //Generate the Block array- populate it with 12 random values between 1 & 3
        while (runs <= 11) {
            Block[runs] = 1 + (int) (Math.random() * 3);
            runs++;
        }
        int[] Add;
        Add = new int[10];
        //create new array- "Add" and populate it with 10 values of added 12 random blocks
        Add[0] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7] + Block[8] + Block[9] + Block[10] + Block[11] + Block[0];
        Add[1] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7] + Block[8] + Block[9] + Block[10] + Block[11];
        Add[2] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7] + Block[8] + Block[9] + Block[10];
        Add[3] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7] + Block[8] + Block[9];
        Add[4] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7] + Block[8];
        Add[5] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6] + Block[7];
        Add[6] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5] + Block[6];
        Add[7] = Block[1] + Block[2] + Block[3] + Block[4] + Block[5];
        Add[8] = Block[1] + Block[2] + Block[3] + Block[4];
        Add[9] = Block[1] + Block[2] + Block[3];

        String BlockString = "nope";
        int Increment = 0;
        //Loop- run to find the highest number of Blocks that can be added to make 12 or less
        boolean stop = false;
        while (!stop) {
            if (Add[Increment] <= 12) {
                stop = true;
                //pick a number of blocks between 0 and max number of blocks that adds to less than 12
                //iteration one - creates a number between 0 and increment
                //int choose = ((int) (Math.random() * Increment));
                //iteration 2 - creates a number between 9 and increment
                int choose = 9 - ((int) (Math.random() * Increment));
                //int choose = Increment + 3;  //this works - anything larger than Increment gives us our result
                //Concatenate blocks into a block config. Max total of 12 when added. Picks a random number of blocks.
                if (Add[choose] <= 12) {
                    if (choose == 0) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]) + Integer.toString(Block[8]) + Integer.toString(Block[9]) + Integer.toString(Block[10]) + Integer.toString(Block[11]) + Integer.toString(Block[0]);
                        return (BlockString);
                    } else if (choose == 1) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]) + Integer.toString(Block[8]) + Integer.toString(Block[9]) + Integer.toString(Block[10]) + Integer.toString(Block[11]);
                        return (BlockString);
                    } else if (choose == 2) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]) + Integer.toString(Block[8]) + Integer.toString(Block[9]) + Integer.toString(Block[10]);
                        return (BlockString);
                    } else if (choose == 3) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]) + Integer.toString(Block[8]) + Integer.toString(Block[9]);
                        return (BlockString);
                    } else if (choose == 4) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]) + Integer.toString(Block[8]);
                        return (BlockString);
                    } else if (choose == 5) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]) + Integer.toString(Block[7]);
                        return (BlockString);
                    } else if (choose == 6) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]) + Integer.toString(Block[6]);
                        return (BlockString);
                    } else if (choose == 7) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]) + Integer.toString(Block[5]);
                        return (BlockString);
                    } else if (choose == 8) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]) + Integer.toString(Block[4]);
                        return (BlockString);
                    } else if (choose == 9) {
                        BlockString = Integer.toString(Block[1]) + Integer.toString(Block[2]) + Integer.toString(Block[3]);
                        return (BlockString);
                    }
                }
            }
            Increment++;
        }
        return (BlockString);
    }


    public static void runAllTests() {
        System.out.println(
                "Running all tests - hit enter after each test is complete to contine:");
        System.out.println();

        runStageATest();

        System.out.println();

        runStageBTest1();

        System.out.println();

        runStageBTest2();

        System.out.println();

        runStageCTest1();

        System.out.println();

        runStageCTest2();

        System.out.println();

        runStageCTest3();

        System.out.println();

        runStageCTest4();

        System.out.println();

        runStageCTest5();

        System.out.println();

    }

    public static void printMenu() {
        System.out.println(
                "************************** ROBOT TEST HARNESS MENU **************************");
        System.out.println();

        System.out.println("1. Stage A Test 1 - bars = 777777, blocks = 3333 (default bar / block config)\n");
        System.out.println("2. Stage B Test 1 - bars = 734561, blocks = 3333 (default block config)\n");
        System.out.println("3. Stage B Test 2 - bars = 137561, blocks = 3333 (default block config)\n");
        System.out.println("4. Stage C Test 1 - bars = 734561, blocks = 231231\n");
        System.out.println("5. Stage C Test 2 - bars = 222222, blocks = 2111\n");
        System.out.println("6. Stage C Test 3 - bars = 444444, blocks = 1222\n");
        System.out.println("7. Stage C Test 4 - bars = 676767, blocks = 1233\n");
        System.out.println("8. Stage C Test 5 - bars = 676767, blocks = 1332\n");
        System.out.println("9. Stage C Test Custom (user supplies bar / block config)\n");
        System.out.println("10. Run All Tests (1 - 8)\n");
        System.out.println("11. Run Random Test\n");
        System.out.println("0. Exit Test Harness\n");
        System.out.println();
    }
}
