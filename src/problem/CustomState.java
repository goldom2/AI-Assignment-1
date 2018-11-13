package problem;



import simulator.State;

import java.util.Objects;

/**
 * An immutable class representing a state in the game changing technology
 * environment, defined by:
 *
 * - position of the car (i.e. the cell index)
 * - whether the car is in slip condition
 * - whether the car is in breakdown condition
 * - car type
 * - car fuel
 * - car tire pressure
 * - the driver
 * - tire model
 */
public class CustomState {

    /** The position of the car in terms of grid cell in environment **/
    private int pos;
    /** Whether the car is in a slip condition **/
    private boolean slip;
    /** Whether the car is broken down **/
    private boolean breakdown;
    /** The car type **/
    private String carType;
    /** Fuel remaining **/
    private int fuel;
    /** Tire pressure **/
    private TirePressure tirePressure;
    /** The driver **/
    private String driver;
    /** The tire model **/
    private Tire tireModel;

    /**
     * Construct a new state with the given parameter values
     *
     * @param pos cell index of car in environment
     * @param slip if car is in slip condition
     * @param breakdown if car is in breakdown condition
     * @param carType the type of car
     * @param fuel the amount of fuel
     *        (assumes ProblemSpec.FUEL_MIN <= fuel <= ProblemSpec.FUEL_MAX)
     * @param tirePressure the pressure of tires
     * @param driver the driver
     * @param tireModel the model of the tires
     */
    public CustomState(int pos, boolean slip, boolean breakdown, String carType,
                 int fuel, TirePressure tirePressure, String driver, Tire tireModel) {
        this.pos = pos;
        this.slip = slip;
        this.breakdown = breakdown;
        this.carType = carType;
        this.fuel = fuel;
        this.tirePressure = tirePressure;
        this.driver = driver;
        this.tireModel = tireModel;
    }

    public CustomState(State state) {
        this.pos = state.getPos();
        this.slip = state.isInSlipCondition();
        this.breakdown = state.isInBreakdownCondition();
        this.carType = state.getCarType();
        this.fuel = state.getFuel();
        this.tirePressure = state.getTirePressure();
        this.driver = state.getDriver();
        this.tireModel = state.getTireModel();
    }

    public State returnState(){
        return new State(pos, slip, breakdown, carType, fuel, tirePressure, driver, tireModel);
    }


    /**
     * Get the initial state of the problem
     *
     * @param carType the initial car type
     * @param driver the initial driver
     * @param tire the initial tire model
     * @return the initial state
     */
    public static CustomState getStartState(String carType, String driver,
                                      Tire tire) {
        // position is not 0 indexed, as per assignment spec
        return new CustomState(1, false, false, carType, ProblemSpec.FUEL_MAX,
                TirePressure.ONE_HUNDRED_PERCENT, driver, tire);
    }

    /**
     * Return the next state after moving the cars position from current state.
     *
     * N.B. move distance should be in range:
     *      [ProblemSpec.CAR_MIN_MOVE, ProblemSpec.CAR_MAX_MOVE]
     *
     * @param move the distance to move
     * @param N the max position (i.e. the goal region)
     * @return the next state
     */
    public CustomState changePosition(int move, int N) {
        CustomState nexState = copyState();
        if (nexState.pos + move > N) {
            nexState.pos = N;
        } else if (nexState.pos + move < 1) {
            // not zero indexed as per assignment spec
            nexState.pos = 1;
        } else {
            nexState.pos += move;
        }
        return nexState;
    }

    /**
     * Return the next state after changing the slip condition of current state
     *
     * @param newSlip the new slip condition
     * @return the next state
     */
    public CustomState changeSlipCondition(boolean newSlip) {
        CustomState nextState = copyState();
        nextState.slip = newSlip;
        return nextState;
    }

    /**
     * Return the next state after changing the breakdown condition of current state
     *
     * @param newBreakdown the new breakdown condition
     * @return the next state
     */
    public CustomState changeBreakdownCondition(boolean newBreakdown) {
        CustomState nextState = copyState();
        nextState.breakdown = newBreakdown;
        return nextState;
    }

    /**
     * Return the next state after changing car type in current state.
     *
     * @param newCarType the new car type
     * @return the next state
     */
    public CustomState changeCarType(String newCarType) {
        CustomState nextState = copyState();
        nextState.carType = newCarType;
        nextState.fuel = ProblemSpec.FUEL_MAX;
        nextState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
        return nextState;
    }

    /**
     * Return the next state after changing the driver in current state.
     *
     * @param newDriver the new driver
     * @return the next state
     */
    public CustomState changeDriver(String newDriver) {
        CustomState nextState = copyState();
        nextState.driver = newDriver;
        return nextState;
    }

