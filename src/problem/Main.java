package problem;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import simulator.*;

public class Main {

    private static HashMap<State, ScoredAction> genStates(ProblemSpec ps){

        HashMap<State, ScoredAction> allStates = new HashMap<>();

        for(int pos = 1; pos <= ps.getN(); pos++){
            for(String car : ps.getCarOrder()){
                for(String driver : ps.getDriverOrder()){
                    for(Tire tire : ps.getTireOrder()){
                        for(TirePressure tp : TirePressure.values()){
                            for(int fuel = 0; fuel <= 50; fuel++){
                                State temp = new State(pos, false, false, car,
                                        fuel, tp, driver, tire);

                                Action a = new Action(ActionType.MOVE);

                                if(pos == ps.getN()){
                                    allStates.put(temp, new ScoredAction(a, 100.0));
                                }else{
                                    allStates.put(temp, new ScoredAction(a, 0.0));
                                }
                            }
                        }
                    }
                }
            }
        }

        return allStates;

    }

    private static ScoredAction valueIterate(ProblemSpec ps, State currentState, HashMap<State, ScoredAction> currentSet,
                                       HashMap<State, ScoredAction> allSet){

        ScoredAction ret = null;
        Action act;

        double max = 0;

        State projectedState;
        double vNext;
        double rSet;

        Level level = ps.getLevel();

        for(ActionType actionType : level.getAvailableActions()){
            double res;

            switch(actionType) {
                case MOVE:
                    double[] moveProbs = Util.getMoveProb(ps, currentState);

                    double sum = 0.0;
                    rSet = allSet.get(currentState).getScore();
                    int reqFuel = Util.getFuelConsumption(currentState, ps);

                    if(Util.checkMoveCondition(ps, currentState)) {

                        for (int i = 0; i < moveProbs.length; i++) {

                            if(i < 10){

                                projectedState = currentState.changePosition((i - 4), ps.getN());
                                projectedState = projectedState.consumeFuel(reqFuel);

                                vNext = currentSet.get(projectedState).getScore();

                                sum += moveProbs[i] * vNext;

                            }else{ // slip or breakdown

                                vNext = currentSet.get(currentState).getScore();

                                if(i == 11){
                                    sum += moveProbs[i] * vNext * Math.pow(ps.getDiscountFactor(),
                                            (double)(ps.getSlipRecoveryTime() - 1));
                                }else{
                                    sum += moveProbs[i] * vNext * Math.pow(ps.getDiscountFactor(),
                                            (double)(ps.getRepairTime() - 1));
                                }
                            }
                        }

                        res = rSet + ps.getDiscountFactor() * sum;

                        if(max <= res){
                            max = res;
                            act = new Action(ActionType.MOVE);
                            ret = new ScoredAction(act, res);
                        }
                    }

                    break;

                case CHANGE_CAR:
                    for(String car : ps.getCarOrder()){
                        if(!car.equals(currentState.getCarType())){
                            projectedState = currentState.changeCarType(car);

                            vNext = currentSet.get(projectedState).getScore();
                            rSet = allSet.get(currentState).getScore();

                            res = rSet + ps.getDiscountFactor()*vNext;

                            if(max <= res){
                                max = res;
                                act = new Action(ActionType.CHANGE_CAR, car);
                                ret = new ScoredAction(act, res);
                            }
                        }
                    }

                    break;

                case CHANGE_DRIVER:
                    for(String driver : ps.getDriverOrder()){
                        if(!driver.equals(currentState.getDriver())){
                            projectedState = currentState.changeDriver(driver);

                            vNext = currentSet.get(projectedState).getScore();
                            rSet = allSet.get(currentState).getScore();

                            res = rSet + ps.getDiscountFactor()*vNext;

                            if(max <= res){
                                max = res;
                                act = new Action(ActionType.CHANGE_DRIVER, driver);
                                ret = new ScoredAction(act, res);
                            }
                        }
                    }

                    break;
                case CHANGE_TIRES:

                    for(Tire tires : ps.getTireOrder()){
                        if(!tires.equals(currentState.getTireModel())){
                            projectedState = currentState.changeTires(tires);

                            vNext = currentSet.get(projectedState).getScore();
                            rSet = allSet.get(currentState).getScore();

                            res = rSet + ps.getDiscountFactor()*vNext;

                            if(max <= res){
                                max = res;
                                act = new Action(ActionType.CHANGE_TIRES, tires);
                                ret = new ScoredAction(act, res);
                            }
                        }
                    }

                    break;
                case ADD_FUEL:
                    int fuel = Math.min(10, 50 - currentState.getFuel());
                    projectedState = currentState.addFuel(fuel); // add fuel where cost is 1 time unit

                    vNext = currentSet.get(projectedState).getScore();
                    rSet = allSet.get(currentState).getScore();

                    res = rSet + ps.getDiscountFactor()*vNext;

                    if(max <= res){
                        max = res;
                        act = new Action(ActionType.ADD_FUEL, fuel);
                        ret = new ScoredAction(act, res);
                    }

                    break;
                case CHANGE_PRESSURE:

                    for(TirePressure tp : TirePressure.values()){
                        if(!tp.equals(currentState.getTirePressure())){
                            projectedState = currentState.changeTirePressure(tp);

                            vNext = currentSet.get(projectedState).getScore();
                            rSet = allSet.get(currentState).getScore();

                            res = rSet + ps.getDiscountFactor()*vNext;

                            if(max <= res){
                                max = res;
                                act = new Action(ActionType.CHANGE_PRESSURE, tp);
                                ret = new ScoredAction(act, res);
                            }
                        }
                    }

                    break;
                case CHANGE_CAR_AND_DRIVER:

                    for(String car : ps.getCarOrder()){
                        for(String driver : ps.getDriverOrder()){
                            if(!car.equals(currentState.getCarType()) &&
                                !driver.equals(currentState.getDriver())){
                                projectedState = currentState.changeCarAndDriver(car, driver);

                                vNext = currentSet.get(projectedState).getScore();
                                rSet = allSet.get(currentState).getScore();

                                res = rSet + ps.getDiscountFactor()*vNext;

                                if(max <= res){
                                    max = res;
                                    act = new Action(ActionType.CHANGE_CAR_AND_DRIVER, car, driver);
                                    ret = new ScoredAction(act, res);
                                }
                            }
                        }
                    }

                    break;
                default:

                    System.out.println("you wrong");
                    System.exit(9);

                    break;
            }
        }
        if(ret == null){
            System.out.println("you bad");
            System.exit(69);
        }
        return ret;
    }

