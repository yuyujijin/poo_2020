import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    private final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(new NumberAxis(), new NumberAxis());

    // Les couleurs pour les cases du tableaux
    private final static Color[] colors = new Color[]{ Color.web("dfdfdf"), Color.web("f0f0f0") };
    private static Map<Class,Color> classColors = new HashMap<>();

    public CalculatriceView(CalculatriceController c){
        // On definit la taille, et on créer un nouveau modèle
        WIDTH = 900; HEIGHT = 700;
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
        varScroll.setPrefHeight(HEIGHT * 0.50);

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
                    } catch (Exception e) {
                        alert(e.toString());
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

        lineChart.setPrefSize(WIDTH * 0.3,HEIGHT * 0.5);

        VBox leftPane = new VBox();
        leftPane.getChildren().addAll(varScroll,lineChart);

        // On attribut les tailles en largeur
        midPane.setPrefWidth(WIDTH * .32);
        histScroll.setPrefWidth(WIDTH * .36);
        varScroll.setPrefWidth(WIDTH * .32);


        // Puis on créer la box central
        setTitle("Calculatrice");
        HBox root = new HBox();
        root.getChildren().addAll(leftPane,midPane,histScroll);

        setScene(new Scene(root, WIDTH, HEIGHT));
        show();

        // On update un coup la vue
        printStack(stackDisplay,stackScroll,controller.getStackToArray());
        printHistorique(controller.getHistToArray());
        printVar(controller.getVarToArray());
    }

    // Met a jour le graph
    private void updateGraph(Function<Integer,Integer> f, String s){
        lineChart.getData().clear();
        int range = 20;
        XYChart.Series<Number,Number> chart = new XYChart.Series<>();
        for(Integer x = -range; x < range; x += 1){
            try{
                chart.getData().add(new XYChart.Data<>(x,f.apply(x)));
            }catch(ArithmeticException e){
            }
        }
        lineChart.setTitle(s);
        lineChart.getData().add(chart);
    }

    // Permet d'afficher une alerte (utilisé pour les exception)
    private void alert(String s) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Exception durant l'execution");
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }

    // On y ajoute les différentes couleurs pour les différents types
    private void putMapColors(){
        classColors.put(Integer.class,Color.web("8ea0ab"));
        classColors.put(Boolean.class,Color.web("d17979"));
        classColors.put(Fraction.class,Color.web("7bb165"));
        classColors.put(Ensemble.class,Color.web("6fb0bb"));
    }

    // Permet de créer les labels en haut des differentes parties ("VARIABLES","PILE","HISTORIQUE")
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
        // D'abord on clear l'historique'
        historiqueDisplay.getChildren().clear();
        historiqueDisplay.getChildren().add(createStackLabel("HISTORIQUE",historiqueDisplay.getWidth(),30, Color.web("bf7f7f")));

        for(Object op : tokens){
            historiqueDisplay.getChildren().add(createHistItem(op,colors[colorsIndex],i++));
            colorsIndex = (colorsIndex + 1) % 2;
        }

        // A chaque réaffichage on tente d'update le graph avec le sommet de l'historique
        if(tokens.length > 0) updateGraphFromObj(tokens[i - 1]);
        // Pour scroller tout en bas
        histScroll.getParent().layout();
        histScroll.setVvalue(1.0d);
    }

    private void printVar(Map.Entry<String,Token.RecallToken>[] tokens){
        int colorsIndex = 0;
        // D'abord on clear les vars
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
        // Créer un item de l'historique (TextField permettant de modifier la variable)
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
                        alert(e.toString());
                    }
                    printHistorique(controller.getHistToArray());
                    printVar(controller.getVarToArray());
                }
            }
        });
        return createCollectionItem(t,op,c);
    }

    private StackPane createStackItem(Object op, Color c){
        // Créer un item de la pile (Text simple)
        Text t = new Text(op.toString());
        t.setFont(Font.font("Verdana", 12));

        return createCollectionItem(t,op,c);
    }

    private StackPane createVarItem(Map.Entry<String, Token.RecallToken> op, Color c){
        // Créer un item des variables (TextField permettant de modifier la variable)
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
                        alert(e.toString());
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

        // Le type de l'objet stocké
        Text typeText = new Text((op instanceof Token)? ((Token) op).valueClassToString() : op.getClass().getSimpleName());
        typeText.setFont(Font.font ("Verdana", FontWeight.BLACK.BOLD, 12));
        typeText.setFill(Color.WHITE);
        type.getChildren().add(typeText);

        // Le bloc de droite (pour type + icone graph (si graphable))
        HBox rightPart = new HBox(10);
        rightPart.getChildren().addAll(type);

        // Une pane pour la partie de gauche
        BorderPane bPane = new BorderPane();
        bPane.setLeft(text);
        BorderPane.setAlignment(text,Pos.CENTER_LEFT);
        bPane.setRight(rightPart);

        for(Node n : bPane.getChildren()) BorderPane.setMargin(n,new Insets(10));

        stackItem.getChildren().addAll(bPane);
        graphItemAction(op,stackItem,c,rightPart);
        return stackItem;
    }

    private void graphItemAction(Object op, StackPane item, Color c, HBox rightPart){
        // Une opération retournant comme valeur un entier
        if(op instanceof Token.OperationToken && ((Token.OperationToken) op).getValue() instanceof Integer){
            // Lorsque l'on clique, on essaie d'afficher un graph
            item.setOnMouseClicked(event -> updateGraphFromObj(op));
            // On récupère les 2 élements avant le token
            Token[] hist = controller.getHistToArray();
            for(int i = 0; i < hist.length; i++){
                if(hist[i] == op){
                    Token t2 = hist[i - 1];
                    Token t1 = hist[i - 2];
                    // Si t1 est une variable libre, et t2 une variable liée ou l'inverse
                    if ((!(t1 instanceof Token.OperandToken) && t2 instanceof Token.OperandToken)
                                || !(t2 instanceof Token.OperandToken) && t1 instanceof Token.OperandToken) {
                        // On affiche une image de graph
                        InputStream is = getClass().getClassLoader().getResourceAsStream("graph.png");
                        ImageView imageView = new ImageView( new Image(is));
                        imageView.setFitWidth(15); imageView.setFitHeight(15);

                        rightPart.getChildren().add(0,imageView);

                        // Et un effet d'hover
                        item.setOnMouseEntered(event -> {
                            item.setBackground(new Background(new BackgroundFill(Color.web("c6c6c6"), CornerRadii.EMPTY, Insets.EMPTY)));
                        });
                        item.setOnMouseExited(event -> {
                            item.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
                        });
                    }
                }
            }
        }
    }

    private void updateGraphFromObj(Object op){
        if(op instanceof Token.OperationToken && ((Token.OperationToken) op).getValue() instanceof Integer){
            // On récupère les 2 derniers éléments avant 'op' dans l'historique
            // et si l'un (et seulement un) des deux est un token d'opérande (une valeur fixée), alors on graphe
            Token[] hist = controller.getHistToArray();
            for(int i = 0; i < hist.length; i++){
                if(hist[i] == op){
                    Token t2 = hist[i - 1];
                    Token t1 = hist[i - 2];
                    // t1 variable libre, t2 variable liée
                    if (!(t1 instanceof Token.OperandToken) && t2 instanceof Token.OperandToken) {
                        updateGraph(x -> (Integer) ((Token.OperationToken) op).getOperation()
                                        .compute(new Integer[]{(Integer) t2.getValue(), x}),
                                "x " + ((Token.OperationToken) op).getOperationName() + " " + t2.getValue());
                        // l'inverse
                    } else if (!(t2 instanceof Token.OperandToken) && t1 instanceof Token.OperandToken) {
                        updateGraph(x -> (Integer) ((Token.OperationToken) op).getOperation()
                                        .compute(new Integer[]{x, (Integer) t1.getValue()}),
                                t1.getValue() + " " + ((Token.OperationToken) op).getOperationName() + " x");
                    }
                }
            }
        }
    }
}
