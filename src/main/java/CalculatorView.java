import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.util.HashSet;

public final class CalculatorView extends Application {
    private Calculatrice modele;
    private int WIDTH, HEIGHT;
    private Color[] colors = new Color[]{ Color.web("dfdfdf"), Color.web("f0f0f0") };
    private int colorsIndex = 0;

    public void start(Stage primaryStage) throws Exception {
        // On definit la taille, et on créer un nouveau modèle
        WIDTH = 850; HEIGHT = 700;
        modele = new Calculatrice();

        // On créer les 3 contenaires pour les variables, la pile & l'invitée de commande et enfin l'historique
        ScrollPane stackScroll = new ScrollPane();
        stackScroll.setFitToWidth(true);
        stackScroll.setPrefHeight(HEIGHT * 0.4);

        ScrollPane histScroll = new ScrollPane();
        histScroll.setFitToWidth(true);

        ScrollPane varScroll = new ScrollPane();
        varScroll.setFitToWidth(true);

        // Puis on créer les 3 VBox pour afficher verticalement les valeurs dedans
        VBox stackDisplay = createStackDisplay(HEIGHT *  .8);
        VBox historiqueDisplay = createStackDisplay(HEIGHT);
        VBox varDisplay = createStackDisplay(HEIGHT);

        stackScroll.setContent(stackDisplay);
        histScroll.setContent(historiqueDisplay);
        varScroll.setContent(varDisplay);

        // On créer les bouttons
        BorderPane buttons = createButtonDisplay();

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
                    // TODO : passer par un controller
                    try {
                        modele.addStringToStack(textInput.getText());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // On vide l'entrée
                    textInput.setText("");
                    // Puis à partir de la pile du modèle, on re affiche tout les élements
                    printStack(stackDisplay,stackScroll,modele.stackToArray(),false);
                    printStack(historiqueDisplay,histScroll,modele.histToArray(),true);
                }
            }
        });
        textInput.setPrefHeight(HEIGHT * .05);

        // On créer le conteneur central, contenant la pile, les bouttons et l'entrée clavier
        VBox midPane = new VBox();
        midPane.getChildren().addAll(stackScroll, buttons, textInput);

        // On attribut les tailles en largeur
        midPane.setPrefWidth(WIDTH * .32);
        histScroll.setPrefWidth(WIDTH * .36);
        varScroll.setPrefWidth(WIDTH * .32);

        // Puis on créer la box central
        primaryStage.setTitle("Calculatrice");
        HBox root = new HBox();
        root.getChildren().addAll(varScroll,midPane,histScroll);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    // printStack réupdate l'affichage
    private void printStack(VBox display, ScrollPane scroll, Object[] Tokens, boolean modifiable){
        colorsIndex = 0;
        int i = 0;
        // D'abord on clear la pile
        display.getChildren().clear();

        for(Object op : Tokens){
            display.getChildren().add(createStackItem(op,colors[colorsIndex],i++,modifiable));
            colorsIndex = (colorsIndex + 1) % 2;
        }

        // Pour scroller tout en bas
        scroll.getParent().layout();
        scroll.setVvalue(1.0d);
    }

    private VBox createStackDisplay(double height){
        VBox stackDisplay = new VBox();
        stackDisplay.setMaxHeight(height);
        return stackDisplay;
    }

    private BorderPane createButtonDisplay(){
        BorderPane pane = new BorderPane();
        pane.setPrefSize(WIDTH,HEIGHT * 0.55);

        MenuBar mb = new MenuBar();
        Menu menu = new Menu("Select a type");

        HashSet<Class> s = modele.getAvailableTypes();
        for(Class c : s){
            MenuItem menuItem = new MenuItem(c.getSimpleName());
            menu.getItems().add(menuItem);
        }
        mb.getMenus().addAll(menu);
        pane.setTop(mb);
        return pane;
    }

    private StackPane createStackItem(Object op, Color c,int index, boolean modifiable){
        Node t;
        if(op instanceof Token.OperationToken || !modifiable) {
            t = new Text(op.toString());
            ((Text) t).setFont(Font.font("Verdana", 12));
        }else {
            t = new TextField(op.toString());
            ((TextField) t).setFont(Font.font("Verdana", 12));
            t.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent ke) {
                    if (ke.getCode().equals(KeyCode.ENTER) && ((TextField) t).getText().length() > 0) {
                        String value = ((TextField) t).getText();
                        try{
                            modele.updateValue(index, Calculatrice.TypeParser.parse(value));
                        }catch(Exception e){
                            System.out.println(e);
                        }
                        //printStack(historiqueDisplay,histScroll,modele.histToArray(),true);
                    }
                }
            });
        }

        StackPane stackItem = new StackPane();
        stackItem.setBackground(new Background(new BackgroundFill(c,CornerRadii.EMPTY, Insets.EMPTY)));

        stackItem.setPrefSize(WIDTH , HEIGHT * .04);
        stackItem.setAlignment(Pos.CENTER_LEFT);

        StackPane type = new StackPane();
        type.setBackground(new Background(new BackgroundFill(Color.RED,CornerRadii.EMPTY,Insets.EMPTY)));
        type.setPadding(new Insets(5,10,5,10));

        Text typeText = new Text((op instanceof Token)? ((Token) op).valueClassToString() : op.getClass().getSimpleName());
        typeText.setFont(Font.font ("Verdana", FontWeight.BLACK.BOLD, 12));
        typeText.setFill(Color.WHITE);
        type.getChildren().add(typeText);

        InputStream is = getClass().getClassLoader().getResourceAsStream("delete_img.png");
        ImageView imageView = new ImageView( new Image(is));
        imageView.setFitWidth(15); imageView.setFitHeight(15 );

        HBox rightPart = new HBox(10);
        rightPart.getChildren().add(type);

        BorderPane bPane = new BorderPane();
        bPane.setLeft(t);
        BorderPane.setAlignment(t,Pos.CENTER_LEFT);
        bPane.setRight(rightPart);

        for(Node n : bPane.getChildren()) BorderPane.setMargin(n,new Insets(10));

        stackItem.getChildren().addAll(bPane);
        return stackItem;
    }
}
