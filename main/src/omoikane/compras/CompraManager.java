package omoikane.compras;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import omoikane.compras.MVC.ComprasCRUDController;
import omoikane.etiquetas.ImpresionEtiquetasController;
import omoikane.etiquetas.ImpresionEtiquetasModel;
import omoikane.principal.Principal;
import omoikane.sistema.Herramientas;
import omoikane.sistema.Permisos;
import omoikane.sistema.SceneOverloaded;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Octavio Ruiz
 * Date: 12/03/13
 * Time: 01:41
 * To change this template use File | Settings | File Templates.
 */
public class CompraManager {

    private JInternalFrame frame;

    private SceneOverloaded initCompra() throws IOException {
        Platform.setImplicitExit(false);
        ApplicationContext context = Principal.applicationContext;

        SceneOverloaded scene = (SceneOverloaded)context.getBean("compraView");

        return scene;
    }


    private SceneOverloaded initComprasCRUD() throws IOException {
        Platform.setImplicitExit(false);
        ApplicationContext context = Principal.applicationContext;

        SceneOverloaded scene = (SceneOverloaded)context.getBean("comprasCRUDView");

        return scene;
    }

    public JInternalFrame startJFXCompra() {
        JInternalFrame frame = null;
        if(omoikane.sistema.Usuarios.cerrojo(Permisos.getPMA_REGISTRARCOMPRA())) frame = _startJFXCompra();
        return frame;
    }

    public JInternalFrame _startJFXCompra() {
        frame = new JInternalFrame("Captura de compras");
        final JFXPanel fxPanel = new JFXPanel();

        frame.setClosable(true);
        frame.add(fxPanel);
        frame.setVisible(true);

        Herramientas.panelCatalogo(frame);
        Principal.getEscritorio().getPanelEscritorio().add(frame);
        frame.setSize(1000, 640);
        frame.setPreferredSize(new Dimension(1000, 640));
        frame.setVisible(true);
        Herramientas.centrarVentana(frame);
        Herramientas.iconificable(frame);
        frame.setResizable(true);
        frame.toFront();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                SceneOverloaded scene = null;
                try {
                    scene = initComprasCRUD();
                    ((ComprasCRUDController)scene.getController()).setFXPanel(fxPanel);
                    ((ComprasCRUDController)scene.getController()).setJInternalFrame(frame);
                    //scene.setFill(null);
                    fxPanel.setScene(scene);
                } catch (IOException e) {

                }

            }
        });

        return frame;
    }

}
