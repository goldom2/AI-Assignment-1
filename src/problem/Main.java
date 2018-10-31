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
            double res = 0;

            switch(actionType) {
                case MOVE:
                    double[] moveProbs = Util.getMoveProb(ps, currentState);

                    double sum = 0.0;
                    rSet = allSet.get(currentState).getScore();

                    for (int i = 0; i < moveProbs.length; i++) {

                        if(Util.checkMoveCondition(ps, currentState)) {
                            if(i < 10){

                                int reqFuel = Util.getFuelConsumption(currentState, ps);

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
                                            (double)(ps.getSlipRecoveryTime() - 1));
                                }
                            }
                        }
                    }

                    act = new Action(ActionType.MOVE);
                    res = rSet + ps.getDiscountFactor() * sum;

                    if(max <= res){
                        max = res;
                        ret = new ScoredAction(act, res);
                    }

                    break;

                case CHANGE_CAR:
                    for(String car : ps.getCarOrder()){
                        if(!car.equals(currentState.getCarType())){
                            projectedState = currentState.changeCarType(car);

                            vNext = currentSet.get(projectedState).getScore();
                            rSet = allSet.get(currentState).getScore();

                            res = rSet + ps.getDiscountFactor()*vNext;
                            act = new Action(ActionType.MOVE);

                            if(max <= res){
                                max = res;
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
                            act = new Action(ActionType.MOVE);

                            if(max <= res){
                                max = res;
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
                            act = new Action(ActionType.MOVE);

                            if(max <= res){
                                max = res;
                                ret = new ScoredAction(act, res);
                            }
                        }
                    }

                    break;
                case ADD_FUEL:

                    projectedState = currentState.addFuel(10); // add fuel where cost is 1 time unit

                    vNext = currentSet.get(projectedState).getScore();
                    rSet = allSet.get(currentState).getScore();

                    res = rSet + ps.getDiscountFactor()*vNext;
                    act = new Action(ActionType.MOVE);

                    if(max <= res){
                        max = res;
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
                            act = new Action(ActionType.MOVE);

                            if(max <= res){
                                max = res;
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
                                act = new Action(ActionType.MOVE);

                                if(max <= res){
                                    max = res;
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

        return ret;
    }

    private static void run(ProblemSpec ps, String output){

        Simulator sim = new Simulator(ps, output);

        HashMap<State, ScoredAction> allStates = genStates(ps);   // that R(s) good shit

        HashMap<State, ScoredAction> current = (HashMap<State, ScoredAction>) allStates.clone(); // v(s)
        HashMap<State, ScoredAction> next = new HashMap();

        boolean hasConverged = false;

        long startTime = System.nanoTime();
        while(!hasConverged){

            hasConverged = true;

            for(State state : current.keySet()){
                ScoredAction res = valueIterate(ps, state, current, allStates);

                double v = current.get(state).getScore();
                double vdash = res.getScore();

                next.put(state, res);

                if(hasConverged && (Math.abs(v - vdash) > 0.00001)){
                    System.out.println(v + " + " + vdash);
                    hasConverged = false;
                }
            }

            current = (HashMap<State, ScoredAction>) next.clone();
            next.clear();
        }

        long endTime = System.nanoTime();
        System.out.println("time: " + (endTime - startTime)/1000000);

        State start = sim.reset();  // initial state
        System.out.println(current.get(start));

        while(!sim.isGoalState(start)){

            ScoredAction step = current.get(start);
            start = sim.step(step.getAction());
        }
    }

    public static void main(String[] args) {

        ProblemSpec ps;
        try {
            ps = new ProblemSpec("examples/level_2/input_lvl2.txt");
            run(ps, "outputs/test.txt");
//            System.out.println(ps.toString());
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");
    }
}
