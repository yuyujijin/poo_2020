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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.text.Element;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.util.HashSet;
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
        scroll.setPrefHeight(HEIGHT * 0.4);
        stackDisplay = createStackDisplay();
        scroll.setContent(stackDisplay);

        BorderPane buttons = createButtonDisplay();

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
                    scroll.getParent().layout();
                    scroll.setVvalue(1D);
                }
            }
        });
        textInput.setPrefSize(WIDTH, HEIGHT * .05);

        primaryStage.setTitle("Calculatrice");
        VBox root = new VBox();
        root.getChildren().addAll(scroll, buttons, textInput);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void printStack(){
        colorsIndex = 0;
        int i = 0;
        // D'abord on clear la pile
        stackDisplay.getChildren().clear();


        for(Operande op : modele.stackToArray()){
            stackDisplay.getChildren().add(createStackItem(op,colors[colorsIndex],i++));
            colorsIndex = (colorsIndex + 1) % 2;
        }
    }

    private VBox createStackDisplay(){
        VBox stackDisplay = new VBox();
        stackDisplay.setMaxHeight(HEIGHT * .8);
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

    public void setModele(Calculatrice modele){
        this.modele = modele;
    }

    private StackPane createStackItem(Operande op, Color c,int index){
        Node t;
        if(op instanceof Operande.OperandeWithInputs) {
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
                            modele.updateValue(index, Calculatrice.TypeParser.parse(value).getValue());
                        }catch(Exception e){
                            System.out.println(e);
                        }
                        printStack();
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

        Text typeText = new Text(op.valueClassToString());
        typeText.setFont(Font.font ("Verdana", FontWeight.BLACK.BOLD, 12));
        typeText.setFill(Color.WHITE);
        type.getChildren().add(typeText);

        InputStream is = getClass().getClassLoader().getResourceAsStream("delete_img.png");
        ImageView imageView = new ImageView( new Image(is));
        imageView.setFitWidth(15); imageView.setFitHeight(15 );
        Button delete = new Button("",imageView);
        delete.setOnAction(actionEvent -> {
            try{
                modele.removeValue(index);
            }catch(Exception e){
                System.out.println(e);
            }
            printStack();
        });

        HBox rightPart = new HBox(10);
        rightPart.getChildren().addAll(delete,type);

        BorderPane bPane = new BorderPane();
        bPane.setLeft(t);
        BorderPane.setAlignment(t,Pos.CENTER_LEFT);
        bPane.setRight(rightPart);

        for(Node n : bPane.getChildren()) BorderPane.setMargin(n,new Insets(10));

        stackItem.getChildren().addAll(bPane);
        return stackItem;
    }
}
