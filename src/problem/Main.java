package problem;

import java.io.IOException;
import java.util.HashMap;

import simulator.*;

public class Main {

    public static int fuelSteps = 5; // Must be a factor of 10 as we only refuel in steps of 10
    private static HashMap<CustomState, ScoredAction> genStates(ProblemSpec ps){

        HashMap<CustomState, ScoredAction> allStates = new HashMap<>();

        for(int pos = 1; pos <= ps.getN(); pos++){
            for(String car : ps.getCarOrder()){
                for(String driver : ps.getDriverOrder()){
                    for(Tire tire : ps.getTireOrder()){
                        for(TirePressure tp : TirePressure.values()){
                            for(int fuel = 0; fuel <= 50; fuel += fuelSteps){
                                CustomState temp = new CustomState(pos, false, false, car,
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

    private static ScoredAction valueIterate(ProblemSpec ps, CustomState currentState, HashMap<CustomState, ScoredAction> currentSet,
                                       HashMap<CustomState, ScoredAction> allSet){

        ScoredAction ret = null;
        Action act;

        double max = 0;

        CustomState projectedState;
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

                            projectedState = currentState.consumeFuel((int)Math.ceil(reqFuel / fuelSteps) * fuelSteps);

                            if(i < 10){

                                projectedState = projectedState.changePosition((i - 4), ps.getN());

                                vNext = currentSet.get(projectedState).getScore();

                                sum += moveProbs[i] * vNext;

                            }else{ // slip or breakdown

                                vNext = currentSet.get(projectedState).getScore();

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
                        if (!car.equals(currentState.getCarType())) {
                            for (String driver : ps.getDriverOrder()) {
                                if (!driver.equals(currentState.getDriver())) {
                                    projectedState = currentState.changeCarAndDriver(car, driver);

                                    vNext = currentSet.get(projectedState).getScore();
                                    rSet = allSet.get(currentState).getScore();

                                    res = rSet + ps.getDiscountFactor() * vNext;

                                    if (max <= res) {
                                        max = res;
                                        act = new Action(ActionType.CHANGE_CAR_AND_DRIVER, car, driver);
                                        ret = new ScoredAction(act, res);
                                    }
                                }
                            }
                        }
                    }
                    break;

                default:
                    System.out.println("Cannot Run Level 5");
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
        long beforeMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        Simulator sim = new Simulator(ps, output);
        switch (ps.getLevel().getLevelNumber()) {
            case 1:
                fuelSteps = 50;
                break;

            case 2:
                fuelSteps = 1;
                break;

            case 3:
                fuelSteps = 5;
                break;

            case 4:
                fuelSteps = 5;
                break;
        }

        HashMap<CustomState, ScoredAction> allStates = genStates(ps);   // that R(s) good shit

        HashMap<CustomState, ScoredAction> current = (HashMap<CustomState, ScoredAction>) allStates.clone(); // v(s)
        HashMap<CustomState, ScoredAction> next = new HashMap(); // v[t+1](s)

        System.out.println("Number of states = " + allStates.size());

        boolean hasConverged = false;

        long startTime = System.nanoTime();
        while(!hasConverged){
            System.out.print(".");

            hasConverged = true;

            for(CustomState state : current.keySet()){
                if (state.getPos() == ps.getN()) {
                    next.put(state, new ScoredAction(null, 100));
                    continue;
                }

                ScoredAction res = valueIterate(ps, state, current, allStates);

                double v = current.get(state).getScore();
                double vdash = res.getScore();

                next.put(state, res);
                if(hasConverged && (Math.abs(v - vdash) > 0.0001)) {
                    hasConverged = false;
                }
                if ((System.nanoTime() - startTime)/1000000 > 119000) {
                    hasConverged = true;
                    break;
                }
            }

            current = (HashMap<CustomState, ScoredAction>) next.clone();
        }
        System.out.println("Done");

        long endTime = System.nanoTime();
        System.out.println("Planning time: " + (endTime - startTime)/1000000);

        CustomState start = new CustomState(sim.reset());  // initial state
        while(!sim.isGoalState(start.returnState()) && start != null){
            start = start.consumeFuel(start.getFuel() % fuelSteps);
            ScoredAction step = current.get(start);
            start = new CustomState(sim.step(step.getAction()));
        }
        endTime = System.nanoTime();
        System.out.println("Total time: " + (endTime - startTime)/1000000);
        long afterMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        long memUsed = afterMem - beforeMem;
        System.out.println("Memory used = " + memUsed);
    }

    public static void main(String[] args) {

        String input = "examples/level_2/input_lvl2_demo.txt";
        String output = "outputs/test.txt";

        if (args.length != 2) {
            System.out.println("Usage: java ProgramName inputFileName outputFileName");
            System.exit(2);
        } else {
            input = args[0];
            output = args[1];
        }

        ProblemSpec ps;
        try {
            ps = new ProblemSpec(input);
            run(ps, output);
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");
    }
}