    /**
     * Return the next state after changing the tire model in current state.
     *
     * @param newTire the new tire model
     * @return the next state
     */
    public CustomState changeTires(Tire newTire) {
        CustomState nextState = copyState();
        nextState.tireModel = newTire;
        nextState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
        return nextState;
    }

    /**
     * Return the next state after adding fuel to current state. Amount of fuel
     * in a state is capped at ProblemSpec.FUEL_MAX
     *
     * @param fuelToAdd amount of fuel to add
     * @return the next state
     */
    public CustomState addFuel(int fuelToAdd) {
        if (fuelToAdd < 0) {
            throw new IllegalArgumentException("Fuel to add must be positive");
        }

        CustomState nextState = copyState();
        if (nextState.fuel + fuelToAdd > ProblemSpec.FUEL_MAX) {
            nextState.fuel = ProblemSpec.FUEL_MAX;
        } else {
            nextState.fuel += fuelToAdd;
        }
        return nextState;
    }

    /**
     * Return the next state after consuming fuel in current state.
     *
     * @param fuelConsumed amount of fuel consumed
     * @return the next state
     */
    public CustomState consumeFuel(int fuelConsumed) {
        if (fuelConsumed < 0) {
            throw new IllegalArgumentException("Fuel consumed must be positive");
        }
        CustomState nextState = copyState();
        nextState.fuel -= fuelConsumed;
        if (nextState.fuel < ProblemSpec.FUEL_MIN) {
            throw new IllegalArgumentException("Too much fuel consumed: "
                    + fuelConsumed);
        }
        return nextState;
    }

    /**
     * Return the next state after changing the tire pressure in current state.
     *
     * @param newTirePressure the new tire pressure
     * @return the next state
     */
    public CustomState changeTirePressure(TirePressure newTirePressure) {
        CustomState nextState = copyState();
        nextState.tirePressure = newTirePressure;
        return nextState;
    }

    /**
     * Return the next state after changing the car type and the driver in
     * current state.
     *
     * @param newCarType the new car type
     * @param newDriver the new driver
     * @return the next state
     */
    public CustomState changeCarAndDriver(String newCarType, String newDriver) {
        CustomState nextState = copyState();
        nextState.carType = newCarType;
        nextState.driver = newDriver;
        nextState.fuel = ProblemSpec.FUEL_MAX;
        nextState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
        return nextState;
    }

    /**
     * Return the next state after changing the tire model, adding fuel and
     * changing the tire pressure
     *
     * @param newTireModel new tire model
     * @param fuelToAdd the amount of fuel to add
     * @param newTirePressure the new tire pressure
     * @return the next state
     */
    public CustomState changeTireFuelAndTirePressure(Tire newTireModel, int fuelToAdd,
                                               TirePressure newTirePressure) {
        CustomState nextState = addFuel(fuelToAdd);
        nextState.tireModel = newTireModel;
        nextState.tirePressure = newTirePressure;
        return nextState;
    }

    /**
     * Copy this state, returning a deep copy
     *
     * @return deep copy of current state
     */
    public CustomState copyState() {
        return new CustomState(pos, slip, breakdown, carType, fuel, tirePressure, driver,
                tireModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomState state = (CustomState) o;
        return pos == state.pos &&
                slip == state.slip &&
                breakdown == state.breakdown &&
                fuel == state.fuel &&
                Objects.equals(carType, state.carType) &&
                tirePressure == state.tirePressure &&
                Objects.equals(driver, state.driver) &&
                tireModel == state.tireModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, slip, breakdown, carType, fuel, tirePressure, driver, tireModel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State: [ ");
        sb.append("Pos=").append(pos).append(" | ");
        sb.append("Car=").append(carType).append(" | ");
        sb.append("Driver=").append(driver).append(" | ");
        sb.append("Tire=").append(tireModel.toString()).append(" | ");
        sb.append("Pressure=").append(tirePressure.asString()).append(" | ");
        sb.append("Fuel=").append(fuel).append(" ]\n");
        return sb.toString();
    }

    public int getPos() {
        return pos;
    }

    public boolean isInSlipCondition() {
        return slip;
    }

    public boolean isInBreakdownCondition() {
        return breakdown;
    }

    public String getCarType() {
        return carType;
    }

    public int getFuel() {
        return fuel;
    }

    public TirePressure getTirePressure() {
        return tirePressure;
    }

    public String getDriver() {
        return driver;
    }

    public Tire getTireModel() {
        return tireModel;
    }
}