    private static void run(ProblemSpec ps, String output){

        Simulator sim = new Simulator(ps, output);

        HashMap<State, ScoredAction> allStates = genStates(ps);   // that R(s) good shit

        HashMap<State, ScoredAction> current = (HashMap<State, ScoredAction>) allStates.clone(); // v(s)
        HashMap<State, ScoredAction> next = new HashMap(); // v[t+1](s)

        boolean hasConverged = false;

        long startTime = System.nanoTime();
        while(!hasConverged){

            hasConverged = true;

            for(State state : current.keySet()){
                ScoredAction res = valueIterate(ps, state, current, allStates);

                double v = current.get(state).getScore();
                double vdash = res.getScore();

                next.put(state, res);
                if(hasConverged && (Math.abs(v - vdash) > 10)) { // Should be smaller, e.g. 0.00001
                    System.out.println(v + " + " + vdash);
                    hasConverged = false;
                }
                if ((System.nanoTime() - startTime)/1000000 > 119000) {
                    System.out.println("Time's up!");
                    hasConverged = true;
                    break;
                }
            }

            current = (HashMap<State, ScoredAction>) next.clone();
            //next.clear();
        }

        long endTime = System.nanoTime();
        System.out.println("time: " + (endTime - startTime)/1000000);

        State start = sim.reset();  // initial state
        System.out.println(current.get(start));

        while(!sim.isGoalState(start) && start != null){

            ScoredAction step = current.get(start);
            start = sim.step(step.getAction());
        }
    }

    public static ActionType MCTS(ProblemSpec ps, State currentState, int currentStep){
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        ActionType choice = null;
        while((endTime - startTime) / 1000000 < 14000){
            Level level = ps.getLevel();



            endTime = System.nanoTime();
        }
        return choice;
    }

    public static boolean simulate(ProblemSpec ps, State simState, int currentStep){

        int req;
        int move;

        while(currentStep <= ps.getMaxT()){
            req = Util.getFuelConsumption(simState, ps);
            if(simState.getFuel() < req){
                simState = simState.addFuel(10);
                currentStep++;
            }
            else{
                move = sampleMoveDistance(ps, simState);
                if(move == 6){
                    currentStep += ps.getSlipRecoveryTime();
                }
                else if(move == 7){
                    currentStep += ps.getRepairTime();
                }
                else{
                    simState = simState.changePosition(move, ps.getN());
                    currentStep++;
                }
            }
            if(simState.getPos() == ps.getN()){
                return true;
            }
        }
        return false;

    }

    public static int sampleMoveDistance(ProblemSpec ps, State state) {

        double[] moveProbs = Util.getMoveProb(ps, state);

        double p = Math.random();
        double pSum = 0;
        int move = 0;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            pSum += moveProbs[k];
            if (p <= pSum) {
                move = ps.convertIndexIntoMove(k);
                break;
            }
        }
        return move;
    }

    public static void main(String[] args) {

        String input = "examples/level_3/input_lvl3_better_cars.txt";
        String output = "outputs/test.txt";

        if (args.length != 2) {
            System.out.println("Usage: java ProgramName inputFileName outputFileName");
            //System.exit(2);
        } else {
            input = args[0];
            output = args[1];
        }

        ProblemSpec ps;
        try {
            ps = new ProblemSpec(input);
            run(ps, output);
//            System.out.println(ps.toString());
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");
    }
}
