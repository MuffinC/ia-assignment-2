public class ThreePrisonersDilemma {
	/*
	 This Java program models the two-player Prisoner's Dilemma game.
	 We use the integer "0" to represent cooperation, and "1" to represent
	 defection.

	 Recall that in the 2-players dilemma, U(DC) > U(CC) > U(DD) > U(CD), where
	 we give the payoff for the first player in the list. We want the three-player game
	 to resemble the 2-player game whenever one player's response is fixed, and we
	 also want symmetry, so U(CCD) = U(CDC) etc. This gives the unique ordering

	 U(DCC) > U(CCC) > U(DDC) > U(CDC) > U(DDD) > U(CDD)

	 The payoffs for player 1 are given by the following matrix: */

    static int[][][] payoff = {
            {{6,3},  //payoffs when first and second players cooperate
                    {3,0}}, //payoffs when first player coops, second defects
            {{8,5},  //payoffs when first player defects, second coops
                    {5,2}}};//payoffs when first and second players defect

	/*
	 So payoff[i][j][k] represents the payoff to player 1 when the first
	 player's action is i, the second player's action is j, and the
	 third player's action is k.

	 In this simulation, triples of players will play each other repeatedly in a
	 'match'. A match consists of about 100 rounds, and your score from that match
	 is the average of the payoffs from each round of that match. For each round, your
	 strategy is given a list of the previous plays (so you can remember what your
	 opponent did) and must compute the next action.  */


    abstract class Player {
        // This procedure takes in the number of rounds elapsed so far (n), and
        // the previous plays in the match, and returns the appropriate action.
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            throw new RuntimeException("You need to override the selectAction method.");
        }

        // Used to extract the name of this player class.
        final String name() {
            String result = getClass().getName();
            return result.substring(result.indexOf('$')+1);
        }
    }

    /* Here are four simple strategies: */

    class NicePlayer extends Player {
        //NicePlayer always cooperates
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 0;
        }
    }

    class NastyPlayer extends Player {
        //NastyPlayer always defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return 1;
        }
    }

    class RandomPlayer extends Player {
        //RandomPlayer randomly picks his action each time
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (Math.random() < 0.5)
                return 0;  //cooperates half the time
            else
                return 1;  //defects half the time
        }
    }

    class TolerantPlayer extends Player {
        //TolerantPlayer looks at his opponents' histories, and only defects
        //if at least half of the other players' actions have been defects
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int opponentCoop = 0;
            int opponentDefect = 0;
            for (int i=0; i<n; i++) {
                if (oppHistory1[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            for (int i=0; i<n; i++) {
                if (oppHistory2[i] == 0)
                    opponentCoop = opponentCoop + 1;
                else
                    opponentDefect = opponentDefect + 1;
            }
            if (opponentDefect > opponentCoop)
                return 1;
            else
                return 0;
        }
    }

    class FreakyPlayer extends Player {
        //FreakyPlayer determines, at the start of the match,
        //either to always be nice or always be nasty.
        //Note that this class has a non-trivial constructor.
        int action;
        FreakyPlayer() {
            if (Math.random() < 0.5)
                action = 0;  //cooperates half the time
            else
                action = 1;  //defects half the time
        }

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            return action;
        }
    }

    class T4TPlayer extends Player {
        //Picks a random opponent at each play,
        //and uses the 'tit-for-tat' strategy against them
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0; //cooperate by default
            if (Math.random() < 0.5)
                return oppHistory1[n-1];
            else
                return oppHistory2[n-1];
        }
    }

    // Added Strategies from https://medium.com/thinking-is-hard/a-prisoners-dilemma-cheat-sheet-4d85fe289d87
    class PROBER extends Player {
        //Prober Strat
         /*
         Strategy: Start with Defect, Cooperate, Cooperate,
         then defect if the other player has cooperated in the second and
         third move (meaning they may be Always Cooperate or another forgiving strategy);
         otherwise, play Tit For Tat.
         based on the handshake theory, fish out what others do then adapt
         would not be better than t4t or always defect, should deter agianst a small number of always coooperate (nice player)
         Quite obviouslt prober did pretty bad
         */
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 1; //Start with Defect
            else if (n==1) return 0; //Cooperate
            else if (n==2) return 0; //Cooperate

            else if (n == 3 && oppHistory1[1] ==0 && oppHistory1[2] ==0){
                return 1;
            }
            else if (n == 3 && oppHistory2[1] ==0 && oppHistory2[2] ==0){
                return 1;
            }

            else{
                if (oppHistory1[n-1] == 1)
                    return oppHistory1[n-1];
                else
                    return oppHistory2[n-1];
            }
        }
    }
    class ADAPTIVE extends Player {
        //Starts off C, C, C, C, C, C, D, D, D, D, D,
        //then takes choices which have given the best average score re-calculated after every move

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int total=0;
            int mean=0;
            if (n<=5) return 0; //cooperate for first 5
            else if (n>5 && n<=10) return 1;//defect until 10
            else{ //when n =11
                total=total+ payoff[myHistory[n-10]][oppHistory1[n-10]][oppHistory2[n-10]] + payoff[oppHistory1[n-10]][myHistory[n-10]][oppHistory2[n-10]] + payoff[oppHistory2[n-10]][oppHistory1[n-10]][myHistory[n-10]];
                mean = total/3;
                if (mean<2) return 1; // when result is 0
                else if (mean<3) return 1;// when result is 2
                else if (mean<5) return 0;// when result is 3
                else if (mean<6) return 1;// when result is 5
                else if (mean<8) return 0;// when result is 6 <- always want to be here basically
                else return 1;// when 8 scenario
            }
        }
    }
    class PAVLOV1 extends Player {
        /*
        * Plays T4T for the first 6 moves
        * Then from the 6 moves, identify the opponent.
        * Normally need to recompute every 6 rounds and decide on a threshold to decide if
        * reconfiguring is needed
        * */
        int sum=0;
        int H=0;
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if(H==1){// sus T4T for tit for 2 tats
                H=0;
                if (Math.random() < 0.5)
                    return oppHistory1[n-1];
                else
                    return oppHistory2[n-1];
            }
            if(n<6){
                // by default first 6 is T4T
                if (n==0) return 0; //cooperate by default
                if (Math.random() < 0.5)
                    return oppHistory1[n-1];
                else
                    return oppHistory2[n-1];
            }
            else{
                for(int y=0;y<6;y++){
                    sum=sum+myHistory[y];
                }

                if(sum ==0){
                    // T4T detected or always cooperate, the previous iteration is already all cooperate
                    // Hence u just need to carry on as T4T
                    if (Math.random() < 0.5)
                        return oppHistory1[n-1];
                    else
                        return oppHistory2[n-1];
                }

                else if (sum>=4){//always defect scenario
                    return 1;// defect also
                }

                else if(sum == 3){//sus T4T
                    H=1;
                    //to adopt tit4 2 tats to recover mutual cooperattion
                    if (Math.random() < 0.5)
                        return oppHistory1[n-1];
                    else
                        return oppHistory2[n-1];
                }
                else{
                    // every other strategy is random strat, counter is to return always defect
                    return 1;
                }
            }
        }
    }
    class PAVLOV2 extends Player {
        /*
         * Plays T4T for the first 6 moves
         * Then from the 6 moves, identify the opponent.
         * Recompute every 6 rounds and decide on a threshold to decide if
         * reconfiguring is needed
         * In general, a higher threshold value has seen pavlov 2 to score much better
         * */
        int sum=0;
        int H=0;
        int thresh= 100;//3x18, max is 144=8*18,  min is 0.
        int count=0;
        int avg=0;
        int total=0;
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if(count%6 ==0 && n !=0){
                for(int P=1;P<7;P++){
                    total=total+ payoff[myHistory[n-P]][oppHistory1[n-P]][oppHistory2[n-P]] + payoff[oppHistory1[n-P]][myHistory[n-P]][oppHistory2[n-P]] + payoff[oppHistory2[n-P]][oppHistory1[n-P]][myHistory[n-P]];
                }
                avg = total/18;
                if(avg<thresh){
                    count=0;
                }
            }
            if(H==1){// sus T4T for tit for 2 tats
                H=0;
                if (Math.random() < 0.5){
                    count++;
                    return oppHistory1[n-1];}
                else{
                    count++;
                    return oppHistory2[n-1];}
            }

            if(count<6){
                // by default first 6 is T4T
                if (count==0) {
                    count++;
                    return 0;} //cooperate by default
                if (Math.random() < 0.5){
                    count++;
                    return oppHistory1[n-1];}
                else{
                    count++;
                    return oppHistory2[n-1];}
            }

            else{
                for(int y=0;y<6;y++){
                    sum=sum+myHistory[y];
                }

                if(sum ==0){
                    // T4T detected or always cooperate, the previous iteration is already all cooperate
                    // Hence u just need to carry on as T4T
                    if (Math.random() < 0.5) {
                        count++;
                        return oppHistory1[n - 1];
                    }
                    else{
                        count++;
                        return oppHistory2[n-1];
                    }
                }

                else if (sum>=4){//always defect scenario
                    count++;
                    return 1;// defect also
                }

                else if(sum == 3){//sus T4T
                    H=1;
                    //to adopt tit4 2 tats to recover mutual cooperattion
                    if (Math.random() < 0.5){
                        count++;
                        return oppHistory1[n-1];}
                    else{
                        count++;
                        return oppHistory2[n-1];}
                }
                else{
                    // every other strategy is random strat, counter is to return always defect
                    count++;
                    return 1;
                }
            }
        }
    }
    /* Nasty Player From https://github.com/almightyGOSU/CZ4046-Intelligent-Agents-Assignment-2/blob/master/src/ThreePrisonersDilemma.java*/
    class Nasty2 extends NastyPlayer {

        //Count the number of defects by opp
        int intPlayer1Defects = 0;
        int intPlayer2Defects = 0;

        //Store the round where agent retaliate against defects
        int intRoundRetailate = -1;

        //Number of rounds where agent coop to observer opp actions
        int intObservationRound = 1;

        //Number of rounds where agent retaliate defects with defects
        //After this round, see opp actions to check if they decide to coop again
        int intGrudgeRound = 3;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {

            //Record Defects count
            if (n > 0) {
                intPlayer1Defects += oppHistory1[n - 1];
                intPlayer2Defects += oppHistory2[n - 1];
            }

            //Start by cooperating
            if (n < intObservationRound) {
                return 0; //cooperate by default
            }

            //Loop rounds where agent coop to reverse the effects of retaliation
            if (intRoundRetailate < -1) {
                intRoundRetailate += 1;
                intPlayer1Defects = 0;
                intPlayer2Defects = 0;
                return 0;
            }

            //Check at round retaliated + threshold to measure if opp wishes to coop again
            if (intRoundRetailate > -1 && n == intRoundRetailate + intGrudgeRound + 1) {
                //Count the number of coop during retaliate round to check opp coop level
                int intPlayer1Coop = 0;
                int intPlayer2Coop = 0;

                for (int intCount = 0; intCount < intGrudgeRound; intCount++) {
                    intPlayer1Coop += oppHistory1[n - 1 - intCount] == 0 ? 1 : 0;
                    intPlayer2Coop += oppHistory2[n - 1 - intCount] == 0 ? 1 : 0;
                    //intPlayer1Coop += oppHistory1[n - 1 - intCount] == 1 ? 1 : 0;
                    //intPlayer2Coop += oppHistory2[n - 1 - intCount] == 1 ? 1 : 0;
                }

                //If both players wish to coop again, start to coop with them
                if (intPlayer1Coop > 1 && intPlayer2Coop > 1 && (oppHistory1[n - 1] + oppHistory2[n - 1]) == 0) {
                    //Hold round where agent coop to show intention to coop again
                    //Count backwards from -2
                    //-2 indicates 1 round where agent coop to reverse effect of retailation
                    //-5 indicates 4 rounds where agent coop to reverse effect
                    intRoundRetailate = -2;
                    intPlayer1Defects = 0;
                    intPlayer2Defects = 0;
                    return 0;
                } else {
                    intRoundRetailate = n;
                    return 1;
                }

            }

            //Punish Defection by defecting straight away
            //Stores the round defected
            if (intPlayer1Defects + intPlayer2Defects > 0) {
                intRoundRetailate = n;
                return 1;
            }
            //Coop as default action
            return 0;
        }
    }
    class Mundhra_Shreyas_Sudhir_Player extends Player {
        private int opp1Defects = 0;
        private int opp2Defects = 0;

        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            // cooperate if this is the first round
            if (n == 0)
                return 0;

            else {
                // find how many times each opponent has defected in the past
                opp1Defects += oppHistory1[n - 1];
                opp2Defects += oppHistory2[n - 1];

                // cooperate if both opponents have mostly cooperated
                if (opp1Defects <= n / 2 && opp2Defects <= n / 2)
                    return 0;

                // defect if both opponents have mostly defected
                if (opp1Defects > n / 2 && opp2Defects > n / 2)
                    return 1;

                    // one opponent has mostly cooperated and another has mostly defected
                else {
                    // find scores upto the current round
                    float[] scores = calculateScores(myHistory, oppHistory1, oppHistory2);

                    // if my agent does not have the least score, use simple majority strategy
                    if (scores[1] < scores[0] || scores[2] < scores[0]) {
                        return switchToSimpleMajority(n, myHistory, oppHistory1, oppHistory2);
                    }

                    // if my agent has the least score
                    else {
                        float[][] probDists = new float[2][2];

                        // find probability of each action for each opponent
                        probDists[0] = findProbabilityDist(oppHistory1);
                        probDists[1] = findProbabilityDist(oppHistory2);

                        // find expected utility for cooperating and defecting
                        float coopUtil = findExpectedUtility(0, probDists);
                        float defectUtil = findExpectedUtility(1, probDists);

                        // choose action having higher expected utility
                        if (coopUtil > defectUtil)
                            return 0;

                        return 1;
                    }
                }
            }
        }

        // simple majority strategy
        int switchToSimpleMajority(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int opponentCoop1 = 0, opponentCoop2 = 0;
            int predAction1, predAction2;

            // find how many times each opponent has cooperated
            for (int i = 0; i < n; i++) {
                if (oppHistory1[i] == 0) {
                    opponentCoop1 += 1;
                }
                if (oppHistory2[i] == 0) {
                    opponentCoop2 += 1;
                }
            }

            // predict action of opponent 1 that it as performed most of the time
            if (opponentCoop1 > n / 2)
                predAction1 = 0;
            else
                predAction1 = 1;

            // predict action of opponent 2 that it as performed most of the time
            if (opponentCoop2 > n / 2)
                predAction2 = 0;
            else
                predAction2 = 1;

            // choose action that maximizes the payoff for the predicted actions
            if (payoff[0][predAction1][predAction2] > payoff[1][predAction1][predAction2])
                return 0;

            return 1;
        }

        // calculate scores of all the players
        float[] calculateScores(int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            int rounds = myHistory.length;
            float ScoreA = 0, ScoreB = 0, ScoreC = 0;

            for (int i = 0; i < rounds; i++) {
                ScoreA = ScoreA + payoff[myHistory[i]][oppHistory1[i]][oppHistory2[i]];
                ScoreB = ScoreB + payoff[oppHistory1[i]][oppHistory2[i]][myHistory[i]];
                ScoreC = ScoreC + payoff[oppHistory2[i]][myHistory[i]][oppHistory1[i]];
            }

            float[] result = { ScoreA / rounds, ScoreB / rounds, ScoreC / rounds };
            return result;
        }

        // find probability distribution of the actions for a given opponent
        float[] findProbabilityDist(int[] history) {
            float[] probDist = new float[2];

            // count the number of times the opponent in question has cooperated or defected
            for (int i = 0; i < history.length; i++) {
                probDist[history[i]]++;
            }

            // find probability that the opponent in question will cooperate or defect
            probDist[0] = probDist[0] / history.length;
            probDist[1] = probDist[1] / history.length;

            return probDist;
        }

        // find expected utility if the agent performs a certain action
        float findExpectedUtility(int action, float[][] probDists) {
            float expectedUtility = 0;

            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    expectedUtility += probDists[0][j] * probDists[1][k] * payoff[action][j][k];
                }
            }

            return expectedUtility;
        }
    }



    // win stay lose shift
    class WinStayLoseShift extends Player {
        int selectAction(int n, int[] myHistory, int[] oppHistory1, int[] oppHistory2) {
            if (n==0) return 0;

            int r = n - 1;
            int myLA = myHistory[r];
            int oppLA1 = oppHistory1[r];
            int oppLA2 = oppHistory2[r];

            if (payoff[myLA][oppLA1][oppLA2]>=5) return myLA;
            return oppAction(myLA);
        }

        private int oppAction(int action) {
            if (action==1) return 0;
            return 1;
        }
    }


    /* In our tournament, each pair of strategies will play one match against each other.
     This procedure simulates a single match and returns the scores. */
    float[] scoresOfMatch(Player A, Player B, Player C, int rounds) {
        int[] HistoryA = new int[0], HistoryB = new int[0], HistoryC = new int[0];
        float ScoreA = 0, ScoreB = 0, ScoreC = 0;

        for (int i=0; i<rounds; i++) {
            int PlayA = A.selectAction(i, HistoryA, HistoryB, HistoryC);
            int PlayB = B.selectAction(i, HistoryB, HistoryC, HistoryA);
            int PlayC = C.selectAction(i, HistoryC, HistoryA, HistoryB);
            ScoreA = ScoreA + payoff[PlayA][PlayB][PlayC];
            ScoreB = ScoreB + payoff[PlayB][PlayC][PlayA];
            ScoreC = ScoreC + payoff[PlayC][PlayA][PlayB];
            HistoryA = extendIntArray(HistoryA, PlayA);
            HistoryB = extendIntArray(HistoryB, PlayB);
            HistoryC = extendIntArray(HistoryC, PlayC);
        }
        float[] result = {ScoreA/rounds, ScoreB/rounds, ScoreC/rounds};
        return result;
    }

    //	This is a helper function needed by scoresOfMatch.
    int[] extendIntArray(int[] arr, int next) {
        int[] result = new int[arr.length+1];
        for (int i=0; i<arr.length; i++) {
            result[i] = arr[i];
        }
        result[result.length-1] = next;
        return result;
    }

	/* The procedure makePlayer is used to reset each of the Players
	 (strategies) in between matches. When you add your own strategy,
	 you will need to add a new entry to makePlayer, and change numPlayers.*/

    int numPlayers = 13;
    Player makePlayer(int which) {
        switch (which) {
            case 0: return new NicePlayer();
            case 1: return new NastyPlayer();
            case 2: return new RandomPlayer();
            case 3: return new TolerantPlayer();
            case 4: return new FreakyPlayer();
            case 5: return new T4TPlayer();
            case 6: return new PROBER();
            case 7: return new ADAPTIVE();
            case 8: return new PAVLOV1();
            case 9: return new WinStayLoseShift();
            case 10: return new PAVLOV2();
            case 11: return new Nasty2();
            case 12: return new Mundhra_Shreyas_Sudhir_Player();
        }
        throw new RuntimeException("Bad argument passed to makePlayer");
    }

    /* Finally, the remaining code actually runs the tournament. */

    public static void main (String[] args) {
        ThreePrisonersDilemma instance = new ThreePrisonersDilemma();
        instance.runTournament();
    }

    boolean verbose = true; // set verbose = false if you get too much text output

    void runTournament() {
        float[] totalScore = new float[numPlayers];

        // This loop plays each triple of players against each other.
        // Note that we include duplicates: two copies of your strategy will play once
        // against each other strategy, and three copies of your strategy will play once.

        for (int i=0; i<numPlayers; i++) for (int j=i; j<numPlayers; j++) for (int k=j; k<numPlayers; k++) {

            Player A = makePlayer(i); // Create a fresh copy of each player
            Player B = makePlayer(j);
            Player C = makePlayer(k);
            int rounds = 90 + (int)Math.rint(20 * Math.random()); // Between 90 and 110 rounds
            float[] matchResults = scoresOfMatch(A, B, C, rounds); // Run match
            totalScore[i] = totalScore[i] + matchResults[0];
            totalScore[j] = totalScore[j] + matchResults[1];
            totalScore[k] = totalScore[k] + matchResults[2];
            if (verbose)
                System.out.println(A.name() + " scored " + matchResults[0] +
                        " points, " + B.name() + " scored " + matchResults[1] +
                        " points, and " + C.name() + " scored " + matchResults[2] + " points.");
        }
        int[] sortedOrder = new int[numPlayers];
        // This loop sorts the players by their score.
        for (int i=0; i<numPlayers; i++) {
            int j=i-1;
            for (; j>=0; j--) {
                if (totalScore[i] > totalScore[sortedOrder[j]])
                    sortedOrder[j+1] = sortedOrder[j];
                else break;
            }
            sortedOrder[j+1] = i;
        }

        // Finally, print out the sorted results.
        if (verbose) System.out.println();
        System.out.println("Tournament Results");
        for (int i=0; i<numPlayers; i++)
            System.out.println(makePlayer(sortedOrder[i]).name() + ": "
                    + totalScore[sortedOrder[i]] + " points.");

    } // end of runTournament()

} // end of class PrisonersDilemma
