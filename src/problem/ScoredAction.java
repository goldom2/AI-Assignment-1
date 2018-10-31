package problem;

public class ScoredAction{

    private Action action;
    private double score;

    public ScoredAction(Action action,double score){
        this.action = action;
        this.score = score;
    }

    public double getScore(){
        return this.score;
    }

    public Action getAction(){
        return this.action;
    }

    @Override
    public String toString() {
        return this.action.getText() + " -> " + this.score;
    }
}
