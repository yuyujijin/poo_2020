import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public final class CalculatriceView extends Stage{
    // Le controlleur
    private final CalculatriceController controller;
    // Les dimensions
    private final int WIDTH, HEIGHT;

    //
    private ScrollPane histScroll;
    private VBox historiqueDisplay;
    private ScrollPane varScroll;
    private VBox varDisplay;

    // Les couleurs pour les cases du tableaux
    private final static Color[] colors = new Color[]{ Color.web("dfdfdf"), Color.web("f0f0f0") };
    private static Map<Class,Color> classColors = new HashMap<>();

    public CalculatriceView(CalculatriceController c){
        // On definit la taille, et on créer un nouveau modèle
        WIDTH = 850; HEIGHT = 700;
        controller = c;

        // On ajoute les couleurs
        putMapColors();

        // On créer les 3 contenaires pour les variables, la pile & l'invitée de commande et enfin l'historique
        ScrollPane stackScroll = new ScrollPane();
        stackScroll.setFitToWidth(true);
        stackScroll.setPrefHeight(HEIGHT * 0.95);

        histScroll = new ScrollPane();
        histScroll.setFitToWidth(true);

        varScroll = new ScrollPane();
        varScroll.setFitToWidth(true);

        // Puis on créer les 3 VBox pour afficher verticalement les valeurs dedans
        VBox stackDisplay = createStackDisplay(HEIGHT *  .95);
        historiqueDisplay = createStackDisplay(HEIGHT);
        varDisplay = createStackDisplay(HEIGHT);

        stackScroll.setContent(stackDisplay);
        histScroll.setContent(historiqueDisplay);
        varScroll.setContent(varDisplay);

        // Puis "l'invitée de commande", pour rentrer les données
        TextField textInput = new TextField();
        textInput.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent ke)
            {
                // Si on appuie sur 'ENTER' et que la longueur de la ligne > 0
                if (ke.getCode().equals(KeyCode.ENTER) && textInput.getText().length() > 0)
                {
                    // On tente d'ajouter le contenu du text dans la pile
                    try {
                        controller.addStringToStack(textInput.getText());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // On vide l'entrée
                    textInput.setText("");
                    // Puis à partir de la pile du modèle, on re affiche tout les élements
                    printStack(stackDisplay,stackScroll,controller.getStackToArray());
                    printHistorique(controller.getHistToArray());
                    printVar(controller.getVarToArray());
                }
            }
        });

        textInput.setPrefHeight(HEIGHT * .05);

        // On créer le conteneur central, contenant la pile, les bouttons et l'entrée clavier
        VBox midPane = new VBox();
        midPane.getChildren().addAll(stackScroll, textInput);

        // On attribut les tailles en largeur
        midPane.setPrefWidth(WIDTH * .32);
        histScroll.setPrefWidth(WIDTH * .36);
        varScroll.setPrefWidth(WIDTH * .32);

        // Puis on créer la box central
        setTitle("Calculatrice");
        HBox root = new HBox();
        root.getChildren().addAll(varScroll,midPane,histScroll);

        setScene(new Scene(root, WIDTH, HEIGHT));
        show();

        // On update un coup la vue
        printStack(stackDisplay,stackScroll,controller.getStackToArray());
        printHistorique(controller.getHistToArray());
        printVar(controller.getVarToArray());
    }

    private void putMapColors(){
        classColors.put(Integer.class,Color.web("8ea0ab"));
        classColors.put(Boolean.class,Color.web("d17979"));
        classColors.put(Fraction.class,Color.web("7bb165"));
    }

    private StackPane createStackLabel(String s, double w, double h, Color c){
        StackPane pane = new StackPane();
        pane.setPrefSize(w,h);
        pane.setBackground(new Background(new BackgroundFill(c,CornerRadii.EMPTY,Insets.EMPTY)));
        pane.setPadding(new Insets(5,10,5,10));

        Text typeText = new Text(s);
        typeText.setFont(Font.font ("Verdana", FontWeight.BLACK.BOLD, 12));
        typeText.setFill(Color.WHITE);
        pane.getChildren().add(typeText);
        return pane;
    }

    private void printStack(VBox display, ScrollPane scroll, Object[] tokens){
        int colorsIndex = 0;
        int i = 0;
        // D'abord on clear la pile
        display.getChildren().clear();
        display.getChildren().add(createStackLabel("PILE",display.getWidth(),30,Color.web("8baabf")));
        for(Object op : tokens){
            display.getChildren().add(createStackItem(op,colors[colorsIndex]));
            colorsIndex = (colorsIndex + 1) % 2;
        }

        // Pour scroller tout en bas
        scroll.getParent().layout();
        scroll.setVvalue(1.0d);
    }

    private void printHistorique(Token[] tokens){
        int colorsIndex = 0;
        int i = 0;
        // D'abord on clear la pile
        historiqueDisplay.getChildren().clear();
        historiqueDisplay.getChildren().add(createStackLabel("HISTORIQUE",historiqueDisplay.getWidth(),30, Color.web("bf7f7f")));

        for(Object op : tokens){
            historiqueDisplay.getChildren().add(createHistItem(op,colors[colorsIndex],i++));
            colorsIndex = (colorsIndex + 1) % 2;
        }

        // Pour scroller tout en bas
        histScroll.getParent().layout();
        histScroll.setVvalue(1.0d);
    }

    private void printVar(Map.Entry<String,Token.RecallToken>[] tokens){
        int colorsIndex = 0;
        // D'abord on clear la pile
        varDisplay.getChildren().clear();
        varDisplay.getChildren().add(createStackLabel("VARIABLES",varDisplay.getWidth(),30,Color.web("b78bbf")));

        for(Map.Entry<String,Token.RecallToken> op : tokens){
            varDisplay.getChildren().add(createVarItem(op,colors[colorsIndex]));
            colorsIndex = (colorsIndex + 1) % 2;
        }

        // Pour scroller tout en bas
        varScroll.getParent().layout();
        varScroll.setVvalue(1.0d);
    }

    private VBox createStackDisplay(double height){
        VBox stackDisplay = new VBox();
        stackDisplay.setMaxHeight(height);
        return stackDisplay;
    }
    private StackPane createHistItem(Object op, Color c,int index){
        TextField t = new TextField(op.toString());
        t.setFont(Font.font("Verdana", 12));
        t.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER) && t.getText().length() > 0) {
                    String value = t.getText();
                    try{
                        controller.updateValue(index, value);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    printHistorique(controller.getHistToArray());
                    printVar(controller.getVarToArray());
                }
            }
        });
        return createCollectionItem(t,op,c);
    }

    private StackPane createStackItem(Object op, Color c){
        Text t = new Text(op.toString());
        t.setFont(Font.font("Verdana", 12));

        return createCollectionItem(t,op,c);
    }

    private StackPane createVarItem(Map.Entry<String, Token.RecallToken> op, Color c){
        TextField t = new TextField(op.getValue().toString());
        t.setFont(Font.font("Verdana", 12));
        t.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER) && t.getText().length() > 0) {
                    String value = t.getText();
                    try{
                        controller.updateVar(op.getKey(),value);
                    }catch(Exception e){
                        System.out.println(e);
                    }
                    printHistorique(controller.getHistToArray());
                    printVar(controller.getVarToArray());
                }
            }
        });
        return createCollectionItem(t,op.getValue(),c);
    }

    private StackPane createCollectionItem(Node text, Object op, Color c){
        StackPane stackItem = new StackPane();
        stackItem.setBackground(new Background(new BackgroundFill(c,CornerRadii.EMPTY, Insets.EMPTY)));

        stackItem.setPrefSize(WIDTH , HEIGHT * .04);
        stackItem.setAlignment(Pos.CENTER_LEFT);

        StackPane type = new StackPane();
        Color classCol = classColors.get((op instanceof Token)? ((Token) op).getValue().getClass() : op.getClass());
        // Cas d'une couleur non definie (oublie)
        if(classCol == null) classCol = Color.web("a4a4a4");
        type.setBackground(new Background(new BackgroundFill(classCol,CornerRadii.EMPTY,Insets.EMPTY)));
        type.setPadding(new Insets(5,10,5,10));

        Text typeText = new Text((op instanceof Token)? ((Token) op).valueClassToString() : op.getClass().getSimpleName());
        typeText.setFont(Font.font ("Verdana", FontWeight.BLACK.BOLD, 12));
        typeText.setFill(Color.WHITE);
        type.getChildren().add(typeText);

        HBox rightPart = new HBox(10);
        rightPart.getChildren().add(type);

        BorderPane bPane = new BorderPane();
        bPane.setLeft(text);
        BorderPane.setAlignment(text,Pos.CENTER_LEFT);
        bPane.setRight(rightPart);

        for(Node n : bPane.getChildren()) BorderPane.setMargin(n,new Insets(10));

        stackItem.getChildren().addAll(bPane);
        return stackItem;
    }
}
