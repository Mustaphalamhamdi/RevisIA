module com.mustapha.revisia.revisia_new {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires org.apache.pdfbox;

    requires java.naming;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires java.desktop;  // Add this line
    opens com.mustapha.revisia.models to org.hibernate.orm.core;  // Add this line
    exports com.mustapha.revisia.models;  // Add this line

    opens com.mustapha.revisia to javafx.fxml;
    opens com.mustapha.revisia.controllers to javafx.fxml;

    exports com.mustapha.revisia;
    exports com.mustapha.revisia.controllers;

}