package com.paulo.reminder;

import com.paulo.reminder.service.AppConfigService;
import com.paulo.reminder.service.DesktopNotificationService;
import com.paulo.reminder.service.SourceSyncService;
import com.paulo.reminder.service.TaskSyncService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import javax.swing.*;
import java.awt.*;

@QuarkusMain
public class DesktopInterface implements QuarkusApplication {
    @Inject
    SourceSyncService sourceSyncService;
    @Inject
    TaskSyncService taskSyncService;
    @Inject
    AppConfigService appConfigService;
    @Inject
    DesktopNotificationService desktopNotificationService;

    public static void main(String[] args){
        Quarkus.run(DesktopInterface.class, args);
    }

    @Override
    public int run(String... args){
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Log.warn("No se pudo cargar el estilo moderno de windows");
        }
        System.setProperty("java.awt.headless", "false");

        EventQueue.invokeLater(this::startTray);

        Quarkus.waitForExit();
        return 0;
    }
    private void startTray() {
        if (!SystemTray.isSupported()) return;

        try{
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png"));
            TrayIcon trayIcon = new TrayIcon(image, "Reminder Blitz");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

            desktopNotificationService.setTrayIcon(trayIcon);
            PopupMenu popupMenu = new PopupMenu();

            MenuItem sync = new MenuItem(" sync now");
            MenuItem config = new MenuItem(" settings");
            MenuItem exit = new MenuItem(" exit");

            exit.addActionListener(e -> {
                System.exit(0);
            });
            sync.addActionListener(e -> {
                taskSyncService.syncAll();
            });

            config.addActionListener(e -> showConfigWindow());

            popupMenu.add(sync);
            popupMenu.add(config);
            popupMenu.addSeparator();
            popupMenu.add(exit);

            trayIcon.setPopupMenu(popupMenu);

        }catch(Exception e){
            Log.error("Error al iniciar tray icon" + e);
        }

    }
    private void showConfigWindow() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        var currentConfig = appConfigService.getOrCreateConfig();

        JLabel nameLabel = new JLabel("User Name:");
        nameLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JTextField userNameField = new JTextField(0);
        userNameField.setText(currentConfig.ownerName);
        userNameField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        userNameField.setMaximumSize(new Dimension(225, userNameField.getPreferredSize().height));

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JTextField emailField = new JTextField(0);
        emailField.setText(currentConfig.email);
        emailField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(225, emailField.getPreferredSize().height));

        JLabel intervalLabel = new JLabel("Sync (minutes)");
        intervalLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JSpinner intervalSpinner = new JSpinner(new SpinnerNumberModel(60, 5, 1440, 5));
        intervalSpinner.setValue(currentConfig.interval);
        intervalSpinner.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        intervalSpinner.setMaximumSize(new Dimension(225, intervalSpinner.getPreferredSize().height));

        JComponent editor = intervalSpinner.getEditor();
        JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
        textField.setHorizontalAlignment(JTextField.LEFT);
        textField.setEditable(false);
        textField.setBackground(Color.WHITE);

        JLabel sourceNameLabel = new JLabel("Source Name");
        sourceNameLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JTextField sourceNameField = new JTextField(0);
        sourceNameField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        sourceNameField.setMaximumSize(new Dimension(225, sourceNameField.getPreferredSize().height));

        JLabel sourceLabel = new JLabel("Calendar URL");
        sourceLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JTextField sourceUrlField = new JTextField(0);
        sourceUrlField.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        sourceUrlField.setMaximumSize(new Dimension(225, sourceUrlField.getPreferredSize().height));

        mainPanel.add(nameLabel);
        mainPanel.add(userNameField);
        mainPanel.add(Box.createVerticalStrut(5));

        mainPanel.add(emailLabel);
        mainPanel.add(emailField);
        mainPanel.add(Box.createVerticalStrut(5));

        mainPanel.add(intervalLabel);
        mainPanel.add(intervalSpinner);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(sourceNameLabel);
        mainPanel.add(sourceNameField);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(sourceLabel);
        mainPanel.add(sourceUrlField);


        int result = JOptionPane.showConfirmDialog(
                null,
                mainPanel,
                "App Configuration",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String userName = userNameField.getText();
            int interval = (int) intervalSpinner.getValue();
            String email = emailField.getText();

            appConfigService.updateConfig(userName, email, interval);

            String sourceName = sourceNameField.getText();
            String sourceUrl = sourceUrlField.getText();

            if (!sourceUrl.isBlank() && !sourceName.isBlank()) {
                try {
                    sourceSyncService.addSource(sourceName, sourceUrl);
                } catch (Exception ex) {
                    Log.info("Error al añadir fuente: " + ex.getMessage());
                }
            }
        }
    }

}
