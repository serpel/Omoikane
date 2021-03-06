 /* Author Phesus        //////////////////////////////
 *  ORC,ACR             /////////////
 *                     /////////////
 *                    /////////////
 *                   /////////////
 * //////////////////////////////                   */

package omoikane.principal

 import omoikane.formularios.CatalogoVentas
 import omoikane.principal.*
import omoikane.sistema.*
import groovy.sql.*;
import groovy.swing.*;
import javax.swing.*;
import java.awt.event.WindowListener;
import javax.swing.event.*;
import groovy.inspect.swingui.*;
import javax.swing.table.TableColumn
import java.awt.event.*;
import groovy.swing.*

 import java.text.NumberFormat

 import static omoikane.sistema.Usuarios.*;
import static omoikane.sistema.Permisos.*;
import omoikane.sistema.n2t.*;

class Ventas {

    static def lastMovID  = -1
    static def IDAlmacen = Principal.IDAlmacen
    static def escritorio = omoikane.principal.Principal.escritorio

    static def lanzarCatalogo() //lanza el catalogo de ventas
    {
        if(cerrojo(PMA_ABRIRVENTAS)){
            CatalogoVentas cat = (new omoikane.formularios.CatalogoVentas())
            cat.setVisible(true);
            escritorio.getPanelEscritorio().add(cat)
            
            Herramientas.panelCatalogo(cat)
            cat.txtBusqueda.keyReleased = { if(it.keyCode == it.VK_ESCAPE) cat.btnCerrar.doClick() }
            cat.btnVentasXLinea.actionPerformed = { lanzarVentasXLinea(); }

            Herramientas.iconificable(cat)
            cat.toFront()
            try { cat.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario catálogo ventas", Herramientas.getStackTraceString(e)) }
            cat.txtBusqueda.requestFocus()
            return cat
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarDetalles(ID)
    {
        if(cerrojo(PMA_DETALLESVENTAS)){
            lastMovID = ID
            def form = (new omoikane.formularios.VentasDetalles())
            form.setVisible(true);
            escritorio.getPanelEscritorio().add(form)
            Herramientas.setColumnsWidth(form.jTable1, [0.2,0.5,0.1,0.1,0.1])
            Herramientas.panelFormulario(form)
            Herramientas.In2ActionX(form, KeyEvent.VK_F1    , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F3    , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F4    , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F5    , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F6    , "facturar" ) { form.btnFacturado.doClick()}
            Herramientas.In2ActionX(form, KeyEvent.VK_F7    , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F8    , "imprimir" ) { form.btnImprimir.doClick() }
            Herramientas.In2ActionX(form, KeyEvent.VK_F11   , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_F12   , "nada"     ) { }
            Herramientas.In2ActionX(form, KeyEvent.VK_DELETE, "nada"     ) { }
            SwingBuilder.build {
            //Al presionar F1: (lanzarCatalogoDialogo) de cliente
            form.getFactura().keyReleased = {
                if(it.keyCode == it.VK_F1)
                Thread.start { form.getFactura().setText(Clientes.lanzarDialogoCatalogo());form.getFactura().requestFocus() } }
            form.IDSeleccionado=ID
            Herramientas.iconificable(form)
            form.toFront()
            try { form.setSelected(true) } catch(Exception e) { Dialogos.lanzarDialogoError(null, "Error al iniciar formulario nuevo movimiento de almacén", Herramientas.getStackTraceString(e)) }
            def serv    = Nadesico.conectar()
            def mov     = serv.getVenta(ID,IDAlmacen)
            serv.desconectar()

            NumberFormat mf = NumberFormat.getCurrencyInstance();
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            nf.setGroupingUsed(true);

            form.setIDVenta(mov.id_venta as String)
            form.setCliente(mov.nombreCliente as String)
            form.setDescuento( nf.format( mov.descuento ) )
            form.setImpuesto ( nf.format( mov.impuestos ) )
            if (mov.facturada as Boolean){form.setFacturar(mov.id_cliente as String)}
            form.setSubtotal( nf.format ( mov.subtotal ) )
            form.setTotal( mf.format( mov.total ) )
            form.setAlmacen(mov.nombreAlmacen as String)
            form.setFecha(mov.fecha_hora as String)
            form.setTablaPrincipal(mov.tabMatriz as List)
            def n = new omoikane.sistema.n2t()
            form.letra = n.aCifra(mov.total)
        }

            return form
        }else{Dialogos.lanzarAlerta("Acceso Denegado")}
    }

    static def lanzarVentasXLinea() {
        
        new VentasXLinea(new omoikane.formularios.VentasXLineaReporte())
    }
    static def lanzarImprimir(queryMovs)
    {
        def reporte = new Reporte('omoikane/reportes/ReporteVentas.jasper', [QueryTxt:queryMovs]);
        reporte.lanzarPreview()
    }

    static def lanzarImprimirVenta(form)
    {
        def reporte = new Reporte('omoikane/reportes/VentaEncabezado.jasper',[SUBREPORT_DIR:"omoikane/reportes/",IDMov:lastMovID as String]);
        reporte.lanzarPreview()
    }

    static def lanzarImprimirFactura(form,numeroletra)
    {
        def reporte = new Reporte('omoikane/reportes/FacturaEncabezado.jasper',[SUBREPORT_DIR:"omoikane/reportes/",IDVenta:lastMovID as String,NumLetra:numeroletra,cliente:form.getCliente()]);
        reporte.lanzarPreview()
    }

    static def reimprimirTicket(form)
    {
        def comprobante = omoikane.principal.Principal.applicationContext.getBean(Comprobantes.class);
        comprobante.ticket(IDAlmacen, form as Long)//imprimir ticket
        comprobante.imprimir()//imprimir ticket
    }

    static def actualizar(id,cliente)
    {
        def serv    = Nadesico.conectar()
        def mov     = serv.modVenta(id,cliente)
        serv.desconectar()
    }


}

