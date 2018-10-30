package problem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import simulator.*;

public class Main {

    /**
     * Iterate through the ActionSpace (a) to find the most optimal (arg max) action to take
     * - movement option 1
     * - change car option CT
     * - change drvier option DT
     * - change tire option NT
     * - change pressure option 3
     * - refuel option !?
     */
    private static Map<Action, Double> getNextStates(ProblemSpec ps, State currentState){

        Map<Action, Double> nextStates = new HashMap<>();

        State projectedState;

        Level level = ps.getLevel();

        for(ActionType at : level.getAvailableActions()){
            switch(at.getActionNo()) {
                case 1:
                    /**
                     * movement option - take the current state score
                     */

                    Action move = new Action(ActionType.MOVE);

                    if(Util.checkMoveCondition(ps, currentState)) {
                        nextStates.put(move, Util.getMovementScore(ps, currentState));

                    } else {
                        nextStates.put(move, -10.0); // might set to be worse
                    }

                    break;
                case 2:
                    /**
                     * change car option - take the resulting state
                     * - assumes that it is always possible to change car type without fail
                     */

                    for(String car : ps.getCarOrder()){
                        if(!car.equals(currentState.getCarType())){
                            projectedState = currentState.changeCarType(car);

                            Action cc = new Action(ActionType.CHANGE_CAR, car);
                            nextStates.put(cc, Util.getMovementScore(ps, projectedState) - 1);
                        }
                    }

                    break;
                case 3:
                    /**
                     * change driver option - take the resulting state
                     * - assumes that it is always possible to change driver type without fail
                     */

                    for(String driver : ps.getDriverOrder()){
                        if(!driver.equals(currentState.getDriver())){
                            projectedState = currentState.changeDriver(driver);

                            Action cd = new Action(ActionType.CHANGE_DRIVER, driver);
                            nextStates.put(cd, Util.getMovementScore(ps, projectedState) - 1);
                        }
                    }

                    break;
                case 4:
                    /**
                     * change tires option - take the resulting state
                     * - assumes that it is always possible to change tires type without fail
                     */

                    for(Tire tire : ps.getTireOrder()){
                        if(!tire.equals(currentState.getTireModel())){
                            projectedState = currentState.changeTires(tire);

                            Action ct = new Action(ActionType.CHANGE_TIRES, tire);
                            nextStates.put(ct, Util.getMovementScore(ps, projectedState) - 1);
                        }
                    }

                    break;
                case 5:
                    /**
                     * add fuel option - take the resulting state
                     * - assumes that it is always possible to add x amount of fuel without fail
                     * - here the system has an infinite number of possibilities
                     * - in this implementation only fill to amount of fuel
                     *   required for the current (move) step
                     */

                    int fuelRequired = Util.getFuelConsumption(currentState, ps);
                    int currentFuel = currentState.getFuel();

                    Action rf;

                    if(fuelRequired > currentFuel){
                        int fill = fuelRequired - currentFuel;
                        int stepsRequired = (int) Math.ceil(fill / 10);

                        projectedState = currentState.addFuel(fill);

                        rf = new Action(ActionType.ADD_FUEL, fill);
                        nextStates.put(rf, Util.getMovementScore(ps, projectedState) - stepsRequired);

                    }else{
                        rf = new Action(ActionType.ADD_FUEL, 0);
                        nextStates.put(rf, -10.0);
                    }

                    break;
                case 6:
                    /**
                     * change pressure option - take the resulting state
                     * - assumes that it is always possible to change tires type without fail
                     */

                    for(TirePressure tp : TirePressure.values()){
                        if(!tp.equals(currentState.getTirePressure())) {
                            projectedState = currentState.changeTirePressure(tp);

                            Action cp = new Action(ActionType.CHANGE_PRESSURE, tp);
                            nextStates.put(cp, Util.getMovementScore(ps, projectedState) - 1);
                        }
                    }
                    break;
                case 7:
                    // perform action 2 and action 3 -- plain solution just iterate through both lists
                    // ...assume lists are relatively small and time complexity won't be heavily impacted

                    for(String car : ps.getCarOrder()){
                        for(String driver : ps.getDriverOrder()){

                            if(!driver.equals(currentState.getDriver()) &&
                                    !car.equals(currentState.getCarType())){

                                projectedState = currentState.changeCarAndDriver(car, driver);

                                Action ccd = new Action(ActionType.CHANGE_CAR_AND_DRIVER, car, driver);
                                nextStates.put(ccd, Util.getMovementScore(ps, projectedState) - 1);
                            }
                        }
                    }

                    break;
            }
        }

        return nextStates;
    }

    private static void run(ProblemSpec ps, String output){

        Simulator simulator = new Simulator(ps, output);

        State currentState  = simulator.reset();

        while(!simulator.isGoalState(currentState)){

            Map<Action, Double> res = getNextStates(ps, currentState);

            double best = -10;
            Action winner = null;

            for(Map.Entry<Action, Double> entry : res.entrySet()){
//                System.out.println(entry.getKey().getText());
//                System.out.println(entry.getValue());
                if(entry.getValue() > best){
                    best = entry.getValue();
                    winner = entry.getKey();
                }
            }

//            System.out.println(winner);

            currentState = simulator.step(winner);
            System.out.println(winner.getText());
            System.out.println(best);

            System.out.println(currentState);

        }
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
