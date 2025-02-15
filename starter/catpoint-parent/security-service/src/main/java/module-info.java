module com.udacity.securityservice{
    requires com.udacity.imageservice;
    requires java.desktop;
    requires java.prefs;
    requires miglayout.swing;
    requires com.google.common;
    requires com.google.gson;
    requires org.junit.jupiter.api;
    requires org.mockito;
//    requires org.junit.jupiter.params;


    exports com.udacity.securityservice.application;
    exports com.udacity.securityservice.data;

//    opens com.udacity.securityservice.service to org.junit.jupiter.api, org.mockito;
//    opens com.udacity.securityservice.data to org.junit.jupiter.api, org.mockito;
    opens com.udacity.securityservice.service to org.junit.platform.commons, org.mockito;
}
