package problem;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import simulator.*;

public class Main {

    private static HashMap<State, Double> genStates(ProblemSpec ps){

        HashMap<State, Double> allStates = new HashMap<>();

        for(int pos = 1; pos <= ps.getN(); pos++){
            for(String car : ps.getCarOrder()){
                for(String driver : ps.getDriverOrder()){
                    for(Tire tire : ps.getTireOrder()){
                        for(TirePressure tp : TirePressure.values()){
                            for(int fuel = 0; fuel <= 50; fuel++){
                                State temp = new State(pos, false, false, car,
                                        fuel, tp, driver, tire);

                                if(pos == ps.getN()){
                                    allStates.put(temp, 100.0);
                                }else{
                                    allStates.put(temp, 0.0);
                                }
                            }
                        }
                    }
                }
            }
        }

        return allStates;

    }

    private static Double valueIterate(ProblemSpec ps, State currentState, HashMap<State, Double> currentSet,
                                       HashMap<State, Double> allSet){

        double max = 0;

        State projectedState;
        double vNext;
        double rSet;

        Level level = ps.getLevel();

        for(ActionType actionType : level.getAvailableActions()){
            double res = 0;

            switch(actionType) {
                case MOVE:
                    double[] moveProbs = Util.getMoveProb(ps, currentState);

                    double sum = 0.0;
                    rSet = allSet.get(currentState);

                    for (int i = 0; i < moveProbs.length; i++) {

                        if(Util.checkMoveCondition(ps, currentState)) {
                            if(i < 10){

                                int reqFuel = Util.getFuelConsumption(currentState, ps);

                                projectedState = currentState.changePosition((i - 4), ps.getN());
                                projectedState = projectedState.consumeFuel(reqFuel);

                                vNext = currentSet.get(projectedState);

                                sum += moveProbs[i] * vNext;

                            }else{ // slip or breakdown

                                vNext = currentSet.get(currentState);

                                if(i == 11){
                                    sum += moveProbs[i] * vNext * Math.pow(ps.getDiscountFactor(),
                                            (double)(ps.getSlipRecoveryTime() - 1));
                                }else{
                                    sum += moveProbs[i] * vNext * Math.pow(ps.getDiscountFactor(),
                                            (double)(ps.getSlipRecoveryTime() - 1));
                                }
                            }
                        }
                    }

                    if(rSet != 100){
                        System.out.println(sum);
                    }
                    res = rSet + ps.getDiscountFactor() * sum;

                    max = max > res ? max : res;

                    break;

                case CHANGE_CAR:
                    for(String car : ps.getCarOrder()){
                        if(!car.equals(currentState.getCarType())){
                            projectedState = currentState.changeCarType(car);

                            vNext = currentSet.get(projectedState);
                            rSet = allSet.get(currentState);

                            res = rSet + ps.getDiscountFactor()*vNext;
                            max = max > res ? max : res;
                        }
                    }

                    break;

                case CHANGE_DRIVER:
                    for(String driver : ps.getDriverOrder()){
                        if(!driver.equals(currentState.getDriver())){
                            projectedState = currentState.changeDriver(driver);

                            vNext = currentSet.get(projectedState);
                            rSet = allSet.get(currentState);

                            res = rSet + ps.getDiscountFactor()*vNext;
                            max = max > res ? max : res;
                        }
                    }

                    break;
                case CHANGE_TIRES:

                    for(Tire tires : ps.getTireOrder()){
                        if(!tires.equals(currentState.getTireModel())){
                            projectedState = currentState.changeTires(tires);

                            vNext = currentSet.get(projectedState);
                            rSet = allSet.get(currentState);

                            res = rSet + ps.getDiscountFactor()*vNext;
                            max = max > res ? max : res;
                        }
                    }

                    break;
                case ADD_FUEL:

                    projectedState = currentState.addFuel(10); // add fuel where cost is 1 time unit

                    vNext = currentSet.get(projectedState);
                    rSet = allSet.get(currentState);

                    res = rSet + ps.getDiscountFactor()*vNext;
                    max = max > res ? max : res;

                    break;
                case CHANGE_PRESSURE:

                    for(TirePressure tp : TirePressure.values()){
                        if(!tp.equals(currentState.getTirePressure())){
                            projectedState = currentState.changeTirePressure(tp);

                            vNext = currentSet.get(projectedState);
                            rSet = allSet.get(currentState);

                            res = rSet + ps.getDiscountFactor()*vNext;
                            max = max > res ? max : res;
                        }
                    }

                    break;
                case CHANGE_CAR_AND_DRIVER:

                    for(String car : ps.getCarOrder()){
                        for(String driver : ps.getDriverOrder()){
                            if(!car.equals(currentState.getCarType()) &&
                                !driver.equals(currentState.getDriver())){
                                projectedState = currentState.changeCarAndDriver(car, driver);

                                vNext = currentSet.get(projectedState);
                                rSet = allSet.get(currentState);

                                res = rSet + ps.getDiscountFactor()*vNext;
                                max = max > res ? max : res;
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

        return max;
    }

    private static void run(ProblemSpec ps, String output){

        Simulator simulator = new Simulator(ps, output);
        State currentState  = simulator.reset();

        HashMap<State, Double> allStates = genStates(ps);   // that R(s) good shit

        HashMap<State, Double> current = (HashMap<State, Double>) allStates.clone();
        HashMap<State, Double> next = new HashMap();

        int hardStop = 0;

        long startTime = System.nanoTime();
        while(hardStop < 5){    // this should be set till convergence

            for(State state : current.keySet()){
                next.put(state, valueIterate(ps, state, current, allStates));
            }

            current = (HashMap<State, Double>) next.clone();
            next.clear();

            System.out.println("iteration: " + hardStop);
            System.out.println("value of current state: " + current.get(currentState));
            hardStop++;
        }

        System.out.println(current);

        long endTime = System.nanoTime();
        System.out.println("time: " + (endTime - startTime)/1000000);

//        State currentState  = simulator.reset();
//
//        while(!simulator.isGoalState(currentState)){
//
//            Map<Action, Double> res = getNextStates(ps, currentState);
//
//            double best = -10;
//            Action winner = null;
//
//            for(Map.Entry<Action, Double> entry : res.entrySet()){
////                System.out.println(entry.getKey().getText());
////                System.out.println(entry.getValue());
//                if(entry.getValue() > best){
//                    best = entry.getValue();
//                    winner = entry.getKey();
//                }
//            }
//
////            System.out.println(winner);
//
//            currentState = simulator.step(winner);
////            System.out.println(winner.getText());
////            System.out.println(best);
////
////            System.out.println(currentState);
//
//        }
    }

    public static void main(String[] args) {

        ProblemSpec ps;
        try {
            ps = new ProblemSpec("examples/level_1/input_lvl1.txt");
            run(ps, "outputs/test.txt");
//            System.out.println(ps.toString());
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");
    }
}
