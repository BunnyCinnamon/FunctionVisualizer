module cinnamon {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.expression.parser;
    requires com.google.common;

    opens cinnamon to javafx.fxml;
    exports cinnamon;
}