import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Map;

public final class CalculatriceController extends Application {
    private Calculatrice calculatrice;

    public Object[] getStackToArray(){
        return calculatrice.stackToArray();
    }

    public Token[] getHistToArray(){
        return calculatrice.histToArray();
    }

    public Map.Entry<String, Token.RecallToken>[] getVarToArray(){
        return calculatrice.varToArray();
    }

    public void updateValue(int i, String s){
        calculatrice.updateValue(i,s);
    }

    public void updateVar(String s, String v){
        calculatrice.updateVar(s,v);
    }

    public void addStringToStack(String s) throws IllegalArgumentException, IndexOutOfBoundsException {
        calculatrice.addStringToStack(s);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        calculatrice = new Calculatrice();
        CalculatriceView calculatorView = new CalculatriceView(this);
    }
}
