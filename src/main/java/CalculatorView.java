import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

public class CalculatorView extends Application {
    private Calculatrice modele;
    private int WIDTH, HEIGHT;
    private Color[] colors = new Color[]{ Color.web("dfdfdf"), Color.web("f0f0f0") };
    private int colorsIndex = 0;
    private VBox stackDisplay;

    public void start(Stage primaryStage) throws Exception {
        WIDTH = 450; HEIGHT = 700;
        modele = new Calculatrice();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(HEIGHT * 0.95);
        stackDisplay = createStackDisplay();
        scroll.setContent(stackDisplay);

        TextField textInput = new TextField();
        textInput.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent ke)
            {
                if (ke.getCode().equals(KeyCode.ENTER) && textInput.getText().length() > 0)
                {
                    // On tente d'ajouter le contenu du text dans la pile
                    try {
                        modele.addStringToStack(textInput.getText());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    textInput.setText("");
                    // Puis à partir de la pile du modèle, on re affiche tout les élements
                    printStack();
                    scroll.setVvalue(1D);
                }
            }
        });
        textInput.setPrefSize(WIDTH,HEIGHT * .05);

        primaryStage.setTitle("Calculatrice");
        VBox root = new VBox();
        root.getChildren().addAll(scroll, textInput);
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void printStack(){
        colorsIndex = 0;
        int i = 0;
        // D'abord on clear la pile
        stackDisplay.getChildren().clear();


        for(String str : modele.stackToString()){
            stackDisplay.getChildren().add(createStackItem(str,colors[colorsIndex],i++));
            colorsIndex = (colorsIndex + 1) % 2;
        }
    }

    private VBox createStackDisplay(){
        VBox stackDisplay = new VBox();
        stackDisplay.setMaxHeight(HEIGHT * .8);
        return stackDisplay;
    }

    public void setModele(Calculatrice modele){
        this.modele = modele;
    }

    private StackPane createStackItem(String s, Color c,int index){
        Text t = new Text(s);
        t.setFont(Font.font ("Verdana", 12));

        StackPane stackItem = new StackPane();
        stackItem.setBackground(new Background(new BackgroundFill(c,CornerRadii.EMPTY, Insets.EMPTY)));

        stackItem.setPrefSize(WIDTH , HEIGHT * .04);
        stackItem.setAlignment(Pos.CENTER_LEFT);

        Button maj = new Button("Mise à jour");
        maj.setOnAction(actionEvent -> {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Mise à jour");
            dialog.setHeaderText("Mise à jour de la valeur");
            dialog.setContentText("Entrez une valeur:");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(name ->
                    modele.updateValue(index, Calculatrice.TypeParser.parse(result.get()).getValue()));

            printStack();
        });

        BorderPane bPane = new BorderPane();
        bPane.setLeft(t);
        BorderPane.setAlignment(t,Pos.CENTER_LEFT);
        bPane.setRight(maj);

        for(Node n : bPane.getChildren()) BorderPane.setMargin(n,new Insets(10));

        stackItem.getChildren().addAll(bPane);
        return stackItem;
    }
}
