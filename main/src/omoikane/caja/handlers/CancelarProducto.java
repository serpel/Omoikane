package omoikane.caja.handlers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import omoikane.caja.business.Security;
import omoikane.caja.presentation.CajaController;
import omoikane.caja.presentation.ProductoModel;
import omoikane.entities.Cancelacion;
import omoikane.entities.Usuario;
import omoikane.principal.Principal;
import omoikane.producto.Articulo;
import omoikane.repository.CancelacionRepo;
import omoikane.repository.VentaRepo;
import omoikane.sistema.Permisos;
import omoikane.sistema.Usuarios;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Octavio
 * Date: 6/12/12
 * Time: 04:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class CancelarProducto extends ICajaEventHandler {

    VentaKBHandler ventaKBHandler;
    CancelacionRepo repo;
    VentaRepo ventaRepo;

    public static Logger logger = Logger.getLogger(CancelarVenta.class);

    public CancelarProducto(CajaController controller) {
        super(controller);
        ventaKBHandler = new VentaKBHandler();
        repo = (CancelacionRepo) Principal.applicationContext.getBean("cancelacionRepo");
        ventaRepo = (VentaRepo) Principal.applicationContext.getBean("ventaRepo");
    }

    @Override
    public void handle(Event event) {
        modoCancelar();
    }

    public void cancelar() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _cancelar();
            }
        });
    }

    private void _cancelar() {
        Boolean auth = Security.cancelacion();

        final Boolean finalAuth = auth;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    if(finalAuth) {
                        Integer selectedRow = getController().getVentaTableView().getSelectionModel().selectedIndexProperty().getValue();
                        ProductoModel quitar = getController().getVentaTableView().getSelectionModel().getSelectedItem();
                        getController().getCajaLogic().deleteRowFromVenta(selectedRow);
                        registrar(quitar);
                    } else {
                        getController().getCapturaTextField().requestFocus();
                    }
                } catch (Exception e) {
                    logger.error("Error al cancelar producto", e);
                }
                return null;
            }
        };
        Platform.runLater(task);
    }

    private void registrar(ProductoModel quitar) {
        Cancelacion c = new Cancelacion();
        c.setArticulo   ( new Articulo( quitar.getLongId() ) );
        c.setCajero     ( new Usuario( new Long(Usuarios.getIDUsuarioActivo()   ) ) );
        c.setAutorizador( new Usuario( new Long(Usuarios.getIDUltimoAutorizado()) ) );
        repo.save(c);
    }

    public void modoCancelar() {
        getController().setCapturaPaneDisable(true);
        getController().setMainToolBarDisable(true);
        getController().showHud("[Enter] Cancelar producto \n[Esc] No cancelar");
        getController().getVentaTableView().requestFocus();
        getController().getVentaTableView().onKeyReleasedProperty().set(ventaKBHandler);
    }

    public void modoNormal() {

        Platform.runLater(() -> _modoNormal());
    }

    private void _modoNormal() {
        getController().setCapturaPaneDisable(false);
        getController().setMainToolBarDisable(false);
        getController().hideHud();
        getController().getVentaTableView().onKeyReleasedProperty().set(null);
        getController().getJInternalFrame().toFront();
        getController().getFxPanel().requestFocus();
        getController().getMainAnchorPane().requestFocus();
        getController().getCapturaTextField().requestFocus();
    }

    public class VentaKBHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            CajaController controller = CancelarProducto.this.getController();

            if(event.getCode() == KeyCode.ENTER) {
                CancelarProducto.this.cancelar();
                CancelarProducto.this.modoNormal();

                event.consume();
            }
            if(event.getCode() == KeyCode.ESCAPE) {
                CancelarProducto.this.modoNormal();
            }
        }
    }
}
