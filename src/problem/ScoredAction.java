package problem;

public class ScoredAction{

    private Action action;
    private double score;

    public ScoredAction(Action action,double score){
        this.action = action;
        this.score = score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void setAction(Action action){
        this.action = action;
    }

    public double getScore(){
        return this.score;
    }

    public Action getAction(){
        return this.action;
    }
}
