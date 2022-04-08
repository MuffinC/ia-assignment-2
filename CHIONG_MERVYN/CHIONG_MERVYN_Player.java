
import java.util.Random;
//need random function
class CHIONG_MERVYN_Player extends Player {
    //Need random function
    //Scoreboard
    int[][][] payoff = {
            {{6, 3},     //payoffs when first and second players cooperate
                    {3, 0}},     //payoffs when first player coops, second defects
            {{8, 5},     //payoffs when first player defects, second coops
                    {5, 2}}};    //payoffs when first and second players defect

    final String NAME = "[*] MERVYN CHIONG JIA RONG";
    final String MATRIC_NO = "[*] U1921023k";

    //We need to keep track of our total scores and our opponents
    int myscore=0, oppon1score=0, oppon2score=0;
    int[] myhist, opp1hist, opp2hist;

    // If opponents are cooperative, no reason to defect. Therefore, need to keep track of cooperation
    int oppo1coop = 0, oppo2coop = 0;

    int prevround;
    //aside from this, it is ideal to always keep track on what our opponents are playing and how they react at all times + yourself
    int mylastmove, opp1lastmove, opp2lastmove;


    int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
        //Always cooperate on the first round
        if (n == 0) return 0;

        //Update all variables
        this.prevround = n - 1; // previous round index
        this.myhist = myHistory;
        this.opp1hist = oppHistory1;
        this.opp2hist = oppHistory2;

        //update what was played previously
        mylastmove = myHistory[prevround];
        opp1lastmove = oppHistory1[prevround];
        opp2lastmove = oppHistory2[prevround];

        //Update scores of opponents +me, it is assumed that you will be the player 2 for both of the other players
        this.myscore = myscore + payoff[mylastmove][opp1lastmove][opp2lastmove];
        this.oppon1score = oppon1score + payoff[opp1lastmove][opp2lastmove][mylastmove];
        this.oppon2score = oppon2score + payoff[opp2lastmove][opp1lastmove][mylastmove];

        //Always keep track of cooperation from opponents
        if (n > 0) {
            if (oppHistory1[prevround] == 0) {
                oppo1coop += 1;
            }
            if (oppHistory2[prevround] == 0) {
                oppo2coop += 1;
            }
        }

        //~Update of data completed~

        /*~Implementation of rules~
         * I will be seperating each scenario into a seperate functions
         * to make things easier to understand.
         * Scenario 1: When both player 1 and 2 are not cooperating for a vast majority of the game
         * Retaliate as a response, in here we introduce an element of randomness, because as the game goes on. Unpredictability
         * can be the 1 element needed to win. Currently, the chance of it occurring is set to 0.01%
         * */
        if (oppo1coop < n/2 && oppo2coop < n/2) {
            return RandomnessR(1);
            //return 1;
        }
        /*
         * Scenario 2: They have been rather cooperative, no reason to exploit nor break the mold.
         * Hence, no randomness is needed
         * */

        if (oppo1coop >= n/2 && oppo2coop >= n/2) {
            //return RandomnessR(0);
            return 0;
        }
        else {
            /*The above 2 statements handle when opponents have mainly defected or cooperated for a vast majority of the time
             * This is for when 1 is a majority defect and 1 is majority coop, to evaluate our action we need to see what is the best course of action
             * based on the overall grand scheme of things.
             * */
            if(myscore>oppon1score || myscore>oppon2score){
                // Scenario 3: When your agent is the 2nd highest of the trio
                return maximise(n,oppo1coop,oppo2coop);
            }
            else{
                //Scenario 4: When your agent somehow has equal amount to an ooponent
                return panacea(n,oppo1coop,oppo2coop,myHistory,oppHistory1,oppHistory2);
            }
        }
    }
    private int RandomnessR(int actions){
        // Be unpredictable, means that we need to add an element of randomness or something that seems weird so that people are thrown off
        int nums[] = new int[1000];
        Random r = new Random();
        if (actions == 0) {
            //more likely to cooperate
            for (int x = 0; x < 999; x++) {
                nums[x] = 0;
            }
            nums[999] = 1;
            int randomNumber = r.nextInt(nums.length);
            return nums[randomNumber];
        } else {
            for (int x = 0; x < 999; x++) {
                nums[x] = 1;
            }
            nums[999] = 0;
            int randomNumber = r.nextInt(nums.length);
            return nums[randomNumber];
        }
    }
    private int maximise(int total,int coop1,int coop2){
        int predic1,predic2;//prediction on what the opponent will do
        if (coop1>total/2) predic1= 0;
        else predic1=1;
        if (coop2>total/2) predic2=0;
        else predic2=1;
        //base your actions, on what was predicted that will net you the highest gain
        if(payoff[0][predic1][predic2]>payoff[1][predic1][predic2]) return 0;
        else return 1;
    }
    private int panacea(int total,int coop1,int coop2,int []myhist,int[]opp1hist,int[]opp2hist){
        int defect1,defect2;
        defect1 = total-coop1;
        defect2 = total-coop2;
        double cooputil=0,defectutil=0;
        float[] probDist1 = new float[2];
        float[] probDist2 = new float[2];

        probDist1[0]=coop1/opp1hist.length;// coop probability for opp1
        probDist1[1]=defect1/opp1hist.length;// defect probability for opp1

        probDist2[0]=coop2/opp2hist.length;// coop probability for opp2
        probDist2[1]=defect2/opp2hist.length;// defect probability for opp1
        //cooputility
        for(int x=0;x<2;x++){
            for(int y=0;y<2;y++){
                cooputil+= probDist1[x]* probDist2[y]* payoff[0][x][y];
            }
        }
        //defectutility
        for(int x=0;x<2;x++){
            for(int y=0;y<2;y++){
                defectutil+= probDist1[x]* probDist2[y]* payoff[1][x][y];
            }
        }
        if (cooputil>defectutil)return 0;
        else return 1;

    }
}
