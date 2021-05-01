package entity.logic.action;

import entity.Ant;
import openGL.world.World;

import java.util.Random;

public abstract class Action {

    protected boolean isConditional = false;

    public boolean isConditional() {
        return isConditional;
    }

    public abstract void execute(Ant a, World world);

    public boolean isConditionSatisfied(Ant a, World world) {
        return true;
    }

    public boolean equals(Object o) {
        if (o instanceof Action) {
            return o.toString().equals(toString());
        }
        return false;
    }

    public static Action getRandomAction() {
        Random random = new Random();
        int val = random.nextInt(10);
        switch (val) {
            case 1:
                return new ActionRight();
            case 2:
                return new ActionLeft();
            case 3:
                return new ActionGet();
            case 4:
                return new ActionPut();
            case 5:
                return new ActionRandom();
            case 6:
                return new ActionBackHome();
            case 7:
                return new ActionCondAntHill();
            case 8:
                return new ActionCondFood();
            case 9:
                return new ActionCondLoaded();
            default:
                return new ActionForward();
        }
    }

    public static Action getRandomSimpleAction() {
        Random random = new Random();
        int val = random.nextInt(7);
        switch (val) {
            case 1:
                return new ActionRight();
            case 2:
                return new ActionLeft();
            case 3:
                return new ActionGet();
            case 4:
                return new ActionPut();
            case 5:
                return new ActionRandom();
            case 6:
                return new ActionBackHome();
            default:
                return new ActionForward();
        }
    }

    public static Action getRandomConditionalAction() {
        Random random = new Random();
        int val = random.nextInt(3);
        switch (val) {
            case 1:
                return new ActionCondAntHill();
            case 2:
                return new ActionCondFood();
            default:
                return new ActionCondLoaded();
        }
    }

    public static Action getAction(String name) {
        switch (name) {
            case "right":
                return new ActionRight();
            case "left":
                return new ActionLeft();
            case "get":
                return new ActionGet();
            case "put":
                return new ActionPut();
            case "random":
                return new ActionRandom();
            case "home":
                return new ActionBackHome();
            case "anthill":
                return new ActionCondAntHill();
            case "food":
                return new ActionCondFood();
            case "isLoaded":
                return new ActionCondLoaded();
            default:
                return new ActionForward();
        }
    }
}
