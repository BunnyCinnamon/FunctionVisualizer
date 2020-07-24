module cinnamon {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.expression.parser;

    opens cinnamon to javafx.fxml;
    exports cinnamon;
}